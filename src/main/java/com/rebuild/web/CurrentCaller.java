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

package com.rebuild.web;

import cn.devezhao.persist4j.engine.ID;

/**
 * 当前请求用户（线程量）
 * 
 * @author zhaofang123@gmail.com
 * @since 11/20/2017
 */
public class CurrentCaller {

	private static final ThreadLocal<ID> CALLER = new ThreadLocal<>();
	
	protected void set(ID user) {
		CALLER.set(user);
	}
	
	protected void clear() {
		CALLER.remove();
	}

	/**
	 * @return
	 * @throws InvalidRequestException
	 */
	public ID get() throws InvalidRequestException {
		return get(false);
	}
	
	/**
	 * @return
	 * @throws InvalidRequestException
	 */
	public ID get(boolean allowNull) throws InvalidRequestException {
		ID user = CALLER.get();
		if (user == null && allowNull == false) {
			throw new InvalidRequestException(403, "无效请求用户");
		}
		return user;
	}
}
