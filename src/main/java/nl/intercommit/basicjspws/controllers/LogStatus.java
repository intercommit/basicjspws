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
package nl.intercommit.basicjspws.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static nl.intercommit.basicjspws.ControllerUtil.*;

import nl.intercommit.basicjspws.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.ViewStatusMessagesServlet;

/**
 * Calls logback's {@link ViewStatusMessagesServlet} to show logback's events.
 * @author FWiers
 *
 */
public class LogStatus implements Controller {
	
	private static final Logger log = LoggerFactory.getLogger(LogStatus.class);

	ViewStatusMessagesServlet logServlet;
	
	@Override
	public String getName() { return "logStatusPageUrl"; }

	public LogStatus() {
		super();
		this.logServlet = new ViewStatusMessagesServlet();
		try {
			logServlet.init();
		} catch (Exception e) {
			log.warn("Could not intiailize log status servlet.", e);
			logServlet = null;
		}
	}
	
	@Override
	public String handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
		
		if (logServlet == null) {
			sendError(response, 500, ViewStatusMessagesServlet.class.getName() + " unavailable");
			return null;
		}
		try {
			logServlet.service(request, response);
		} catch (Exception e) {
			log.error("Failed to show log status information.", e);
		}
		return null;
	}

}
