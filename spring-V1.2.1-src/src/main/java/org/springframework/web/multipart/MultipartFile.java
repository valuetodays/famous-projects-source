/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface which represents an uploaded file received in a multipart request.
 *
 * <p>The file contents are either stored in memory or temporarily on disk.
 * In either case, the user is responsible for copying file contents to a
 * session-level or persistent store if desired. The temporary storages
 * will be cleared at the end of request processing.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see org.springframework.web.multipart.MultipartHttpServletRequest
 * @see org.springframework.web.multipart.MultipartResolver
 */
public interface MultipartFile {

	/**
	 * Return the name of the parameter in the multipart form.
	 * @return the name of the parameter
	 */
	String getName();

	/**
	 * Return whether the uploaded file is empty in the sense that
	 * no file has been chosen in the multipart form.
	 * @return whether the uploaded file is empty
	 */
	boolean isEmpty();

	/**
	 * Return the original filename in the client's filesystem.
	 * This may contain path information depending on the browser used,
	 * but it typically will not with any other than Opera.
	 * @return the original filename, or null if empty
	 */
	String getOriginalFilename();

	/**
	 * Return the content type of the file.
	 * @return the content type, or null if empty or not defined
	 */
	String getContentType();

	/**
	 * Return the size of the file in bytes.
	 * @return the size of the file, or 0 if empty
	 */
	long getSize();

	/**
	 * Return the contents of the file as an array of bytes.
	 * @return the contents of the file as bytes,
	 * or an empty byte array if empty
	 * @throws IOException in case of access errors
	 * (if the temporary store fails)
	 */
	byte[] getBytes() throws IOException;

	/**
	 * Return an InputStream to read the contents of the file from.
	 * The user is responsible for closing the stream.
	 * @return the contents of the file as stream,
	 * or an empty stream if empty
	 * @throws IOException in case of access errors
	 * (if the temporary store fails)
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Transfer the received file to the given destination file.
	 * <p>This may either move the file in the filesystem, copy the file in the
	 * filesystem, or save memory-held contents to the destination file.
	 * If the destination file already exists, it will be deleted first.
	 * <p>If the file has been moved in the filesystem, this operation cannot
	 * be invoked again. Therefore, call this method just once to be able to
	 * work with any storage mechanism.
	 * @param dest the destination file
	 * @throws IOException in case of reading or writing errors
	 * @throws IllegalStateException if the file has already been moved
	 * in the filesystem as is not available anymore for another transfer
	 */
	void transferTo(File dest) throws IOException, IllegalStateException;

}
