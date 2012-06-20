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


/**
 * Main interface for requestUrl-controllers.
 * A controller is like a stateless bean: it must be able to handle many concurrent requests
 * (which result in various threads calling {@link #handleRequest(HttpServletRequest, HttpServletResponse)} 
 * at the same time).
 * Therefor, a controller should not have any class-variables unless they are carefully managed 
 * (e.g. a {@link java.util.concurrent.ConcurrentHashMap}).
 * If requests results in data/structures that are not manageable within the scope of the handleRequest-method,
 * create new objects for each request (this will have impact on performace) and manage data/structures in 
 * these objects. 
 * <br>Controllers can/should use static functions from {@link ControllerUtil}   
 * @author frederikw
 *
 */
public interface Controller {

	/** The name of the attribute for the title of the web-page, used in jsp-files. */
	String PAGE_TITLE = "pageTitle";
	
	/** 
	 * Called by {@link AppServlet} to handle a request.
	 * Any {@link Throwable} from this method is catched by the {@link AppServlet}
	 * in which case {@link AppServlet} will send a 500 "internal server error" response.
	 * @return null (response has been handled by controller) or the jsp-page fileName to display.
	 */
	String handleRequest(HttpServletRequest request, HttpServletResponse response);

	/** 
	 * The name for the controller. If the name ends with "Url" the name will
	 * be registered in ServletContext with an associated request-URL.
	 * In this manner, the jsp-pages can use the name to refer to another jsp-page. 
	 * @return The name for the controller.
	 */
	String getName();
}
