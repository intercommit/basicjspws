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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static utility functions that can be used in a {@link Controller}.
 * Many of these functions use variables set in {@link AppInit} (retrieved via {@link AppInit#appInstance}).
 * Use 
 * <br><code>import static nl.intercommit.basicjspws.ControllerUtil.*;</code>
 * <br>to get easy access to the functions in a {@link Controller}. 
 * @author FWiers
 *
 */
public class ControllerUtil {

	private static final Logger log = LoggerFactory.getLogger(ControllerUtil.class);

	private ControllerUtil() {}
	
	/**
	 * Description of remote location between []. If used in log-statements, 
	 * it is best to cache this value in a local String. 
	 * @return [RemoteIP:port] from request.
	 */
	public static String getRemoteLocation(final HttpServletRequest request) {

		return "[" + request.getRemoteAddr() + ":" + request.getRemotePort() + "]";
	}
	
	/**
	 * Logs in debug-mode the headers, parameters and query-string of the request. 
	 * @param log Logger to use.
	 * @param request The request to log details about.
	 */
	public static void logRequestDetails(final Logger log, final HttpServletRequest request) {
		
		if (!log.isDebugEnabled()) return;
		StringBuilder sb = new StringBuilder(128);
		sb.append(getRemoteLocation(request)).append(" ");
		sb.append(request.getMethod()).append(" request details:");
		@SuppressWarnings("unchecked")
		Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames == null || !headerNames.hasMoreElements()) {
			sb.append("\nNo headers.");
		} else {
			while (headerNames.hasMoreElements()) {
				String hname = headerNames.nextElement();
				String hvalue = request.getHeader(hname);
				if (hvalue != null) {
					sb.append("\nHeader ").append(hname).append(": ").append(hvalue);
				}
			}
		}
		@SuppressWarnings("unchecked")
		Map<String, String[]> params = request.getParameterMap();
		int numberOfParams = params.keySet().size(); 
		if (numberOfParams == 0) {
			sb.append("\nNo parameters.");
		} else { 
			for(String paramName: params.keySet()) {
				sb.append("\nParam ").append(paramName).append(": ").append(Arrays.toString(params.get(paramName)));
			}
		}
		if (request.getQueryString() == null) {
			sb.append("\nNo query string.");
		} else {
			String q = request.getQueryString();
			try { q = URLDecoder.decode(request.getQueryString(), "UTF-8"); } catch (Exception ignored) {}
			sb.append("\nQuery string:\n").append(q);
		}
		log.debug(sb.toString());
	}
	
	/**
	 * Tries to read the contents from the Reader of the request.
	 * @param request the (post) request.
	 * @return null (failed to get any contents) or an non-empty String.
	 */
	public static String getRequestContent(final HttpServletRequest request) {
		
		boolean haveSomething = false;
		final StringBuilder sb = new StringBuilder("");
		BufferedReader r = null;
		try {
			r = request.getReader();
			char[] cbuf = new char[1024];
			int read = 0;
			while ((read = r.read(cbuf)) > 0) {
				haveSomething = true;
				sb.append(cbuf, 0, read);
			}
		} catch (Exception e) {
			log.warn("Could not read text from request: " + e);
			haveSomething = false;
		} finally {
			FileUtil.close(r);
		}
		return (haveSomething ? sb.toString() : null);
	}
	
	/** Use this method instead of request.getSession() so that statistics are updated. */
	public static HttpSession getSession(final HttpServletRequest request) {

		if (request.getSession(false) == null) {
			//String lang = request.getLocale().getLanguage();
			String rhost = request.getRemoteHost();
			long sessionNumber = ControllerUtil.getAppStats().incSession(rhost);
			if (log.isDebugEnabled()) { 
				log.debug("New session {} created for {}:{}", 
						new String[] { Long.toString(sessionNumber), rhost, Integer.toString(request.getRemotePort()) });
			}
		}
		return request.getSession();
	}
	
	/** Returns null or a trimmed, non-empty value for the parameter. */
	public static String getParamTrimmed(final HttpServletRequest request, final String paramName) {
		
		String v = request.getParameter(paramName);
		if (v != null) v = v.trim();
		return (v == null || v.isEmpty() ? null : v);
	}
	
	/** 
	 * Sends the errorCode with the erroMsg.
	 * @return always null.
	 */
	public static String sendError(final HttpServletResponse response, final int errorCode, final String errorMsg) {
		try {
			response.sendError(errorCode, errorMsg);
		} catch (Exception e) {
			log.warn("Could not send error response \"" + errorCode + ": " + errorMsg + "\" (" + e + ")");
		}
		return null;
	}

	/**
	 * Sends a redirect.
	 * @return always null.
	 */
	public static String sendRedirect(final HttpServletResponse response, final String location) {
		try {
			response.sendRedirect(location);
		} catch (Exception e) {
			log.warn("Could not redirect to location " + location + "(" + e + ")");
		}
		return null;
	}

	/** Same as {@link #writeResponse(HttpServletResponse, String, String, String)} 
	 * but uses the default encoding for character set.
	 */
	public static String writeResponse(final HttpServletResponse response, final String contentType, final String output) {
		return writeResponse(response, contentType, getDefaultEncoding(), output);
	}
	/**
	 * Sends output to the client. Commits the response (no further writing possible).
	 * @param response may not already be committed.
	 * @param contentType Mandatory e.g. text/plain text/html text/xml
	 * @param encoding Mandatory e.g. UTF-8 or ISO-8859-1 (the default)
	 * @param output The text to send as output.
	 * @return always null.
	 */
	public static String writeResponse(final HttpServletResponse response, final String contentType, final String encoding, final String output) {
		
		// Must call setContentType before setCharacterEncoding, else latter has no effect.
		try {
			response.setContentType(contentType);
			response.setCharacterEncoding(encoding);
			PrintWriter pw = response.getWriter();
			pw.write(output);
			pw.flush();
		} catch (Exception e) {
			log.warn("Could not write text-response: " + e);
		}
		return null;
	}

	/** Calls {@link #writeResponse(HttpServletResponse, String, String, InputStreamReader, boolean)} 
	 * but uses the default encoding for character set (see {@link #getDefaultEncoding()}).
	 */
	public static String writeResponse(final HttpServletResponse response, final String contentType, 
			final InputStreamReader reader, final boolean closeReader) {
		return writeResponse(response, contentType, getDefaultEncoding(), reader, closeReader);
	}

	/** Same as {@link #writeResponse(HttpServletResponse, String, String, String)} 
	 * but reads the text to send from the given InputStreamReader.
	 * Commits the response (no further writing possible).
	 * @param closeReader If true, given reader is always closed.
	 */
	public static String writeResponse(final HttpServletResponse response, final String contentType, final String encoding, 
			final InputStreamReader reader, final boolean closeReader) {
		
		// Must call setContentType before setCharacterEncoding, else latter has no effect.
		try {
			response.setContentType(contentType);
			response.setCharacterEncoding(encoding);
			PrintWriter pw = response.getWriter();
			char[] cbuf = new char[8192];
			int len = 0;
			while ((len = reader.read(cbuf)) > 0) {
				pw.write(cbuf, 0, len);
			}
			pw.flush();
		} catch (Exception e) {
			log.warn("Could not write text-response from reader: " + e);
		} finally {
			if (closeReader) FileUtil.close(reader);
		}
		return null;
	}

	/** Calls {@link #writeResponse(HttpServletResponse, String, String, InputStream, boolean)}
	 *  with a general content type for bytes (causes download windows to appear in browser). */
	public static String writeResponse(final HttpServletResponse response, final String fileName, 
			final InputStream in, final boolean closeIn) {
		return writeResponse(response, "application/octet-stream", fileName, in, closeIn);
	}

	/** 
	 * Sends a file in bytes back to the client. Commits the response (no further writing possible).  
	 * General content-type is "application/octet-stream", but for example for pdf use "application/pdf". 
	 * @param closeIn If true, given input-stream is always closed.
	 * @return always null.
	 */
	public static String  writeResponse(final HttpServletResponse response, final String contentType, 
			final String fileName, final InputStream in, final boolean closeIn) {
		
		OutputStream out = null;
		try {
			response.setContentType(contentType);
			response.setHeader("Content-Disposition","attachment;filename="+fileName);
			out = response.getOutputStream();
			FileUtil.copyStreams(in, out);
			out.flush();
		} catch (Exception e) {
			log.warn("Could not write byte-response from inputstream: " + e);
		} finally {
			if (closeIn) FileUtil.close(in);
		}
		return null;
	}
	
	/** Gets a session and then the session's servlet context. */
	public static ServletContext getServletContext(final HttpServletRequest request) {
		return getSession(request).getServletContext();
	}
	
	/**
	 * @return The properties from {@link AppInit}
	 */
	public static Properties getAppProps() {
		return AppInit.appInstance.appProps;
	}
	
	/**
	 * @return The default encoding alias from {@link AppInit}
	 */
	public static String getDefaultEncoding() {
		return AppInit.appInstance.defaultEncoding;
	}

	/**
	 * @return The AppStats from {@link AppInit}
	 */
	public static AppStats getAppStats() {
		return AppInit.appInstance.appStats;
	}

	/**
	 * @return The "requestedUrl" attribute set by the MainFilter.
	 */
	public static String getRequestedUrl(final HttpServletRequest request) {
		return (String)request.getAttribute("requestedUrl");
	}
	
	/**
	 * @return True if o is null or o as string is empty after trimming.
	 */
	public static boolean isEmpty(final Object o) { return (o == null || o.toString().trim().isEmpty()); };

	/**
	 * @return true if s is null or empty after trimming.
	 */
	public static boolean isEmpty(final String s) { return (s == null || s.trim().isEmpty()); };
}
