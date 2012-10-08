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

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
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
	/** The name of the first part in the URL referring to this application (e.g. localhost:8080/baseName/) */
	public String baseName;
	/** The the first part in the URL referring to this application
	 * surrounded by forward slashes  (e.g. localhost:8080&lt;baseUrl&gt;). */
	public String baseUrl;
	/** Properties loaded during startup. */
	public Properties appProps;
	/** The home-directory of this application (usually home-directory of Tomcat). */
	public String appHomeDir;
	/** Statistics gathered during the life-time of this application. Shown on the /pages/stats. */
	public AppStats appStats;
	/** The default encoding used to send responses. Used by {@link ControllerUtil}. */
	public String defaultEncoding;
	/** The servlet context that this listener uses to register global webapp attributes. */ 
	public ServletContext sc;

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
	 * Initializes the application:
	 * <br> - sets {@link #appInstance}
	 * <br> - sets {@link #sc}
	 * <br> - calls {@link #setHomeDir()}
	 * <br> - registers in ServletContext appName, appBaseName, appVersion and appHomeDir
	 * <br> - calls {@link LogbackUtil#initLogging(String, String)}
	 * <br> - calls {@link SysPropsUtil#logSysProps(Logger, boolean, boolean)}
	 * <br> - sets {@link #appStats}
	 * <br> - calls {@link #getAppProps(ServletContextEvent)}
	 * <br> - calls {@link #getAppEnv()} and registers it in ServletContext via appEnv
	 * <br> - sets {@link #defaultEncoding} (default UTF-8) using {@link #appProps} baseName.default.encoding as key.
	 * <br> - sets {@link #baseUrl} (default "/baseName/") where baseUrl is constructed using {@link #appProps}'s baseName.base.url as key.
	 */
	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		
		appInstance = this;
		sc = sce.getServletContext();
		appName = getAppName();
		baseName = getBaseName();
		setHomeDir();
		sc.setAttribute("appName", appName);
		sc.setAttribute("appBaseName", baseName);
		sc.setAttribute("appVersion", getAppVersion());
		sc.setAttribute("appHomeDir", appHomeDir); 
		LogbackUtil.initLogging(appHomeDir, baseName + "-logback.xml");
		SysPropsUtil.logSysProps(log, true, true);
		appStats = new AppStats();
		appProps = getAppProps(sce);
		sc.setAttribute("appEnv", getAppEnv()); 
		try {
			defaultEncoding = appProps.getProperty(baseName + ".default.encoding", "UTF-8");
			Charset.forName(defaultEncoding);
		} catch (Exception e) {
			throw new RuntimeException("Invalid encoding for " + baseName + ": " + defaultEncoding, e);
		}
		initApp(sce);
		baseUrl = appProps.getProperty(baseName + ".base.url");
		if (isEmpty(baseUrl)) baseUrl = "/" + baseName + "/";
		if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";
		log.debug("Base URL set to " + baseUrl);
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
	 * Closes the logger (calls {@link LogbackUtil#getLoggerContext()}.stop())
	 * and sets {@link #sc} to null.
	 * Overload to shutdown additional services when application is stopped/undeployed.
	 * <br>Call {@link AppInit#clearThreadLocals(List)} if needed.
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent sce) {

		LogbackUtil.getLoggerContext().stop();
		sc = null;
	}
	
	/**
	 * Calls {@link #clearThreadLocals(List)} with the one threadLocal.
	 */
	protected void clearThreadLocal(ThreadLocal<?> threadLocal) {
		clearThreadLocals(Arrays.asList(new ThreadLocal<?>[] { threadLocal }));
	}
	
	/**
	 * Clears (nullifies) all thread-local instances from all threads.
	 * This prevents a severe log-statement from Tomcat about memory leakage through used thread-locals.
	 * Note that Tomcat 7.0.6 can clear these leaks itself, see
	 * https://issues.apache.org/bugzilla/show_bug.cgi?id=49159
	 * <br>Copied from<br>
	 * http://svn.apache.org/repos/asf/tomcat/tc7.0.x/tags/TOMCAT_7_0_8/java/org/apache/catalina/loader/WebappClassLoader.java
	 * @param threadLocals A list of static ThreadLocal variables that are used as thread-locals in this web-app.
	 */
	protected void clearThreadLocals(List<ThreadLocal<?>> threadLocals) {
		
		Thread[] threads = getThreads();
		try {
			// Make the fields in the Thread class that store ThreadLocals accessible
			Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
			threadLocalsField.setAccessible(true);
			Field inheritableThreadLocalsField = Thread.class.getDeclaredField("inheritableThreadLocals");
			inheritableThreadLocalsField.setAccessible(true);
			// Make the underlying array of ThreadLoad.ThreadLocalMap.Entry objects accessible
			Class<?> tlmClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
			Field tableField = tlmClass.getDeclaredField("table");
			tableField.setAccessible(true);

			for (int i = 0; i < threads.length; i++) {
				Object threadLocalMap;
				if (threads[i] != null) {
					// Clear the first map
					threadLocalMap = threadLocalsField.get(threads[i]);
					clearThreadLocals(threadLocals, threadLocalMap, tableField);
					// Clear the second map
					threadLocalMap = inheritableThreadLocalsField.get(threads[i]);
					clearThreadLocals(threadLocals, threadLocalMap, tableField);
				}
			}
		} catch (Exception e) {
			log.warn("Failed to clear thread-local variables.", e);
		}       
	}
	
	/**
	 * Get the set of current threads as an array.
	 */
	private Thread[] getThreads() {

		// Get the current thread group 
		ThreadGroup tg = Thread.currentThread( ).getThreadGroup( );
		// Find the root thread group
		while (tg.getParent() != null) {
			tg = tg.getParent();
		}
		int threadCountGuess = tg.activeCount() + 50;
		Thread[] threads = new Thread[threadCountGuess];
		int threadCountActual = tg.enumerate(threads);
		// Make sure we don't miss any threads
		while (threadCountActual == threadCountGuess) {
			threadCountGuess *=2;
			threads = new Thread[threadCountGuess];
			// Note tg.enumerate(Thread[]) silently ignores any threads that
			// can't fit into the array 
			threadCountActual = tg.enumerate(threads);
		}
		return threads;
	}

	private void clearThreadLocals(List<ThreadLocal<?>> threadLocals, Object map, Field internalTableField) 
			throws IllegalAccessException, NoSuchFieldException {

		if (map == null) return;
		Object[] table = (Object[]) internalTableField.get(map);
		if (table == null) return;
		int count = 0;
		for (int j =0; j < table.length; j++) {
			if (table[j] == null) continue;
			// Check the key
			Object key = ((Reference<?>) table[j]).get();
			if (threadLocals.contains(key)) {
				count++;
				table[j] = null;
			}
		}
		if (count > 0) {
			log.debug("Removed " + count + " thread-local instances.");
		}
	}

	public static boolean isEmpty(final String s) { return (s == null || s.trim().isEmpty()); }
}
