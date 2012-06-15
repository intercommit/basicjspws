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

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Servlet called for all pages after {@link AppFilter} has pre-processed the requests.
 * The servlet depends on servlet context variables set by {@link AppInit} to map 
 * URLs with page-controllers. The servlet also depends on the {@link AppFilter} to set 
 * the correct "requestedUrl".
 * Page-controllers can return the name of the jsp-page to display (set in {@link UrlController#jspFileName}),
 * which this servlet will handle using a RequestDispatcher.
 * <br>New controllers can be added by extending this class and overloading {@link #init()}.
 * If this is done, register the new servlet in WebContent/WEB-INF/web.xml
 * as the servlet-class for MainServlet.
 */
public class AppServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(AppServlet.class);
	
	/**
	 * Registers the controllers per UrlController.
	 */
	@Override
    public void init() throws ServletException {
    	
		UrlControllers ucs = AppInit.appInstance.urlControllers;
    	IndexController ic = new IndexController();
		ucs.get(UrlController.BASE_URL).controller = ic;
		ucs.get(UrlController.INDEX_PAGE).controller = ic;
		ucs.get(UrlController.STATS_PAGE).controller = new StatsPageController();
		ucs.get(UrlController.SYSENV_PAGE).controller = new SysEnvPageController();
		ucs.get(UrlController.LOG_PAGE).controller = new LogPageController();
		ucs.get(UrlController.LOG_ERROR_PAGE).controller = new LogErrorPageController();
		ucs.get(UrlController.LOG_STATUS_PAGE).controller = new LogStatusPageController();
    	log.debug("Servlet initialized");
    }

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		Controller handler = AppInit.appInstance.urlControllers.getByRequestUrl((String)request.getAttribute("requestedUrl")); // Set by MainFilter
		if (handler == null) {
			response.getWriter().write("Cannot find controller for URL " + request.getAttribute("requestedUrl"));
			log.warn("No controller available for URL " + request.getAttribute("requestedUrl"));
			return;
		}
		String viewName = null;
		try {
			viewName = handler.handleRequest(request, response);
		} catch (Exception e) {
			log.error("Controller " + handler.getClass().getName() + " failed to handle request properly.", e);
			ControllerUtil.sendError(response, 500, "Cannot process request for URL " + request.getAttribute("requestedUrl") + ": " + e);
			return;
		} catch (Throwable t) {
			log.error("Server barfed while executing controller " + handler.getClass().getName(), t);
			ControllerUtil.sendError(response, 500, "Server having trouble processing request for URL " + request.getAttribute("requestedUrl") + ": " + t);
			return;
		}
		if (isEmpty(viewName)) {
			//response.getWriter().write("Controller did not return a page for for URL " + request.getAttribute("requestedUrl"));
			//log.warn("Controller " + handler.getClass().getName() + " did not return a view-page for URL " + request.getAttribute("requestedUrl"));
			log.debug("doPost done.");
			return;
		}
		RequestDispatcher view = request.getRequestDispatcher(viewName);
		if (view == null) {
			log.warn("Controller " + handler.getClass().getName() + " returned view page " + viewName +", but the page does not exist.");
			ControllerUtil.sendError(response, 404, "Could not find page " + viewName);
			return;
		}
		view.forward(request, response);
		log.debug("doPost done for {}", viewName);
	}
	
	@Override
	public void destroy() {}
	
	public static boolean isEmpty(final String s) { return (s == null || s.trim().isEmpty()); };
	
}
