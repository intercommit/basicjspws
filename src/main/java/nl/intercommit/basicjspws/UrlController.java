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

/**
 * Often a URL (e.g. /pages/index) is related to a controller
 * (e.g. {@link IndexController}) and that controller often
 * uses a jsp-page to show a result (e.g. "/WEB-INF/pages/index.jsp").
 * This class relates all these resources using {@link #name} as key.
 * A new urlController should be registered in {@link AppInit#setUrlControllers(String)}
 * which uses {@link UrlControllers} to keep an overview for lookup in main classes
 * like {@link AppServlet}.
 * @author FWiers
 *
 */
public class UrlController {

	/** The root-Url of this application. */
	public static final String BASE_URL = "appBaseUrl";
	/** The images directory, for use in jsp-pages. */
	public static final String IMAGES_URL = "appImagesUrl";
	public static final String INDEX_PAGE = "indexPageUrl";
	public static final String STATS_PAGE = "statsPageUrl";
	public static final String SYSENV_PAGE = "sysEnvPageUrl";
	public static final String LOG_PAGE = "logPageUrl";
	public static final String LOG_ERROR_PAGE = "logErrorPageUrl";
	public static final String LOG_STATUS_PAGE = "logStatusPageUrl";
	
	/** 
	 * The location of the jsp-file (if any), e.g. "/WEB-INF/pages/index.jsp"
	 */
	public String jspFileName;
	/**
	 * The name/key (registered in ServletContext) for this UrlController.
	 */
	public String name;
	/**
	 * The requestUrl associated with the {@link #controller} (used by {@link AppServlet})
	 */
	public String requestUrl;
	/**
	 * An instance of a controller that will handle all (concurrent) request from {@link #requestUrl}.
	 */
	public Controller controller;

	public UrlController(String name, String requestUrl, Controller controller, String jspFileName) {
		super();
		this.name = name;
		this.requestUrl = requestUrl;
		this.controller = controller;
		this.jspFileName = jspFileName;
	}
	
	/** @return simpleClassName-{@link #name}. */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "-" + name;
	}
	
}
