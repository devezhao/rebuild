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

package com.rebuild.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

import com.rebuild.server.ServerListener;
import com.rebuild.server.helper.ConfigurableItem;
import com.rebuild.server.helper.SysConfiguration;

/**
 * 页面 Controll
 * 
 * @author zhaofang123@gmail.com
 * @since 09/20/2018
 */
public abstract class BasePageControll extends BaseControll {

	/**
	 * @param page
	 * @return
	 */
	protected ModelAndView createModelAndView(String page) {
		ModelAndView mv = new ModelAndView(page);
		setPageAttribute(mv);
		return mv;
	}
	
	// -- 页面公用属性
	
	/**
	 * @param into
	 */
	public static void setPageAttribute(HttpServletRequest into) {
		into.setAttribute("baseUrl", ServerListener.getContextPath());
		into.setAttribute("appName", SysConfiguration.get(ConfigurableItem.AppName, false));
	}
	
	/**
	 * @param into
	 */
	public static void setPageAttribute(ModelAndView into) {
		into.getModel().put("baseUrl", ServerListener.getContextPath());
		into.getModel().put("appName", SysConfiguration.get(ConfigurableItem.AppName, false));
	}
}
