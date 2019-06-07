/*
rebuild - Building your business-systems freely.
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

package com.rebuild.server.configuration.portals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rebuild.server.Application;
import com.rebuild.server.configuration.ConfigEntry;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.server.metadata.MetadataHelper;
import com.rebuild.server.metadata.entityhub.EasyMeta;
import com.rebuild.utils.JSONUtils;

import cn.devezhao.persist4j.Entity;
import cn.devezhao.persist4j.Field;
import cn.devezhao.persist4j.engine.ID;

/**
 * 数据列表
 * 
 * @author zhaofang123@gmail.com
 * @since 08/30/2018
 */
public class DataListManager extends BaseLayoutManager {
	
	public static final DataListManager instance = new DataListManager();
	private DataListManager() { }
	
	/**
	 * @param belongEntity
	 * @param user
	 * @return
	 */
	public JSON getColumnLayout(String belongEntity, ID user) {
		return getColumnLayout(belongEntity, user, true);
	}

	/**
	 * @param belongEntity
	 * @param user
	 * @param validPrivileges
	 * @return
	 */
	public JSON getColumnLayout(String belongEntity, ID user, boolean validPrivileges) {
		ConfigEntry config = getLayoutOfDatalist(user, belongEntity);
		
		List<Map<String, Object>> columnList = new ArrayList<>();
		Entity entityMeta = MetadataHelper.getEntity(belongEntity);
		Field namedField = MetadataHelper.getNameField(entityMeta);
		
		// 默认配置
		if (config == null) {
			columnList.add(formatColumn(namedField));
			
			String namedFieldName = namedField.getName();
			if (!StringUtils.equalsIgnoreCase(namedFieldName, EntityHelper.CreatedBy)
					&& entityMeta.containsField(EntityHelper.CreatedBy)) {
				columnList.add(formatColumn(entityMeta.getField(EntityHelper.CreatedBy)));
			}
			if (!StringUtils.equalsIgnoreCase(namedFieldName, EntityHelper.CreatedOn)
					&& entityMeta.containsField(EntityHelper.CreatedOn)) {
				columnList.add(formatColumn(entityMeta.getField(EntityHelper.CreatedOn)));
			}
		} else {
			for (Object o : (JSONArray) config.getJSON("config")) {
				JSONObject item = (JSONObject) o;
				String field = item.getString("field");
				Field lastField = MetadataHelper.getLastJoinField(entityMeta, field);
				if (lastField == null) {
					LOG.warn("Unknow field '" + field + "' in '" + belongEntity + "'");
					continue;
				}
				
				String fieldPath[] = field.split("\\.");
				Map<String, Object> formatted = null;
				if (fieldPath.length == 1) {
					formatted = formatColumn(lastField);
				} else {
					
					// 如果没有引用实体的读权限，则直接过滤掉字段
					
					Field parentField = entityMeta.getField(fieldPath[0]);
					if (!validPrivileges) {
						formatted = formatColumn(lastField, parentField);
					} else if (Application.getSecurityManager().allowedR(user, lastField.getOwnEntity().getEntityCode())) {
						formatted = formatColumn(lastField, parentField);
					}
				}
				
				if (formatted != null) {
					Integer width = item.getInteger("width");
					if (width != null) {
						formatted.put("width", width);
					}
					columnList.add(formatted);
				}
			}
		}
		
		return JSONUtils.toJSONObject(
				new String[] { "entity", "nameField", "fields" },
				new Object[] { belongEntity, namedField.getName(), columnList });
	}
	
	/**
	 * @param field
	 * @return
	 */
	public Map<String, Object> formatColumn(Field field) {
		return formatColumn(field, null);
	}
	
	/**
	 * @param field
	 * @param parent
	 * @return
	 */
	public Map<String, Object> formatColumn(Field field, Field parent) {
		String parentField = parent == null ? "" : (parent.getName() + ".");
		String parentLabel = parent == null ? "" : (EasyMeta.getLabel(parent) + ".");
		EasyMeta easyField = new EasyMeta(field);
		return JSONUtils.toJSONObject(
				new String[] { "field", "label", "type" },
				new Object[] { parentField + easyField.getName(), parentLabel + easyField.getLabel(), easyField.getDisplayType(false) });
	}
}
