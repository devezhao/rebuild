/*
rebuild - Building your business-systems freely.
Copyright (C) 2018-2019 devezhao <zhaofang123@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package com.rebuild.server.helper.datalist;

import cn.devezhao.persist4j.Entity;
import cn.devezhao.persist4j.Query;
import cn.devezhao.persist4j.engine.ID;
import cn.devezhao.persist4j.util.support.QueryHelper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rebuild.server.Application;
import com.rebuild.server.helper.SetUser;
import com.rebuild.server.metadata.MetadataHelper;
import com.rebuild.server.service.query.ParserTokens;

import java.util.HashSet;
import java.util.Set;

/**
 * 批量选择记录（在列表中）查询器
 *
 * @author ZHAO
 * @since 2019/12/1
 */
public class BatchOperatorQuery extends SetUser<BatchOperatorQuery> {

    /**
     * 选中数据
     */
    static final int DR_SELECTED = 1;
    /**
     * 当前页数据
     */
    static final int DR_PAGED = 2;
    /**
     * 查询后数据
     */
    static final int DR_QUERYED = 3;
    /**
     * 全部数据
     */
    static final int DR_ALL = 4;

    private int dataRange;
    private JSONObject queryData;

    /**
     * @param dataRange
     * @param queryData
     */
    public BatchOperatorQuery(int dataRange, JSONObject queryData) {
        this.dataRange = dataRange;
        this.queryData = queryData;
    }

    /**
     * 对查询数据进行包装（根据数据范围）
     *
     * @param maxRows
     * @return
     */
    public JSONObject wrapQueryData(int maxRows) {
        if (this.dataRange != DR_PAGED) {
            queryData.put("pageNo", 1);
            queryData.put("pageSize", maxRows);  // Max
        }
        if (this.dataRange == DR_SELECTED || this.dataRange == DR_ALL) {
            queryData.remove("filter");
            queryData.remove("advFilter");
        }
        if (this.dataRange == DR_SELECTED) {
            JSONObject idsItem = new JSONObject();
            idsItem.put("op", ParserTokens.IN);
            idsItem.put("field", getEntity().getPrimaryField().getName());
            idsItem.put("value", queryData.getString("_selected"));

            JSONArray items = new JSONArray();
            items.add(idsItem);
            JSONObject filter = new JSONObject();
            filter.put("items", items);
            queryData.put("filter", filter);
        }

        queryData.put("reload", Boolean.FALSE);
        return queryData;
    }

    /**
     * 获取查询 SQL-where
     *
     * @return
     */
    public String getFilterSql() {
        QueryParser queryParser = new QueryParser(wrapQueryData(Integer.MAX_VALUE));
        return queryParser.toWhereSql();
    }

    /**
     * 直接获取记录 ID
     *
     * @return
     */
    public ID[] getQueryedRecords() {
        if (this.dataRange == DR_SELECTED) {
            String selected = queryData.getString("_selected");

            Set<ID> ids = new HashSet<>();
            for (String s : selected.split("\\|")) {
                if (ID.isId(s)) {
                    ids.add(ID.valueOf(s));
                }
            }
            return ids.toArray(new ID[0]);
        }

        Entity entity = getEntity();
        String sql = String.format("select %s from %s where %s",
                entity.getPrimaryField().getName(), entity.getName(), getFilterSql());
        Query query = Application.getQueryFactory().createQuery(sql, getUser());
        Object[][] array = QueryHelper.readArray(query);

        Set<ID> ids = new HashSet<>();
        for (Object[] o : array) {
            ids.add((ID) o[0]);
        }
        return ids.toArray(new ID[0]);
    }

    /**
     * @return
     */
    private Entity getEntity() {
        String entityName = queryData.getString("entity");
        return MetadataHelper.getEntity(entityName);
    }
}