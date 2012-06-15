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
import java.util.Collections;

/**
 * Static methods dealing with propery values from the system (environment).
 * @author fwiers
 *
 */
public class SysPropsUtil {
	
	private SysPropsUtil() {}

	public static final String lf = System.getProperty("line.separator");

	/**
	 * Shows system properties (from {@link System#getProperties()}) and system environment properties (from {@link System#getenv()}) in log.
	 * @param log The log to log the properties to in one log-statement.
	 * @param includeEnv Include system environment properties or not.
	 * @param debug log as debug statement or, if false, log as info-statement.
	 */
	public static void logSysProps(final org.slf4j.Logger log, final boolean includeEnv, final boolean debug) {

		if (debug && !log.isDebugEnabled()) return;
		if (includeEnv) {
			if (debug) {
				log.debug("System environment properties: {}", getSystemEnv());
			} else {
				log.info("System environment properties: {}", getSystemEnv());
			}
		}
		if (debug) {
			log.debug("System properties: {}", getSystemProps());
		} else {
			log.info("System properties: {}", getSystemProps());
		}
	}

	public static String getSystemEnv() {

		ArrayList<String> sysKeys = new ArrayList<String>(); 
		for (final Object k : System.getenv().keySet()) {
			sysKeys.add(k.toString());
		}
		Collections.sort(sysKeys);
		StringBuilder sb = new StringBuilder();
		for (String k : sysKeys) {
			sb.append(lf).append(k).append(": ").append(System.getenv(k));
		}
		return sb.toString();
	}

	public static String getSystemProps() {

		ArrayList<String> sysKeys = new ArrayList<String>(); 
		for (Object k : System.getProperties().keySet()) {
			sysKeys.add(k.toString());
		}
		Collections.sort(sysKeys);
		StringBuilder sb = new StringBuilder();
		for (String k : sysKeys) {
			sb.append(lf).append(getPropKey(k))
			.append("=")
			.append(getPropValue(System.getProperty(k)));
		}
		return sb.toString();
	}

	/** Converts a key-name to a property key-name */ 
	public static String getPropKey(String key) {
		return convertToProp(key, true, true);
	}
	/** Converts a value to a property value */ 
	public static String getPropValue(String key) {
		return convertToProp(key, false, true);
	}
	/**
	 * Converts key/prop-value strings to Properties-format.
	 * <br>Copied from java.util.Properties.java. 
	 * Converts unicodes to encoded &#92;uxxxx and escapes
	 * special characters with a preceding slash
	 * @param theString A key or prop-value to convert.
	 * @param escapeSpace Should be true for a key, false for a property value.
	 * @param escapeUnicode Should be true by default.
	 */
	public static String convertToProp(String theString, boolean escapeSpace, boolean escapeUnicode) {
		
		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuilder outBuffer = new StringBuilder(bufLen);

		for(int x=0; x<len; x++) {
			char aChar = theString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127)) {
				if (aChar == '\\') {
					outBuffer.append('\\'); outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch(aChar) {
			case ' ':
				if (x == 0 || escapeSpace)
					outBuffer.append('\\');
				outBuffer.append(' ');
				break;
			case '\t':outBuffer.append('\\'); outBuffer.append('t');
			break;
			case '\n':outBuffer.append('\\'); outBuffer.append('n');
			break;
			case '\r':outBuffer.append('\\'); outBuffer.append('r');
			break;
			case '\f':outBuffer.append('\\'); outBuffer.append('f');
			break;
			case '=': // Fall through
			case ':': // Fall through
			case '#': // Fall through
			case '!':
				outBuffer.append('\\'); outBuffer.append(aChar);
				break;
			default:
				if (escapeUnicode && ((aChar < 0x0020) || (aChar > 0x007e))) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >>  8) & 0xF));
					outBuffer.append(toHex((aChar >>  4) & 0xF));
					outBuffer.append(toHex( aChar        & 0xF));
				} else {
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}

	/**
	 * Convert a nibble to a hex character
	 * <br>Copied from java.util.Properties.java. 
	 * @param   nibble  the nibble to convert.
	 */
	public static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	/** A table of hex digits 
	 * <br>Copied from java.util.Properties.java. 
	 * */
	public static final char[] hexDigit = {
		'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
	};

}
