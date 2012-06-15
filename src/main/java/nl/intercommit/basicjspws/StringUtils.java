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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains methods that help prevent synchronization bottle-necks when dealing with Charsets, 
 * <br>see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6790402 
 * <br>Solution copied form  
 * <br>http://bazaar.launchpad.net/~mark-mysql/connectorj/5.1/view/1063/src/com/mysql/jdbc/StringUtils.java
 * @author frederikw
 *
 */
public class StringUtils {

	protected static final ConcurrentHashMap<String, Charset> charsetsByAlias = 
			new ConcurrentHashMap<String, Charset>();

	public static final String platformEncoding = System.getProperty("file.encoding");

	public static Charset findCharset(final String alias) throws UnsupportedEncodingException {
		
		try {
			Charset cs = charsetsByAlias.get(alias);
			if (cs == null) {
				cs = Charset.forName(alias);
				charsetsByAlias.putIfAbsent(alias, cs);
			}
			return cs;
			// We re-throw these runtimes for compatibility with java.io
		} catch (UnsupportedCharsetException uce) {
			throw new UnsupportedEncodingException(alias);
		} catch (IllegalCharsetNameException icne) {
			throw new UnsupportedEncodingException(alias);
		} catch (IllegalArgumentException iae) {
			throw new UnsupportedEncodingException(alias);
		}
	}

	// FrederikW: comments from MySQL source:
	// The following methods all exist because of the Java bug
	// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6790402
	// which has been observed by users and reported as MySQL Bug#61105
	// We can turn around and replace them with their java.lang.String
	// equivalents if/when that bug is ever fixed.

	public static String toString(final byte[] value, final int offset, final int length,
			final String encoding) throws UnsupportedEncodingException {

		Charset cs = findCharset(encoding);
		return cs.decode(ByteBuffer.wrap(value, offset, length)).toString();
	}

	public static String toString(final byte[] value, final String encoding)
			throws UnsupportedEncodingException {

		Charset cs = findCharset(encoding);
		return cs.decode(ByteBuffer.wrap(value)).toString();
	}

	public static String toString(final byte[] value, final int offset, final int length) {

		try {
			Charset cs = findCharset(platformEncoding);
			return cs.decode(ByteBuffer.wrap(value, offset, length)).toString();
		} catch (UnsupportedEncodingException e) {
			// can't happen, emulating new String(byte[])
		}
		return null;
	}

	public static String toString(final byte[] value) {
		
		try {
			Charset cs = findCharset(platformEncoding);
			return cs.decode(ByteBuffer.wrap(value)).toString();
		} catch (UnsupportedEncodingException e) {
			// can't happen, emulating new String(byte[])
		}
		return null;
	}

	public static byte[] getBytes(final String value, final String encoding)
			throws UnsupportedEncodingException {

		Charset cs = findCharset(encoding);
		// can't simply .array() this to get the bytes
		// especially with variable-length charsets the 
		// buffer is sometimes larger than the actual encoded data
		ByteBuffer buf = cs.encode(value);
		int encodedLen = buf.limit();
		byte[] asBytes = new byte[encodedLen];
		buf.get(asBytes, 0, encodedLen);
		return asBytes;
	}

	public static byte[] getBytes(final String value) {
		
		try {
			Charset cs = findCharset(platformEncoding);
			ByteBuffer buf = cs.encode(value);
			int encodedLen = buf.limit();
			byte[] asBytes = new byte[encodedLen];
			buf.get(asBytes, 0, encodedLen);
			return asBytes;
		} catch (UnsupportedEncodingException e) {
			// can't happen, emulating new String(byte[])
		}
		return null;
	}
	
	/**
	 * Surround a given string with a 'border' string, useful when constructing 'like' queries.
	 * Examples:
	 * <br> surround("Test","%") --&gt %Test%
	 * <br> surround("Test%","%") --&gt %Test%
	 * <br> surround("","%") --&gt %%
	 * 
	 */
	public static String surround(final String s, final String border) {
		
		String result = (s == null ? "" : s);
		if (!s.startsWith(border)) {
			result = border + result;
		}
		if (!s.endsWith(border)) {
			result = result + border;
		}
		return result;
	}
	
	/** 
	 * If string is surrounded by character c (e.g. '"'), char c is removed from beginning and end of string. Example:
	 * <br> "Test"/" --&gt Test
	 * <br> "Test/" --&gt "Test
	 * <br> "/" --&gt "
	 * <br> ""/" --&gt <emptry string>
	 * @param s The string to trim
	 * @param c The character to strip from s.
	 * @return The trimmed string. If s was null, null is returned. 
	 * Can return an empty string if string has length 2 and only contains char c.
	 */
	public static String trim(final String s, final char c) {
		
		String newString;
		if (s != null && s.length() > 1 && s.charAt(0) == c && s.charAt(s.length()-1) == c) {
			newString = s.substring(1, s.length() -1);
		} else {
			newString = s;
		}
		return newString;
	}	

}
