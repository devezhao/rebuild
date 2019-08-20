/*
rebuild - Building your business-systems freely.
Copyright (C) 2019 devezhao <zhaofang123@gmail.com>

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

package com.rebuild.server.business.recyclebin;

import cn.devezhao.persist4j.engine.ID;
import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * 记录序列化为 JSON
 *
 * @author devezhao
 * @since 2019/8/19
 */
public class RecycleBean implements Serializable {
    private static final long serialVersionUID = -1058552856844427594L;

    final private ID recordId;

    /**
     * @param recordId
     */
    public RecycleBean(ID recordId) {
        this.recordId = recordId;
    }

    /**
     * 序列化（含明细）
     *
     * @return
     */
    public JSON serialize() {
        return null;
    }
}