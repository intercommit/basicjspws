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

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a list of all registered {@link UrlController}s.
 * This class is not thread-safe when the list is modified.
 * 
 * @author FWiers
 *
 */
public class UrlControllers {

	/** {@link UrlController}s by {@link UrlController#requestUrl} for fast lookup by {@link AppServlet}. */ 
	public Map<String, UrlController> ucByRequestUrl = new HashMap<String, UrlController>();
	/** {@link UrlController}s by {@link UrlController#name}. */
	public Map<String, UrlController> ucs = new HashMap<String, UrlController>(); 
	
	/**
	 * Adds uc to {@link #ucs}
	 * Updates {@link #ucByRequestUrl} if {@link UrlController#requestUrl} is not null.
	 * @param uc
	 */
	public void add(UrlController uc) {
		
		ucs.put(uc.name, uc);
		if (uc.requestUrl != null) {
			ucByRequestUrl.put(uc.requestUrl, uc);
		}
	}
	
	/** Removes the UrlController with name from the maps. */
	public boolean remove(String name) {
		
		UrlController uc = ucs.get(name);
		if (uc == null) return false;
		if (uc.requestUrl != null) {
			ucByRequestUrl.remove(uc.requestUrl);
		}
		return (ucs.remove(name) != null);
	}
	
	/** @return the UrlController with the name */
	public UrlController get(String name) { return ucs.get(name); }

	/** @return the UrlController with the requestUrl. */
	public Controller getByRequestUrl(String requestUrl) {
		
		UrlController uc = ucByRequestUrl.get(requestUrl);
		return (uc == null ? null : uc.controller);
	}
	/** @return the {@link UrlController#jspFileName} from the UrlController with name. */
	public String getJspPage(String name) {
		
		UrlController uc = ucs.get(name);
		return (uc == null ? null : uc.jspFileName);
	}

}
