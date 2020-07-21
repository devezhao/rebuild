/*
Copyright (c) REBUILD <https://getrebuild.com/> and its owners. All rights reserved.

rebuild is dual-licensed under commercial and open source licenses (GPLv3).
See LICENSE and COMMERCIAL in the project root for license information.
*/

package com.rebuild.server.service.project;

import cn.devezhao.commons.CalendarUtils;
import cn.devezhao.persist4j.PersistManagerFactory;
import cn.devezhao.persist4j.Record;
import cn.devezhao.persist4j.engine.ID;
import com.rebuild.server.Application;
import com.rebuild.server.configuration.ProjectManager;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.server.service.BaseService;

/**
 * @author devezhao
 * @since 2020/7/2
 */
public class ProjectTaskService extends BaseService {

    // 中值法排序
    private static final int MID_VALUE = 1000;

    protected ProjectTaskService(PersistManagerFactory aPMFactory) {
        super(aPMFactory);
    }

    @Override
    public Record create(Record record) {
        record.setLong("taskNumber", getNextTaskNumber(record.getID("projectId")));
        record.setInt("seq", getNextSeqViaMidValue(record.getID("projectPlanId")));
        return super.create(record);
    }

    @Override
    public Record update(Record record) {
        if (record.hasValue("status")) {
            int status = record.getInt("status");
            if (status == 0) {
                record.setNull("endTime");
                record.setInt("seq", getSeqInStatus(record.getPrimary(), false));
            } else {
                record.setDate("endTime", CalendarUtils.now());
                record.setInt("seq", getSeqInStatus(record.getPrimary(), true));
            }

        } else if (record.hasValue("seq")) {
            int seq = record.getInt("seq");
            if (seq == -1) {
                record.setInt("seq", getSeqInStatus(record.getPrimary(), true));
            }
        }

        return super.update(record);
    }

    @Override
    public int delete(ID taskId) {
        int d = super.delete(taskId);
        ProjectManager.instance.clean(taskId);
        return d;
    }

    /**
     * @param projectId
     * @return
     */
    synchronized
    private long getNextTaskNumber(ID projectId) {
        Object[] max = Application.createQueryNoFilter(
                "select max(taskNumber) from ProjectTask where projectId = ?")
                .setParameter(1, projectId)
                .unique();
        return (max == null || max[0] == null) ? 1 : ((Long) max[0] + 1);
    }

    /**
     * @param projectPlanId
     * @return
     */
    synchronized
    private int getNextSeqViaMidValue(ID projectPlanId) {
        Object[] seqMax = Application.createQueryNoFilter(
                "select max(seq) from ProjectTask where projectPlanId = ?")
                .setParameter(1, projectPlanId)
                .unique();
        return (seqMax == null || seqMax[0] == null) ? 0 : ((Integer) seqMax[0] + MID_VALUE);
    }

    /**
     * @param taskId
     * @param isMax Max or min
     * @return
     */
    synchronized
    private int getSeqInStatus(ID taskId, boolean isMax) {
        Object[] taskStatus = Application.createQuery(
                "select status,projectPlanId from ProjectTask where taskId = ?")
                .setParameter(1, taskId)
                .unique();
        if (taskStatus == null) return 1;

        Object[] seq = Application.createQuery(
                "select " + (isMax ? "max" : "min") + "(seq) from ProjectTask where status = ? and projectPlanId = ?")
                .setParameter(1, taskStatus[0])
                .setParameter(2, taskStatus[1])
                .unique();

        if (isMax) return (Integer) seq[0] + MID_VALUE;
        else return (Integer) seq[0] - MID_VALUE;
    }

    @Override
    public int getEntityCode() {
        return EntityHelper.ProjectTask;
    }
}