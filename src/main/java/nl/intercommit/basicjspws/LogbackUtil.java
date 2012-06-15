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

import java.io.File;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.CyclicBufferAppender;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Sets up logging for development-environment and production environment.
 * <br>If the system property "-Dlogback.configurationFile=programmatic" is set,
 * logging is setup programmatically via {@link #setLoggingProgrammatic()}
 * and log-statements are send to console.
 * <br>For production, looks for a file named "baseName-logback.xml" in Tomcat conf-directory and loads
 * logging configuration from there.
 * @author FWiers
 *
 */
public class LogbackUtil {
	
	private LogbackUtil() {}

	/**
	 * Sets logging configuration. Uses system property "logback.configurationFile" first if it exists.
	 * @param appHomeDir Home-dir of Tomcat or development configuration directory (see {@link AppInit#setHomeDir()}).
	 * @param logBackConfigFileName "programmatic" for development, else the name of the log-file in the form baseName-logback.xml
	 */
	public static void initLogging(final String appHomeDir, final String logBackConfigFileName) {
		// Setup logging, see also http://logback.qos.ch/manual/configuration.html

		String fname = System.getProperty("logback.configurationFile");
		if (!isEmpty(fname)) {
			if ("programmatic".equals(fname)) {
				setLoggingProgrammatic();
				return;
			} else {
				fname = appHomeDir + "conf" + File.separator + fname;
				if (!new File(fname).exists()) {
					fname = null;
				}
			}
		}
		if (fname == null) fname = appHomeDir + "conf" + File.separator + logBackConfigFileName;
		File configFile = new File(fname);
		if (configFile.exists()) {
			LoggerContext loggerContext = getLoggerContext();
			try {
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(loggerContext);
				//Call context.reset() to clear any previous configuration, e.g. default configuration.
				loggerContext.reset(); 
				configurator.doConfigure(configFile);
				org.slf4j.LoggerFactory.getLogger(LogbackUtil.class).info("Logging configured from " + configFile);
			} catch (JoranException je) {
				// StatusPrinter will handle this
			}
			StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
		} else {
			setLoggingProgrammatic();
		}
	}

	public static void setLoggingProgrammatic() {
		// Code copied from http://logback.qos.ch/xref/chapters/layouts/PatternSample.html

		// assume SLF4J is bound to logback in the current environment
		LoggerContext loggerContext = getLoggerContext();
		//Call context.reset() to clear any previous configuration, e.g. default configuration.
		loggerContext.reset();
		Logger rootLogger = getRootLogger();
		
		rootLogger.setLevel(Level.DEBUG);
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{35} - %msg%n");
		encoder.setContext(loggerContext);
		encoder.start();
		ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
		appender.setContext(loggerContext);
		appender.setEncoder(encoder);
		appender.setName("CONSOLE");
		appender.start();
		rootLogger.addAppender(appender);
		
		CyclicBufferAppender<ILoggingEvent> logBuffer = new CyclicBufferAppender<ILoggingEvent>();
		logBuffer.setContext(loggerContext);
		logBuffer.setMaxSize(512);
		logBuffer.setName("CYCLIC");
		logBuffer.start();
		rootLogger.addAppender(logBuffer);
		
		CyclicBufferAppender<ILoggingEvent> logBufferError = new CyclicBufferAppender<ILoggingEvent>();
		logBufferError.setContext(loggerContext);
		logBufferError.setMaxSize(512);
		logBufferError.setName("CYCLICERROR");
		ThresholdFilter errorFilter = new ThresholdFilter();
		errorFilter.setContext(loggerContext);
		errorFilter.setLevel("ERROR");
		errorFilter.start();
		logBufferError.addFilter(errorFilter);
		logBufferError.start();
		rootLogger.addAppender(logBufferError);
		
		org.slf4j.LoggerFactory.getLogger(LogbackUtil.class).info("Logging configured programmatically, printing log-messages to console.");
	}

	public static LoggerContext getLoggerContext() {
		//Logger rootLogger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//return rootLogger.getLoggerContext();

		// assume SLF4J is bound to logback in the current environment
		return (LoggerContext) LoggerFactory.getILoggerFactory();
	}

	public static Logger getRootLogger() {
		return getLoggerContext().getLogger(Logger.ROOT_LOGGER_NAME);
	}

	public static CyclicBufferAppender<ILoggingEvent> getLogBuffer(String name) {
		return (CyclicBufferAppender<ILoggingEvent>) getRootLogger().getAppender(name);
	}
	
	public static boolean isEmpty(final String s) { return (s == null || s.trim().isEmpty()); }

}
