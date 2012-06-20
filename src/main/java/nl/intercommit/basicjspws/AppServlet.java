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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.intercommit.basicjspws.controllers.*;
import static nl.intercommit.basicjspws.ControllerUtil.getRemoteLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Servlet called for all pages after {@link AppFilter} has pre-processed the requests.
 * <br>New controllers can be added by extending this class and overloading 
 * {@link #registerRequestControllers(String)}, {@link #registerRequestUrlsInServletContextByControllerName(ServletContext)}
 * and/or {@link #init()}.
 * After overloading, register the new servlet class in WebContent/WEB-INF/web.xml
 * as the servlet-class for MainServlet.
 */
public class AppServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(AppServlet.class);
	
	/** All controllers related to their request-URL (used by {@link #doPost(HttpServletRequest, HttpServletResponse)}
	 * to lookup the controller that handles the request). 
	 */
	protected Map<String, Controller> requestControllers = new HashMap<String, Controller>(); 
	
	
	/**
	 * Fills {@link #requestControllers} (request URLs with associated Controller instances).
	 * @param baseUrl e.g. "/baseName/" (always ends and starts with a /). 
	 */
	protected void registerRequestControllers(final String baseUrl) {
		
		requestControllers.put(baseUrl, new Index());
		requestControllers.put(baseUrl + "pages/index", new Index());
		requestControllers.put(baseUrl + "pages/stats", new Stats());
		requestControllers.put(baseUrl + "pages/sysenv", new SysEnv());
		requestControllers.put(baseUrl + "pages/log", new Log());
		requestControllers.put(baseUrl + "pages/logerror", new LogError());
		requestControllers.put(baseUrl + "pages/logstatus", new LogStatus());
	}
	
	/** 
	 * For all {@link Controller}s that have a name ending with "Url", register
	 * the requestUrl as an attribute in ServletContext using the {@link Controller#getName()}
	 * as key.
	 * Also registers "appImagesUrl" to point to the images-directory (for use in jsp-pages).
	 */
	protected void registerRequestUrlsInServletContextByControllerName(final ServletContext sc) {
		
		sc.setAttribute("appImagesUrl", AppInit.appInstance.baseUrl + "images");
		int count = 1;
		Map<Controller, String> reverse = getControllersByRequest();
		for(Controller c  : requestControllers.values()) {
			if (c.getName() != null && c.getName().endsWith("Url")) {
				sc.setAttribute(c.getName(), reverse.get(c));
				count++;
			}
		}
		log.debug("Total of " + count + " urls registered as attribute.");
	}
	
	/**
	 * @return a reverse map of {@link #requestControllers}.
	 */
	protected Map<Controller, String> getControllersByRequest() {
		
		HashMap<Controller, String> reverse = new HashMap<Controller, String>();
		for(String rurl : requestControllers.keySet()) {
			if (requestControllers.get(rurl) != null) reverse.put(requestControllers.get(rurl), rurl);
		}
		return reverse;
	}

	/**
	 * Calls {@link #registerRequestControllers(String)} and 
	 * {@link #registerRequestUrlsInServletContextByControllerName(ServletContext)}.
	 */
	@Override
    public void init() throws ServletException {
    	
		String baseUrl = AppInit.appInstance.baseUrl;
		registerRequestControllers(baseUrl);
		registerRequestUrlsInServletContextByControllerName(getServletContext());
    	log.debug("Servlet initialized");
    }

	/** 
	 * Calls {@link #doPost(HttpServletRequest, HttpServletResponse)}
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * Looks up the controller for the "requestedUrl" (set by {@link AppFilter}) and executes the found controller.
	 * If the controller returns a non-null String, a jsp-page is displayed.
	 * <br>IOExceptions from the request and response objects should bubble up and NOT be catched, 
	 * see also http://stackoverflow.com/questions/4300513/best-practice-response-getoutputstream 
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		Controller handler = requestControllers.get((String)request.getAttribute("requestedUrl"));
		if (handler == null) {
			response.sendError(404, "No controller found for "  + request.getAttribute("requestedUrl"));
			log.warn(getRemoteLocation(request) + " No controller available for URL " + request.getAttribute("requestedUrl"));
			return;
		}
		String viewName = null;
		try {
			viewName = handler.handleRequest(request, response);
		} catch (Exception e) {
			// Runtime exception from controller, e.g. IndexOutOfBoundsExcpetion.
			log.error(getRemoteLocation(request) + " Controller " + handler.getClass().getName() + " failed to handle request properly.", e);
			response.sendError(500, "Cannot process request for URL " + request.getAttribute("requestedUrl") + ": " + e);
			return;
		} catch (Throwable t) {
			// Really bad, e.g. OutOfMemoryError or StackOverflowError.
			log.error(getRemoteLocation(request) + " Server barfed while executing controller " + handler.getClass().getName(), t);
			response.sendError(500, "Server having trouble processing request for URL " + request.getAttribute("requestedUrl") + ": " + t);
			return;
		}
		if (isEmpty(viewName)) {
			if (log.isTraceEnabled()) log.trace(getRemoteLocation(request) + " doPost done.");
			return;
		}
		RequestDispatcher view = request.getRequestDispatcher(viewName);
		if (view == null) {
			log.warn(getRemoteLocation(request) + " Controller " + handler.getClass().getName() + " returned view page " + viewName +", but the page does not exist.");
			response.sendError(404, "Could not find page " + viewName);
			return;
		}
		view.forward(request, response);
		if (log.isTraceEnabled()) log.trace(getRemoteLocation(request) + " doPost done for " + viewName);
	}
	
	@Override
	public void destroy() {}
	
	public static boolean isEmpty(final String s) { return (s == null || s.trim().isEmpty()); };
	
}
