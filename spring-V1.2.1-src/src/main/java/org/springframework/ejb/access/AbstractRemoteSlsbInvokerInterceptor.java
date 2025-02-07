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

package org.springframework.ejb.access;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import javax.ejb.EJBObject;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.remoting.RemoteLookupFailureException;
import org.springframework.remoting.rmi.RmiClientInterceptorUtils;

/**
 * Superclass for interceptors proxying remote Stateless Session Beans.
 *
 * <p>Such an interceptor must be the last interceptor in the advice chain.
 * In this case, there is no target object.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class AbstractRemoteSlsbInvokerInterceptor extends AbstractSlsbInvokerInterceptor {
	
	private Class homeInterface;

	private boolean refreshHomeOnConnectFailure = false;


	/**
	 * Set a home interface that this invoker will narrow to before performing
	 * the parameterless SLSB <code>create()</code> call that returns the actual
	 * SLSB proxy.
	 * <p>Default is none, which will work on all J2EE servers that are not based
	 * on CORBA. A plain <code>javax.ejb.EJBHome</code> interface is known to be
	 * sufficient to make a WebSphere 5.0 Remote SLSB work. On other servers,
	 * the specific home interface for the target SLSB might be necessary.
	 */
	public void setHomeInterface(Class homeInterface) {
		if (homeInterface != null && !homeInterface.isInterface()) {
			throw new IllegalArgumentException(
					"Home interface class [" + homeInterface.getClass() + "] is not an interface");
		}
		this.homeInterface = homeInterface;
	}

	/**
	 * Set whether to refresh the EJB home on connect failure.
	 * Default is false.
	 * <p>Can be turned on to allow for hot restart of the EJB server.
	 * If a cached EJB home throws an RMI exception that indicates a
	 * remote connect failure, a fresh home will be fetched and the
	 * invocation will be retried.
	 * @see java.rmi.ConnectException
	 * @see java.rmi.ConnectIOException
	 * @see java.rmi.NoSuchObjectException
	 */
	public void setRefreshHomeOnConnectFailure(boolean refreshHomeOnConnectFailure) {
		this.refreshHomeOnConnectFailure = refreshHomeOnConnectFailure;
	}

	protected boolean isHomeRefreshable() {
		return this.refreshHomeOnConnectFailure;
	}


	/**
	 * This overridden lookup implementation performs a narrow operation
	 * after the JNDI lookup, provided that a home interface is specified.
	 * @see #setHomeInterface
	 * @see PortableRemoteObject#narrow
	 */
	protected Object lookup() throws NamingException {
		Object homeObject = super.lookup();
		if (this.homeInterface != null) {
			homeObject = PortableRemoteObject.narrow(homeObject, this.homeInterface);
		}
		return homeObject;
	}


	/**
	 * Fetches an EJB home object and delegates to doInvoke.
	 * If configured to refresh on connect failure, it will call
	 * refreshAndRetry on corresponding RMI exceptions.
	 * @see #getHome
	 * @see #doInvoke
	 * @see #refreshAndRetry
	 * @see java.rmi.ConnectException
	 * @see java.rmi.ConnectIOException
	 * @see java.rmi.NoSuchObjectException
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return doInvoke(invocation);
		}
		catch (RemoteConnectFailureException ex) {
			return handleRemoteConnectFailure(invocation, ex);
		}
		catch (RemoteException ex) {
			if (isConnectFailure(ex)) {
				return handleRemoteConnectFailure(invocation, ex);
			}
			else {
				throw ex;
			}
		}
	}

	/**
	 * Determine whether the given RMI exception indicates a connect failure.
	 * Default implementation delegates to RmiClientInterceptorUtils.
	 * @param ex the RMI exception to check
	 * @return whether the exception should be treated as connect failure
	 * @see org.springframework.remoting.rmi.RmiClientInterceptorUtils#isConnectFailure
	 */
	protected boolean isConnectFailure(RemoteException ex) {
		return RmiClientInterceptorUtils.isConnectFailure(ex);
	}

	private Object handleRemoteConnectFailure(MethodInvocation invocation, Exception ex) throws Throwable {
		if (this.refreshHomeOnConnectFailure) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not connect to remote EJB [" + getJndiName() + "] - retrying", ex);
			}
			else if (logger.isWarnEnabled()) {
				logger.warn("Could not connect to remote EJB [" + getJndiName() + "] - retrying");
			}
			return refreshAndRetry(invocation);
		}
		else {
			throw ex;
		}
	}

	/**
	 * Refresh the EJB home object and retry the given invocation.
	 * Called by invoke on connect failure.
	 * @param invocation the AOP method invocation
	 * @return the invocation result, if any
	 * @throws Throwable in case of invocation failure
	 * @see #invoke
	 */
	protected Object refreshAndRetry(MethodInvocation invocation) throws Throwable {
		synchronized (this) {
			try {
				refreshHome();
			}
			catch (Throwable ex) {
				throw new RemoteLookupFailureException("Failed to locate remote EJB [" + getJndiName() + "]", ex);
			}
		}
		return doInvoke(invocation);
	}


	/**
	 * Perform the given invocation on the current EJB home.
	 * Template method to be implemented by a subclass.
	 * @param invocation the AOP method invocation
	 * @return the invocation result, if any
	 * @throws Throwable in case of invocation failure
	 * @see #getHome
	 * @see #newSessionBeanInstance
	 */
	protected abstract Object doInvoke(MethodInvocation invocation) throws Throwable;


	/**
	 * Return a new instance of the stateless session bean.
	 * To be invoked by concrete remote SLSB invoker subclasses.
	 * <p>Can be overridden to change the algorithm.
	 * @throws NamingException if thrown by JNDI
	 * @throws InvocationTargetException if thrown by the create method
	 * @see #create
	 */
	protected EJBObject newSessionBeanInstance() throws NamingException, InvocationTargetException {
		if (logger.isDebugEnabled()) {
			logger.debug("Trying to create reference to remote EJB");
		}

		// invoke the superclass' generic create method
		Object ejbInstance = create();
		if (!(ejbInstance instanceof EJBObject)) {
			throw new AspectException("EJB instance [" + ejbInstance + "] is not a remote SLSB");
		}
		// if it throws remote exception (wrapped in InvocationTargetException), retry?

		if (logger.isDebugEnabled()) {
			logger.debug("Obtained reference to remote EJB: " + ejbInstance);
		}
		return (EJBObject) ejbInstance;
	}

	/**
	 * Remove the given EJB instance.
	 * To be invoked by concrete remote SLSB invoker subclasses.
	 * @param ejb the EJB instance to remove
	 * @see javax.ejb.EJBObject#remove
	 */
	protected void removeSessionBeanInstance(EJBObject ejb) {
		if (ejb != null) {
			try {
				ejb.remove();
			}
			catch (Throwable ex) {
				logger.warn("Could not invoke 'remove' on remote EJB proxy", ex);
			}
		}
	}

}
