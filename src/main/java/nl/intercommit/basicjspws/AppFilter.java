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
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple filter that sets the "requestedUrl" as attribute and updates the statistics.
 * @author FWiers
 *
 */
public class AppFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(AppFilter.class);

	@Override
	public void destroy() {}

	@Override
	public void init(final FilterConfig fc) throws ServletException {}

	/**
	 * Determines the requestURL (registered as attribute "requestedUrl")
	 * and updated the hit-count for the requestedUrl.
	 */
	@Override
	public void doFilter(final ServletRequest filterRequest, final ServletResponse filterResponse,
			final FilterChain chain) throws IOException, ServletException {

		final HttpServletRequest request = (HttpServletRequest) filterRequest;
		//HttpServletResponse response = (HttpServletResponse) filterResponse;
		String requestedUrl = new URL(request.getRequestURL().toString()).getPath();
		log.debug("Filtering for {}", requestedUrl);
		request.setAttribute("requestedUrl", requestedUrl);
		ControllerUtil.getAppStats().incRequest(requestedUrl);
		chain.doFilter(filterRequest, filterResponse);
	}

}
