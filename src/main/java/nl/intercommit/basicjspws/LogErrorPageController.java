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

import static nl.intercommit.basicjspws.ControllerUtil.getAppPage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.CyclicBufferAppender;

/**
 * Shows log-statements from the "CYCLICERROR" log-buffer.
 * Uses {@link UrlController#LOG_ERROR_PAGE}
 * @author FWiers
 *
 */
public class LogErrorPageController implements Controller {
	
	private static final Logger log = LoggerFactory.getLogger(LogErrorPageController.class);

	PatternLayout logLayout;
	CyclicBufferAppender<ILoggingEvent> logBuffer;

	@Override
	public String handleRequest(HttpServletRequest request,	HttpServletResponse response) {
		
		request.setAttribute(PAGE_TITLE, AppInit.appInstance.appName + " Log error");
		request.setAttribute("logText", "");
		logBuffer = LogbackUtil.getLogBuffer("CYCLICERROR");
		if (logBuffer == null) {
			request.setAttribute("logTextInfo", "Log error buffer is not available (please check log configuration for CYCLICERROR appender).");
		} else if (logBuffer.getLength() == 0) {
			request.setAttribute("logTextInfo", "No error log events available, log buffer is empty.");
		} else {
			// logLayout cannot be stored: it no longer works when the log configuration file is updated. 
			logLayout = new PatternLayout();
			logLayout.setContext(LogbackUtil.getLoggerContext());
			logLayout.setPattern("%d [%thread] %-5level %logger - %msg%n");
			logLayout.start();

			int maxEvents = logBuffer.getLength(); 
			request.setAttribute("logTextInfo", logLayout.doLayout(
					LogPageController.createLoggingEvent("Showing " + maxEvents + " log error events, last event first.")));
			StringBuilder sb = new StringBuilder();
			LoggingEvent le;
			for (int i = maxEvents-1; i >= 0; i--) {
				le = (LoggingEvent) logBuffer.get(i);
				String line = logLayout.doLayout(le); 
				sb.append(line);
			}
			request.setAttribute("logText", sb.toString());
			log.debug("Returning {} log error events as text", maxEvents);
			logLayout.stop();
		}
		return getAppPage(UrlController.LOG_ERROR_PAGE);
	}
	
}
