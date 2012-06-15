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

import java.nio.charset.Charset;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to setup the basic jsp framework. 
 * Initializes various variables that are often used during the life-time of the application.
 * See for details {@link #contextInitialized(ServletContextEvent)}.
 * <br>Abstract methods must return a non-null value by the implementing class.
 * The implementing class must be registered in WebContent/WEB-INF/web.xml
 * as the listener-class.
 * <br>All protected methods can be overloaded by the implementing class, call super.method() to 
 * add functionality on top of the framework or replace it.  
 * @author frederikw
 *
 */
public abstract class AppInit implements ServletContextListener {

	private static final Logger log = LoggerFactory.getLogger(AppInit.class);

	/**
	 *  Instance set during startup. This instance is used to get to variables/values set in this class.
	 *  The implementing class can also use this pattern to allow easy access to variable/values
	 *  specific to the application (unrelated to this basic jsp framework). 
	 */
	public static AppInit appInstance;
	
	/** The name if the application as it should be displayed on Web-pages. */
	public String appName;
	/** The first part in the URL referring to this application (e.g. localhost:8080/baseName/) */
	public String baseName;
	/** Properties loaded during startup. */
	public Properties appProps;
	/** The home-directory of this application (usually home-directory of Tomcat). */
	public String appHomeDir;
	/** Statistics gathered during the life-time of this application. Shown on the /pages/stats. */
	public AppStats appStats;
	/** The default encoding used to send responses. Used by {@link ControllerUtil}. */
	public String defaultEncoding;
	/** 
	 * Controllers used to handle requests. Used by the {@link AppServlet} to lookup controllers
	 * that should handle a request (looked up via {@link UrlController#requestUrl}
	 * and to lookup jsp-page files (the {@link UrlController#jspFileName} found using the {@link UrlController#name} as key).
	 */
	public UrlControllers urlControllers;

	/** The app-name is used in jsp-pages to show the name of this application. Stored in ServletContext as appName. */
	protected abstract String getAppName();
	
	/** The base-name is used to contruct the base-URL for the jsp-pages. Stored in ServletContext as appBaseName. */
	protected abstract String getBaseName();
	
	/** Returns a version description (e.g. 1.0.0). Stored in ServletContext as appVersion. 
	 * The version number should be extracted from a Manifest-files. */
	protected abstract String getAppVersion();
	
	/** A short description of the environment this application is running in (e.g. test, prod, etc.). 
	 * Used in index jsp-page (via IndexController). */
	protected abstract String getAppEnv();
	
	/** Load the properties for this application (can return empty Properties instance but not null). */
	protected abstract Properties getAppProps(final ServletContextEvent sce);
	
	/** Initialize the rest of the application. */
	protected abstract void initApp(final ServletContextEvent sce);

	/**
	 * Initializes {@link #urlControllers} and adds all UrlControllers related to the static Strings in {@link UrlController}
	 * (e.g. {@link UrlController#INDEX_PAGE}). 
	 * @param baseUrl e.g. "/baseName/" (always ends and starts with a /), registered as urlController using {@link UrlController#BASE_URL}. 
	 */
	protected void setUrlControllers(final String baseUrl) {
		
		urlControllers = new UrlControllers();
		urlControllers.add(new UrlController(UrlController.BASE_URL, baseUrl, null, null));
		urlControllers.add(new UrlController(UrlController.IMAGES_URL, baseUrl + "images", null, null));
		urlControllers.add(new UrlController(UrlController.INDEX_PAGE, baseUrl + "pages/index", null, "/WEB-INF/pages/index.jsp"));
		urlControllers.add(new UrlController(UrlController.STATS_PAGE, baseUrl + "pages/stats", null, "/WEB-INF/pages/stats.jsp"));
		urlControllers.add(new UrlController(UrlController.SYSENV_PAGE, baseUrl + "pages/sysenv", null, "/WEB-INF/pages/sysenv.jsp"));
		urlControllers.add(new UrlController(UrlController.LOG_PAGE, baseUrl + "pages/log", null, "/WEB-INF/pages/log.jsp"));
		urlControllers.add(new UrlController(UrlController.LOG_ERROR_PAGE, baseUrl + "pages/logerror", null, "/WEB-INF/pages/log.jsp"));
		urlControllers.add(new UrlController(UrlController.LOG_STATUS_PAGE, baseUrl + "pages/logstatus", null, null));
	}
	
	/** 
	 * For all urlControllers (see {@link #setUrlControllers(String)}) that have a name ending with "Url", register
	 * the requestUrl as an attribute in sce's ServletContext using the {@link UrlController#name}
	 * as key.
	 */
	protected void registerRequestUrlsInServletContext(final ServletContextEvent sce) {
		
		ServletContext sc = sce.getServletContext();
		int count = 0;
		for(UrlController uc : urlControllers.ucs.values()) {
			if (uc.name.endsWith("Url")) {
				sc.setAttribute(uc.name, uc.requestUrl);
				count++;
			}
		}
		log.debug("Total of " + count + " urls registered as attribute.");
	}

	/**
	 * Initializes the application:
	 * <br> - sets {@link #appInstance}
	 * <br> - calls {@link #setHomeDir()}
	 * <br> - registers in ServletContext appName, appBaseName, appVersion and appHomeDir
	 * <br> - calls {@link LogbackUtil#initLogging(String, String)}
	 * <br> - calls {@link SysPropsUtil#logSysProps(Logger, boolean, boolean)}
	 * <br> - sets {@link #appStats}
	 * <br> - calls {@link #getAppProps(ServletContextEvent)}
	 * <br> - calls {@link #getAppEnv()} and registers it in ServletContext via appEnv
	 * <br> - sets {@link #defaultEncoding} (default UTF-8) using {@link #appProps} baseName.default.encoding as key.
	 * <br> - calls {@link #setUrlControllers(String)} (default "/baseName/") where baseUrl is constructed using  
	 * {@link #appProps} baseName.base.url as key.
	 * <br> - calls {@link AppInit#registerRequestUrlsInServletContext(ServletContextEvent)}
	 */
	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		
		appInstance = this;
		appName = getAppName();
		baseName = getBaseName();
		setHomeDir();
		sce.getServletContext().setAttribute("appName", appName);
		sce.getServletContext().setAttribute("appBaseName", baseName);
		sce.getServletContext().setAttribute("appVersion", getAppVersion());
		sce.getServletContext().setAttribute("appHomeDir", appHomeDir); 
		LogbackUtil.initLogging(appHomeDir, baseName + "-logback.xml");
		SysPropsUtil.logSysProps(org.slf4j.LoggerFactory.getLogger(this.getClass()), true, true);
		appStats = new AppStats();
		appProps = getAppProps(sce);
		sce.getServletContext().setAttribute("appEnv", getAppEnv()); 
		try {
			defaultEncoding = appProps.getProperty(baseName + ".default.encoding", "UTF-8");
			Charset.forName(defaultEncoding);
		} catch (Exception e) {
			throw new RuntimeException("Invalid encoding for " + baseName + ": " + defaultEncoding, e);
		}
		initApp(sce);
		String baseUrl = appProps.getProperty(baseName + ".base.url");
		if (isEmpty(baseUrl)) baseUrl = "/" + baseName + "/";
		if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";
		log.debug("Base URL set to " + baseUrl);
		setUrlControllers(baseUrl);
		registerRequestUrlsInServletContext(sce);
	}
	
	/** Sets {@link #appHomeDir} (specified via system property -DbaseName.home or uses catalina/tomcat home directory).
	 * Stored in ServletContext as appHomeDir. 
	 * <br>The system property should be used when in development environment to point to your development configuration directory. */
	protected void setHomeDir() {

		String dir = System.getProperty(baseName + ".home");
		if (isEmpty(dir)) dir = System.getProperty("catalina.base");
		if (isEmpty(dir)) dir = System.getProperty("catalina.home");
		if (isEmpty(dir)) dir = System.getenv("CATALINA_BASE");
		if (isEmpty(dir)) dir = System.getenv("CATALINA_HOME");
		if (isEmpty(dir)) dir = System.getProperty("user.dir");
		appHomeDir = InitUtil.getDirParam(dir);
	}
	
	/**
	 * Closes the logger (calls {@link LogbackUtil#getLoggerContext()}.stop()).
	 * Overload to shutdown additional services when application is stopped/undeployed.
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent sce) {

		LogbackUtil.getLoggerContext().stop();
	}
	
	public static boolean isEmpty(final String s) { return (s == null || s.trim().isEmpty()); }
}
