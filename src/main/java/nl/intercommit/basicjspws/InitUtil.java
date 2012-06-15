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

/**
 * Various functions used during initialization via {@link AppInit}.
 * @author FWiers
 *
 */
public class InitUtil {
	
	private InitUtil() {}

	/** Removes any surrounding quotes and ensures the directory ends with a seperator. */ 
	public static String getDirParam(final String s) {
		return (isEmpty(s) ? s : endWithSep(stripQuotes(s)));
	}

	/** If s does not end with a \\ or a /, appends the  {@link File#separator} to s. */
	public static String endWithSep(final String s) {
		if (s.endsWith("\\") || s.endsWith("/")) return s;
		return s + File.separator;
	}

	/** Removes ' or " quotes when they are found at the beginning and ending of s. */
	public static String stripQuotes(String s) {
		
		if (isEmpty(s) || s.length() == 1) return s;
		if ((s.charAt(0) == '\"' || s.charAt(0) == '\'')
				&& (s.charAt(s.length()-1) == '\"' || s.charAt(s.length()-1) == '\'')) {
			if (s.length() == 2) {
				s = "";
			} else {
				s = s.substring(1, s.length()-1);
			}
		}
		return s;
	}

	/** Returns true if s is null or empty after trimming. */
	public static boolean isEmpty(final String s) { return (s == null || s.trim().isEmpty()); }
}
