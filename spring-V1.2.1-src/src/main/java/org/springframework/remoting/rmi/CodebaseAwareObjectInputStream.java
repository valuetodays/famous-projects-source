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

package org.springframework.remoting.rmi;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;
import java.rmi.server.RMIClassLoader;

import org.springframework.util.ClassUtils;

/**
 * Special ObjectInputStream subclass that falls back to a specified codebase
 * to load classes from if not found locally. In contrast to standard RMI
 * conventions for dynamic class download, it is the client that determines
 * the codebase URL here, rather than the "java.rmi.server.codebase" system
 * property on the server.
 *
 * <p>Uses the JDK's RMIClassLoader to load classes from the specified codebase.
 * The codebase can consist of multiple URLs, separated by spaces.
 * Note that RMIClassLoader requires a SecurityManager to be set, like when
 * using dynamic class download with standard RMI! (See the RMI documentation
 * for details.)
 *
 * <p>Even if residing in the RMI package, this class is <i>not</i> used for
 * RmiClientInterceptor, which uses the standard RMI infrastructure instead
 * and thus is only able to rely on RMI's standard dynamic class download via
 * "java.rmi.server.codebase". CodebaseAwareObjectInputStream is used by
 * HttpInvokerClientInterceptor (see the "codebaseUrl" property there).
 *
 * <p>Thanks to Lionel Mestre for suggesting the option and providing
 * a prototype!
 *
 * @author Juergen Hoeller
 * @since 1.1.3
 * @see RMIClassLoader
 * @see org.springframework.remoting.httpinvoker.HttpInvokerClientInterceptor#setCodebaseUrl
 */
public class CodebaseAwareObjectInputStream extends ObjectInputStream {

	private final String codebaseUrl;

	/**
	 * Create a new CodebaseAwareObjectInputStream for the given InputStream and codebase.
	 * @param	in input stream to read from
	 * @param codebaseUrl the codebase URL to load classes from if not found locally
	 * (can consist of multiple URLs, separated by spaces)
	 * @see ObjectInputStream#ObjectInputStream(InputStream)
	 */
	public CodebaseAwareObjectInputStream(InputStream in, String codebaseUrl) throws IOException {
		super(in);
		this.codebaseUrl = codebaseUrl;
	}

	/**
	 * Overridden version delegates to super class first,
	 * falling back to the specified codebase if not found locally.
	 */
	protected Class resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
		try {
			return super.resolveClass(classDesc);
		}
		catch (ClassNotFoundException ex) {
			// Explicitly resolve primitive class name.
			// This will be done by the standard ObjectInputStream on JDK 1.4+,
			// but needs to be done explicitly on JDK 1.3.
			Class clazz = ClassUtils.resolvePrimitiveClassName(classDesc.getName());
			if (clazz != null) {
				return clazz;
			}
			// If codebaseUrl is set, try to load the class with the RMIClassLoader.
			// Else, propagate the ClassNotFoundException.
			if (this.codebaseUrl == null) {
				throw ex;
			}
			return RMIClassLoader.loadClass(this.codebaseUrl, classDesc.getName());
		}
	}

	/**
	 * Overridden version delegates to super class first,
	 * falling back to the specified codebase if not found locally.
	 */
	protected Class resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
		try {
			return super.resolveProxyClass(interfaces);
		}
		catch (ClassNotFoundException ex) {
			if (this.codebaseUrl == null) {
				throw ex;
			}
			ClassLoader loader = RMIClassLoader.getClassLoader(this.codebaseUrl);
			// First resolve the interface names to Class objects, and then
			// get the proxy class defined in the chosen class loader.
			Class[] classObjs = new Class[interfaces.length];
			for (int i = 0; i < interfaces.length; i++) {
				classObjs[i] = Class.forName(interfaces[i], false, loader);
			}
			return Proxy.getProxyClass(loader, classObjs);
		}
	}

}
