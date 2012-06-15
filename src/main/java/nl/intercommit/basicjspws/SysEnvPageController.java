/*  Copyright 2012 InterCommIT b.v.
*
*  This file is part of the "BasicJspWs" project hosted on https://github.com/intercommit/basicjspws
*
*  BasicJspWs is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  any later version.
*
*  BasicJspWs is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with BasicJspWs. If not, see <http://www.gnu.org/licenses/>.
*
*/
package nl.intercommit.basicjspws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static nl.intercommit.basicjspws.ControllerUtil.*;

/**
 * Show the system environment using {@link SysPropsUtil}.
 * Uses {@link UrlController#SYSENV_PAGE}
 * @author frederikw
 *
 */
public class SysEnvPageController implements Controller {

	@Override
	public String handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
		
		request.setAttribute(PAGE_TITLE, AppInit.appInstance.appName + " system environemnt");
		request.setAttribute("systemEnv", SysPropsUtil.getSystemEnv());
		request.setAttribute("systemProps", SysPropsUtil.getSystemProps());
		return getAppPage(UrlController.SYSENV_PAGE);
	}
}
