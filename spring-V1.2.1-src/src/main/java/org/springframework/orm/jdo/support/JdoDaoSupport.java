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

package org.springframework.orm.jdo.support;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jdo.JdoTemplate;
import org.springframework.orm.jdo.PersistenceManagerFactoryUtils;

/**
 * Convenient super class for JDO data access objects.
 *
 * <p>Requires a PersistenceManagerFactory to be set, providing a JdoTemplate
 * based on it to subclasses. Can alternatively be initialized directly via a
 * JdoTemplate, to reuse the latter's settings like PersistenceManagerFactory,
 * JdoDialect, flush mode, etc.
 *
 * <p>This base class is mainly intended for JdoTemplate usage but can also
 * be used when working with PersistenceManagerFactoryUtils directly, e.g. in
 * combination with JdoInterceptor-managed PersistenceManagers. Convenience
 * <code>getPersistenceManager</code> and <code>releasePersistenceManager</code>
 * methods are provided for that usage style.
 *
 * <p>This class will create its own JdoTemplate if only a PersistenceManagerFactory
 * is passed in. The allowCreate flag on that JdoTemplate will be true by default.
 * A custom JdoTemplate instance can be used through overriding <code>createJdoTemplate</code>.
 *
 * @author Juergen Hoeller
 * @since 28.07.2003
 * @see #setPersistenceManagerFactory
 * @see #setJdoTemplate
 * @see #getPersistenceManager
 * @see #releasePersistenceManager
 * @see org.springframework.orm.jdo.JdoTemplate
 * @see org.springframework.orm.jdo.JdoInterceptor
 */
public abstract class JdoDaoSupport implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private JdoTemplate jdoTemplate;
	

	/**
	 * Set the JDO PersistenceManagerFactory to be used by this DAO.
	 * Will automatically create a JdoTemplate for the given PersistenceManagerFactory.
	 * @see #createJdoTemplate
	 * @see #setJdoTemplate
	 */
	public final void setPersistenceManagerFactory(PersistenceManagerFactory persistenceManagerFactory) {
	  this.jdoTemplate = createJdoTemplate(persistenceManagerFactory);
	}

	/**
	 * Create a JdoTemplate for the given PersistenceManagerFactory.
	 * Only invoked if populating the DAO with a PersistenceManagerFactory reference!
	 * <p>Can be overridden in subclasses to provide a JdoTemplate instance
	 * with different configuration, or a custom JdoTemplate subclass.
	 * @param persistenceManagerFactory the JDO PersistenceManagerFactoryto create a JdoTemplate for
	 * @return the new JdoTemplate instance
	 * @see #setPersistenceManagerFactory
	 */
	protected JdoTemplate createJdoTemplate(PersistenceManagerFactory persistenceManagerFactory) {
		return new JdoTemplate(persistenceManagerFactory);
	}

	/**
	 * Return the JDO PersistenceManagerFactory used by this DAO.
	 */
	public final PersistenceManagerFactory getPersistenceManagerFactory() {
		return (this.jdoTemplate != null ? this.jdoTemplate.getPersistenceManagerFactory() : null);
	}

	/**
	 * Set the JdoTemplate for this DAO explicitly,
	 * as an alternative to specifying a PersistenceManagerFactory.
	 * @see #setPersistenceManagerFactory
	 */
	public final void setJdoTemplate(JdoTemplate jdoTemplate) {
		this.jdoTemplate = jdoTemplate;
	}

	/**
	 * Return the JdoTemplate for this DAO, pre-initialized
	 * with the PersistenceManagerFactory or set explicitly.
	 */
	public final JdoTemplate getJdoTemplate() {
	  return jdoTemplate;
	}

	public final void afterPropertiesSet() throws Exception {
		if (this.jdoTemplate == null) {
			throw new IllegalArgumentException("persistenceManagerFactory or jdoTemplate is required");
		}
		initDao();
	}

	/**
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called after population of this instance's bean properties.
	 * @throws Exception if initialization fails
	 */
	protected void initDao() throws Exception {
	}
	

	/**
	 * Get a JDO PersistenceManager, either from the current transaction or
	 * a new one. The latter is only allowed if the "allowCreate" setting
	 * of this bean's JdoTemplate is true.
	 * @return the JDO PersistenceManager
	 * @throws DataAccessResourceFailureException if the PersistenceManager couldn't be created
	 * @throws IllegalStateException if no thread-bound PersistenceManager found and allowCreate false
	 * @see org.springframework.orm.jdo.PersistenceManagerFactoryUtils#getPersistenceManager
	 */
	protected final PersistenceManager getPersistenceManager() {
		return getPersistenceManager(this.jdoTemplate.isAllowCreate());
	}

	/**
	 * Get a JDO PersistenceManager, either from the current transaction or
	 * a new one. The latter is only allowed if "allowCreate" is true.
	 * @param allowCreate if a non-transactional PersistenceManager should be created
	 * when no transactional PersistenceManager can be found for the current thread
	 * @return the JDO PersistenceManager
	 * @throws DataAccessResourceFailureException if the PersistenceManager couldn't be created
	 * @throws IllegalStateException if no thread-bound PersistenceManager found and allowCreate false
	 * @see org.springframework.orm.jdo.PersistenceManagerFactoryUtils#getPersistenceManager
	 */
	protected final PersistenceManager getPersistenceManager(boolean allowCreate)
	    throws DataAccessResourceFailureException {
		return PersistenceManagerFactoryUtils.getPersistenceManager(getPersistenceManagerFactory(), allowCreate);
	}

	/**
	 * Convert the given JDOException to an appropriate exception from the
	 * org.springframework.dao hierarchy.
	 * <p>Delegates to the convertJdoAccessException method of this DAO's JdoTemplate.
	 * @param ex JDOException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #setJdoTemplate
	 * @see org.springframework.orm.jdo.JdoTemplate#convertJdoAccessException
	 */
	protected final DataAccessException convertJdoAccessException(JDOException ex) {
		return this.jdoTemplate.convertJdoAccessException(ex);
	}

	/**
	 * Close the given JDO PersistenceManager, created via this DAO's
	 * PersistenceManagerFactory, if it isn't bound to the thread.
	 * @deprecated in favor of releasePersistenceManager
	 * @see #releasePersistenceManager
	 */
	protected final void closePersistenceManagerIfNecessary(PersistenceManager pm) {
		releasePersistenceManager(pm);
	}

	/**
	 * Close the given JDO PersistenceManager, created via this DAO's
	 * PersistenceManagerFactory, if it isn't bound to the thread.
	 * @param pm PersistenceManager to close
	 * @see org.springframework.orm.jdo.PersistenceManagerFactoryUtils#releasePersistenceManager
	 */
	protected final void releasePersistenceManager(PersistenceManager pm) {
		PersistenceManagerFactoryUtils.releasePersistenceManager(pm, getPersistenceManagerFactory());
	}

}
