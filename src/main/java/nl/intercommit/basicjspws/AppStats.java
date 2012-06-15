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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of statistical data for the web-service instance.
 * Most data is supplied by the {@link AppFilter}.
 * @author frederikw
 *
 */
public class AppStats {

	private static final Logger log = LoggerFactory.getLogger(AppStats.class);

	private final ConcurrentHashMap<String, AtomicLong> requestCountByUrl = new ConcurrentHashMap<String, AtomicLong>();
	private final ConcurrentHashMap<String, AtomicLong> sessionCountByHost = new ConcurrentHashMap<String, AtomicLong>();
	private final long startTime = new Date().getTime();

	public long getStartTime() { return startTime;} 
	
	public long incRequest(final String requestUrl) {
		
		if (isEmpty(requestUrl)) {
			log.warn("Cannot reqister a request count for an empty request URL.");
			return 0L;
		}
		AtomicLong c = requestCountByUrl.get(requestUrl);
		if (c == null) c = new AtomicLong();
		long r = c.incrementAndGet();
		if (r == 1L) requestCountByUrl.put(requestUrl, c);
		return r;
	}
	
	public long getRequestCountTotal() {
		
		long total = 0L;
		for (String s : getRequestCountUrls()) total += requestCountByUrl.get(s).get();
		return total;
	}
	
	public List<String> getRequestCountUrls() {
		Set<String> keys = requestCountByUrl.keySet();
		return (keys == null ? new ArrayList<String>() : Arrays.asList(keys.toArray(new String[0])));
	}
	
	public long getRequestCount(final String requestUrl) { return (requestCountByUrl.get(requestUrl) == null ? 0L : requestCountByUrl.get(requestUrl).get()); }
	
	public long incSession(final String hostIp) {
		
		AtomicLong c = sessionCountByHost.get(hostIp);
		if (c == null) c = new AtomicLong();
		long r = c.incrementAndGet();
		if (r == 1L) sessionCountByHost.put(hostIp, c);
		return r;
	}
	
	public long getSessionCountTotal() {
		
		long total = 0L;
		for (String s : getSessionCountIps()) total += sessionCountByHost.get(s).get();
		return total;
	}
	
	public List<String> getSessionCountIps() {
		Set<String> keys = sessionCountByHost.keySet();
		return (keys == null ? new ArrayList<String>() : Arrays.asList(keys.toArray(new String[0])));
	}
	
	public long getSessionCount(final String hostIp) { return (sessionCountByHost.get(hostIp) == null ? 0L : sessionCountByHost.get(hostIp).get()); }

	public String getStatsDescription() {
		
		StringBuilder sb = new StringBuilder("Started on " + new Date(getStartTime()));
		sb.append("\n\nRequest-counts by URL:");
		for(String s : getRequestCountUrls()) {
			sb.append('\n').append(s).append('\t').append(": ").append(requestCountByUrl.get(s));
		}
		sb.append("\n\nTotal requests: ").append(getRequestCountTotal());
		sb.append("\n\nSession-counts by remote host:");
		for(String s : getSessionCountIps()) {
			sb.append('\n').append(s).append('\t').append(": ").append(sessionCountByHost.get(s));
		}
		sb.append("\n\nTotal sessions: ").append(getSessionCountTotal()).append("\n");
		return sb.toString();
	}
	
	public static boolean isEmpty(final Object o) { return (o == null || o.toString().trim().isEmpty()); };
	public static boolean isEmpty(final String s) { return (s == null || s.trim().isEmpty()); };
}
