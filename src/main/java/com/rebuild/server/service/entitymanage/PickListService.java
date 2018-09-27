/*
rebuild - Building your system freely.
Copyright (C) 2018 devezhao <zhaofang123@gmail.com>

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

package com.rebuild.server.service.entitymanage;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rebuild.server.Application;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.server.service.BaseService;

import cn.devezhao.persist4j.Field;
import cn.devezhao.persist4j.PersistManagerFactory;
import cn.devezhao.persist4j.Record;
import cn.devezhao.persist4j.engine.ID;

/**
 * 
 * @author zhaofang123@gmail.com
 * @since 09/07/2018
 */
public class PickListService extends BaseService  {

	protected PickListService(PersistManagerFactory persistManagerFactory) {
		super(persistManagerFactory);
	}

	@Override
	public int getEntity() {
		return EntityHelper.PickList;
	}

	/**
	 * 保存配置
	 * 
	 * @param field
	 * @param config
	 */
	public void txBatchUpdate(Field field, JSONObject config) {
		Assert.notNull(config, "无效配置");
		ID user = Application.getCurrentCallerUser();
		
		JSONArray showItem = config.getJSONArray("show");
		JSONArray hideItem = config.getJSONArray("hide");
		
		Object[][] itemsHold = Application.createNoFilterQuery(
				"select itemId from PickList where belongEntity = ? and belongField = ?")
				.setParameter(1, field.getOwnEntity().getName())
				.setParameter(2, field.getName())
				.array();
		Set<ID> itemsHoldList = new HashSet<>();
		for (Object[] o : itemsHold) {
			itemsHoldList.add((ID) o[0]);
		}
		
		for (Object o : hideItem) {
			JSONObject item = (JSONObject) o;
			String id = item.getString("id");
			if (!ID.isId(id)) {
				continue;
			}
			
			ID id2id = ID.valueOf(id);
			Record r = EntityHelper.forUpdate(id2id, user);
			r.setBoolean("isHide", true);
			r.setString("text", item.getString("text"));
			update(r);
			itemsHoldList.remove(id2id);
		}
		
		int seq = 0;
		for (Object o : showItem) {
			JSONObject item = (JSONObject) o;
			String id = item.getString("id");
			ID id2id = ID.isId(id) ? ID.valueOf(id) : null;
			
			Record r = id2id == null 
					? EntityHelper.forNew(EntityHelper.PickList, user) : EntityHelper.forUpdate(id2id, user);
			r.setInt("seq", seq++);
			r.setString("text", item.getString("text"));
			r.setBoolean("isHide", false);
			r.setBoolean("isDefault", item.getBoolean("default"));
			if (id2id == null) {
				r.setString("belongEntity", field.getOwnEntity().getName());
				r.setString("belongField", field.getName());
			}
			createOrUpdate(r);
			
			if (id2id != null) {
				itemsHoldList.remove(id2id);
			}
		}
		
		for (ID id : itemsHoldList) {
			delete(id);
		}
	}
}
