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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.CyclicBufferAppender;

/**
* Shows log-statements from the "CYCLIC" log-buffer.
* @author FWiers
*
*/
public class LogPageController implements Controller {
	
	private static final Logger log = LoggerFactory.getLogger(LogPageController.class);

	PatternLayout logLayout;
	CyclicBufferAppender<ILoggingEvent> logBuffer;
	
	@Override
	public String handleRequest(HttpServletRequest request,	HttpServletResponse response) {
		
		request.setAttribute(PAGE_TITLE, AppInit.appInstance.appName + " Log");
		request.setAttribute("logText", "");
		logBuffer = LogbackUtil.getLogBuffer("CYCLIC");
		if (logBuffer == null) {
			request.setAttribute("logTextInfo", "Log buffer is not available (please check log configuration for CYCLIC appender).");
		} else if (logBuffer.getLength() == 0) {
			request.setAttribute("logTextInfo", "No log events available, log buffer is empty.");
		} else {
			// logLayout cannot be stored: it no longer works when the log configuration file is updated. 
			logLayout = new PatternLayout();
			logLayout.setContext(LogbackUtil.getLoggerContext());
			logLayout.setPattern("%d{dd/MM HH:mm:ss:SSS} %-5level %logger{35} - %msg%n");
			logLayout.start();

			int maxEvents = logBuffer.getLength(); 
			request.setAttribute("logTextInfo", logLayout.doLayout(
					createLoggingEvent("Showing " + maxEvents + " log events, last event first.")));
			StringBuilder sb = new StringBuilder();
			LoggingEvent le;
			for (int i = maxEvents-1; i >= 0; i--) {
				le = (LoggingEvent) logBuffer.get(i);
				String line = logLayout.doLayout(le); 
				sb.append(line);
			}
			request.setAttribute("logText", sb.toString());
			log.debug("Returning {} log events as text", maxEvents);
			logLayout.stop();
		}
		return getAppPage(UrlController.LOG_PAGE);
	}
	
	public static LoggingEvent createLoggingEvent(String msg) {
		
		LoggingEvent le = new LoggingEvent();
		le.setTimeStamp(System.currentTimeMillis());
		le.setLevel(Level.INFO);
		le.setThreadName(Thread.currentThread().getName());
		le.setLoggerName(LogPageController.class.getName());
		le.setMessage(msg);
		return le;
	}

}
