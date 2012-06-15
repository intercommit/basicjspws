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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A utility class for handling files. */
public final class FileUtil {
	
	private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

	/** Copies all bytes from inputstream to outputstream. Does NOT close the streams. */
	public static final void copyStreams(final InputStream in, final OutputStream out) throws IOException {
		copyStreams(in , out, new byte[16384]);
	}
	
	/** Copies all bytes from inputstream to outputstream using the provided buffer (must have size > 0). 
	 * Use this when many copy-operations are done in a thread-safe manner to save memory. Does NOT close the streams. */
	public static final void copyStreams(final InputStream in, final OutputStream out, final byte[] buf) throws IOException {

		// Transfer bytes from in to out
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}

	/**
	 * Closes the closable if it is not null.
	 * Logs a warning when an error occurs.
	 */
	public static void close(final Closeable closable) {
		if (closable != null) {
			try { closable.close(); } catch (IOException ioe) {
				log.warn("Failed to close stream: " + ioe);
			}
		}
	}

	/**
	 * Closes the inputstream if it is not null.
	 * Logs a warning when an error occurs.
	 */
	public static void close(final InputStream in) {
		if (in != null) {
			try { in.close(); } catch (IOException ioe) {
				log.warn("Failed to close inputstream: " + ioe);
			}
		}
	}

	/**
	 * Closes the outputstream if it is not null.
	 * Logs a warning when an error occurs.
	 */
	public static void close(final OutputStream out) {
		if (out != null) {
			try { out.close(); } catch (IOException ioe) {
				log.warn("Failed to close outputstream: " + ioe);
			}
		}
	}

	/**
	 * Closes the reader if it it is not null.
	 * Logs a warning when an error occurs.
	 */
	public static void close(final Reader in) {
		if (in != null) {
			try { in.close(); } catch (IOException ioe) {
				log.warn("Failed to close reader: " + ioe);
			}
		}
	}

	/**
	 * Closes the writer if it is not null.
	 * Logs a warning when an error occurs.
	 */
	public static void close(final Writer out) {
		if (out != null) {
			try { out.close(); } catch (IOException ioe) {
				log.warn("Failed to close writer: " + ioe);
			}
		}
	}
}
