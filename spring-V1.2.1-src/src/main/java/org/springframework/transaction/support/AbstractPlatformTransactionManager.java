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

package org.springframework.transaction.support;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.Constants;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.InvalidTimeoutException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSuspensionNotSupportedException;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * Abstract base class that allows for easy implementation of concrete
 * platform transaction managers like JtaTransactionManager and
 * DataSourceTransactionManager.
 *
 * <p>This base class provides the following workflow handling:
 * <ul>
 * <li>determines if there is an existing transaction;
 * <li>applies the appropriate propagation behavior;
 * <li>suspends and resumes transactions if necessary;
 * <li>checks the rollback-only flag on commit;
 * <li>applies the appropriate modification on rollback
 * (actual rollback or setting rollback-only);
 * <li>triggers registered synchronization callbacks
 * (if transaction synchronization is active).
 * </ul>
 *
 * <p>Subclasses have to implement specific template methods for specific
 * states of a transaction, for example begin, suspend, resume, commit, rollback.
 * The most important of them are abstract and must be provided by a concrete
 * implementation; for the rest, defaults are provided, so overriding is optional.
 *
 * <p>Transaction synchronization is a generic mechanism for registering callbacks
 * that get invoked at transaction completion time. This is mainly used internally
 * by the data access support classes for JDBC, Hibernate, and JDO when running
 * within a JTA transaction: They register resources that are opened within the
 * transaction for closing at transaction completion time, allowing e.g. for reuse
 * of the same Hibernate Session within the transaction. The same mechanism can
 * also be leveraged for custom synchronization needs in an application.
 * 
 * <p>The state of this class is serializable, to allow for serializing the
 * transaction strategy along with proxies that carry a transaction interceptor.
 * It is up to subclasses if they wish to make their state to be serializable too.
 * They should implement the <code>java.io.Serializable</code> marker interface in
 * that case, and potentially a private <code>readObject()</code> method (according
 * to Java serialization rules) if they need to restore any transient state.
 *
 * @author Juergen Hoeller
 * @since 28.03.2003
 * @see #setTransactionSynchronization
 * @see TransactionSynchronizationManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 * @see org.springframework.orm.hibernate.HibernateTransactionManager
 * @see org.springframework.orm.jdo.JdoTransactionManager
 */
public abstract class AbstractPlatformTransactionManager implements PlatformTransactionManager {

	/**
	 * Always activate transaction synchronization, even for "empty" transactions
	 * that result from PROPAGATION_SUPPORTS with no existing backend transaction.
	 */
	public static final int SYNCHRONIZATION_ALWAYS = 0;

	/**
	 * Activate transaction synchronization only for actual transactions,
	 * i.e. not for empty ones that result from PROPAGATION_SUPPORTS with no
	 * existing backend transaction.
	 */
	public static final int SYNCHRONIZATION_ON_ACTUAL_TRANSACTION = 1;

	/**
	 * Never active transaction synchronization.
	 */
	public static final int SYNCHRONIZATION_NEVER = 2;


	/** Constants instance for AbstractPlatformTransactionManager */
	private static final Constants constants = new Constants(AbstractPlatformTransactionManager.class);

	/** Transient to optimize serialization */
	protected transient Log logger = LogFactory.getLog(getClass());

	private int transactionSynchronization = SYNCHRONIZATION_ALWAYS;

	private boolean nestedTransactionAllowed = false;

	private boolean rollbackOnCommitFailure = false;


	/**
	 * Set the transaction synchronization by the name of the corresponding constant
	 * in this class, e.g. "SYNCHRONIZATION_ALWAYS".
	 * @param constantName name of the constant
	 * @see #SYNCHRONIZATION_ALWAYS
	 */
	public void setTransactionSynchronizationName(String constantName) {
		setTransactionSynchronization(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set when this transaction manager should activate the thread-bound
	 * transaction synchronization support. Default is "always".
	 * <p>Note that transaction synchronization isn't supported for
	 * multiple concurrent transactions by different transaction managers.
	 * Only one transaction manager is allowed to activate it at any time.
	 * @see #SYNCHRONIZATION_ALWAYS
	 * @see #SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
	 * @see #SYNCHRONIZATION_NEVER
	 * @see TransactionSynchronizationManager
	 * @see TransactionSynchronization
	 */
	public void setTransactionSynchronization(int transactionSynchronization) {
		this.transactionSynchronization = transactionSynchronization;
	}

	/**
	 * Return if this transaction manager should activate the thread-bound
	 * transaction synchronization support.
	 */
	public int getTransactionSynchronization() {
		return transactionSynchronization;
	}

	/**
	 * Set whether nested transactions are allowed. Default is false.
	 * <p>Typically initialized with an appropriate default by the
	 * concrete transaction manager subclass.
	 */
	public void setNestedTransactionAllowed(boolean nestedTransactionAllowed) {
		this.nestedTransactionAllowed = nestedTransactionAllowed;
	}

	/**
	 * Return whether nested transactions are allowed.
	 */
	public boolean isNestedTransactionAllowed() {
		return nestedTransactionAllowed;
	}

	/**
	 * Set if doRollback should be performed on failure of the doCommit call.
	 * Typically not necessary and thus to be avoided as it can override the
	 * commit exception with a subsequent rollback exception. Default is false.
	 * @see #doCommit
	 * @see #doRollback
	 */
	public void setRollbackOnCommitFailure(boolean rollbackOnCommitFailure) {
		this.rollbackOnCommitFailure = rollbackOnCommitFailure;
	}

	/**
	 * Return if a rollback should be performed on failure of the commit call.
	 */
	public boolean isRollbackOnCommitFailure() {
		return rollbackOnCommitFailure;
	}


	//---------------------------------------------------------------------
	// Implementation of PlatformTransactionManager
	//---------------------------------------------------------------------

	/**
	 * This implementation handles propagation behavior. Delegates to
	 * <code>doGetTransaction</code>, <code>isExistingTransaction</code>
	 * and <code>doBegin</code>.
	 * @see #doGetTransaction
	 * @see #isExistingTransaction
	 * @see #doBegin
	 */
	public final TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
		Object transaction = doGetTransaction();

		// Cache debug flag to avoid repeated checks.
		boolean debugEnabled = logger.isDebugEnabled();
		if (debugEnabled) {
			logger.debug("Using transaction object [" + transaction + "]");
		}

		if (definition == null) {
			// Use defaults if no transaction definition given.
			definition = new DefaultTransactionDefinition();
		}

		if (isExistingTransaction(transaction)) {
			// Existing transaction found -> check propagation behavior to find out how to behave.
			return getExistingTransaction(definition, transaction, debugEnabled);
		}

		// Check definition settings for new transaction.
		if (definition.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
			throw new InvalidTimeoutException("Invalid transaction timeout", definition.getTimeout());
		}

		// No existing transaction found -> check propagation behavior to find out how to behave.
		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
			throw new IllegalTransactionStateException(
					"Transaction propagation 'mandatory' but no existing transaction found");
		}
		else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
				definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
		    definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			if (debugEnabled) {
				logger.debug("Creating new transaction");
			}
			doBegin(transaction, definition);
			boolean newSynchronization = (this.transactionSynchronization != SYNCHRONIZATION_NEVER);
			return newTransactionStatus(definition, transaction, true, newSynchronization, debugEnabled, null);
		}
		else {
			// Create "empty" transaction: no actual transaction, but potentially synchronization.
			boolean newSynchronization = (this.transactionSynchronization == SYNCHRONIZATION_ALWAYS);
			return newTransactionStatus(definition, null, false, newSynchronization, debugEnabled, null);
		}
	}

	/**
	 * Create a TransactionStatus for an existing transaction.
	 */
	private TransactionStatus getExistingTransaction(
			TransactionDefinition definition, Object transaction, boolean debugEnabled)
			throws TransactionException {

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
			throw new IllegalTransactionStateException(
					"Transaction propagation 'never' but existing transaction found");
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
			if (debugEnabled) {
				logger.debug("Suspending current transaction");
			}
			Object suspendedResources = suspend(transaction);
			boolean newSynchronization = (this.transactionSynchronization == SYNCHRONIZATION_ALWAYS);
			return newTransactionStatus(
					definition, null, false, newSynchronization, debugEnabled, suspendedResources);
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
			if (debugEnabled) {
				logger.debug("Creating new transaction, suspending current one");
			}
			Object suspendedResources = suspend(transaction);
			doBegin(transaction, definition);
			boolean newSynchronization = (this.transactionSynchronization != SYNCHRONIZATION_NEVER);
			return newTransactionStatus(
					definition, transaction, true, newSynchronization, debugEnabled, suspendedResources);
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			if (!isNestedTransactionAllowed()) {
				throw new NestedTransactionNotSupportedException(
						"Transaction manager does not allow nested transactions by default - " +
						"specify 'nestedTransactionAllowed' property with value 'true'");
			}
			if (debugEnabled) {
				logger.debug("Creating nested transaction");
			}
			if (useSavepointForNestedTransaction()) {
				// Create savepoint within existing Spring-managed transaction,
				// through the SavepointManager API implemented by TransactionStatus.
				// Usually uses JDBC 3.0 savepoints. Never activates Spring synchronization.
				DefaultTransactionStatus status =
						newTransactionStatus(definition, transaction, false, false, debugEnabled, null);
				status.createAndHoldSavepoint();
				return status;
			}
			else {
				// Nested transaction through nested begin and commit/rollback calls.
				// Usually only for JTA: Spring synchronization might get activated here
				// in case of a pre-existing JTA transaction.
				doBegin(transaction, definition);
				boolean newSynchronization = (this.transactionSynchronization != SYNCHRONIZATION_NEVER);
				return newTransactionStatus(definition, transaction, true, newSynchronization, debugEnabled, null);
			}
		}

		// Assumably PROPAGATION_SUPPORTS.
		if (debugEnabled) {
			logger.debug("Participating in existing transaction");
		}
		boolean newSynchronization = (this.transactionSynchronization != SYNCHRONIZATION_NEVER);
		return newTransactionStatus(definition, transaction, false, newSynchronization, debugEnabled, null);
	}

	/**
	 * Create a new TransactionStatus for the given arguments,
	 * initializing transaction synchronization if appropriate.
	 */
	private DefaultTransactionStatus newTransactionStatus(
			TransactionDefinition definition, Object transaction, boolean newTransaction,
			boolean newSynchronization, boolean debug, Object suspendedResources) {

		boolean actualNewSynchronization = newSynchronization &&
				!TransactionSynchronizationManager.isSynchronizationActive();
		if (actualNewSynchronization) {
			if (newTransaction) {
				TransactionSynchronizationManager.setActualTransactionActive(true);
			}
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(definition.isReadOnly());
			TransactionSynchronizationManager.setCurrentTransactionName(definition.getName());
			TransactionSynchronizationManager.initSynchronization();
		}
		return new DefaultTransactionStatus(
				transaction, newTransaction, actualNewSynchronization,
				definition.isReadOnly(), debug, suspendedResources);
	}

	/**
	 * Suspend the given transaction. Suspends transaction synchronization first,
	 * then delegates to the <code>doSuspend</code> template method.
	 * @param transaction the current transaction object
	 * @return an object that holds suspended resources
	 * @see #doSuspend
	 * @see #resume
	 */
	private SuspendedResourcesHolder suspend(Object transaction) throws TransactionException {
		Object holder = doSuspend(transaction);
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			List suspendedSynchronizations = TransactionSynchronizationManager.getSynchronizations();
			for (Iterator it = suspendedSynchronizations.iterator(); it.hasNext();) {
				((TransactionSynchronization) it.next()).suspend();
			}
			TransactionSynchronizationManager.clearSynchronization();
			String name = TransactionSynchronizationManager.getCurrentTransactionName();
			TransactionSynchronizationManager.setCurrentTransactionName(null);
			boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
			TransactionSynchronizationManager.setActualTransactionActive(false);
			return new SuspendedResourcesHolder(holder, suspendedSynchronizations, name, readOnly);
		}
		return new SuspendedResourcesHolder(holder, null, null, false);
	}

	/**
	 * Resume the given transaction. Delegates to the <code>doResume</code>
	 * template method first, then resuming transaction synchronization.
	 * @param transaction the current transaction object
	 * @param resourcesHolder the object that holds suspended resources,
	 * as returned by suspend
	 * @see #doResume
	 * @see #suspend
	 */
	private void resume(Object transaction, SuspendedResourcesHolder resourcesHolder) throws TransactionException {
		if (resourcesHolder.getSuspendedSynchronizations() != null) {
			TransactionSynchronizationManager.setActualTransactionActive(true);
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(resourcesHolder.isReadOnly());
			TransactionSynchronizationManager.setCurrentTransactionName(resourcesHolder.getName());
			TransactionSynchronizationManager.initSynchronization();
			for (Iterator it = resourcesHolder.getSuspendedSynchronizations().iterator(); it.hasNext();) {
				TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
				synchronization.resume();
				TransactionSynchronizationManager.registerSynchronization(synchronization);
			}
		}
		doResume(transaction, resourcesHolder.getSuspendedResources());
	}


	/**
	 * This implementation of commit handles participating in existing
	 * transactions and programmatic rollback requests.
	 * Delegates to <code>isRollbackOnly</code>, <code>doCommit</code>
	 * and <code>rollback</code>.
	 * @see org.springframework.transaction.TransactionStatus#isRollbackOnly
	 * @see #doCommit
	 * @see #rollback
	 */
	public final void commit(TransactionStatus status) throws TransactionException {
		DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;

		if (defStatus.isCompleted()) {
			throw new IllegalTransactionStateException(
					"Transaction is already completed - do not call commit or rollback more than once per transaction");
		}
		if (defStatus.isLocalRollbackOnly()) {
			if (defStatus.isDebug()) {
				logger.debug("Transactional code has requested rollback");
			}
			rollback(status);
			return;
		}
		if (defStatus.isGlobalRollbackOnly() && !shouldCommitOnGlobalRollbackOnly()) {
			if (defStatus.isDebug()) {
				logger.debug("Global transaction is marked as rollback-only but transactional code requested commit");
			}
			rollback(status);
			throw new UnexpectedRollbackException(
					"Transaction has been rolled back because it has been marked as rollback-only");
		}

		// Process actual commit.
		try {
			boolean beforeCompletionInvoked = false;
			try {
				triggerBeforeCommit(defStatus);
				triggerBeforeCompletion(defStatus, null);
				beforeCompletionInvoked = true;
				if (defStatus.hasSavepoint()) {
					if (defStatus.isDebug()) {
						logger.debug("Releasing transaction savepoint");
					}
					defStatus.releaseHeldSavepoint();
				}
				else if (status.isNewTransaction()) {
					logger.debug("Initiating transaction commit");
					doCommit(defStatus);
				}
			}
			catch (UnexpectedRollbackException ex) {
				// can only be caused by doCommit
				triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_ROLLED_BACK, ex);
				throw ex;
			}
			catch (TransactionException ex) {
				// can only be caused by doCommit
				if (isRollbackOnCommitFailure()) {
					doRollbackOnCommitException(defStatus, ex);
				}
				else {
					triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_UNKNOWN, ex);
				}
				throw ex;
			}
			catch (RuntimeException ex) {
				if (!beforeCompletionInvoked) {
					triggerBeforeCompletion(defStatus, ex);
				}
				doRollbackOnCommitException(defStatus, ex);
				throw ex;
			}
			catch (Error err) {
				if (!beforeCompletionInvoked) {
					triggerBeforeCompletion(defStatus, err);
				}
				doRollbackOnCommitException(defStatus, err);
				throw err;
			}
			triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_COMMITTED, null);
		}
		finally {
			cleanupAfterCompletion(defStatus);
		}
	}

	/**
	 * This implementation of rollback handles participating in existing
	 * transactions. Delegates to <code>doRollback</code> and
	 * <code>doSetRollbackOnly</code>.
	 * @see #doRollback
	 * @see #doSetRollbackOnly
	 */
	public final void rollback(TransactionStatus status) throws TransactionException {
		DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
		if (defStatus.isCompleted()) {
			throw new IllegalTransactionStateException(
					"Transaction is already completed - do not call commit or rollback more than once per transaction");
		}
		try {
			try {
				triggerBeforeCompletion(defStatus, null);
				if (defStatus.hasSavepoint()) {
					if (defStatus.isDebug()) {
						logger.debug("Rolling back transaction to savepoint");
					}
					defStatus.rollbackToHeldSavepoint();
				}
				else if (status.isNewTransaction()) {
					logger.debug("Initiating transaction rollback");
					doRollback(defStatus);
				}
				else if (defStatus.getTransaction() != null) {
					if (defStatus.isDebug()) {
						logger.debug("Setting existing transaction rollback-only");
					}
					doSetRollbackOnly(defStatus);
				}
				else {
					logger.warn("Should roll back transaction but cannot - no transaction available");
				}
			}
			catch (RuntimeException ex) {
				triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_UNKNOWN, ex);
				throw ex;
			}
			catch (Error err) {
				triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_UNKNOWN, err);
				throw err;
			}
			triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_ROLLED_BACK, null);
		}
		finally {
			cleanupAfterCompletion(defStatus);
		}
	}

	/**
	 * Invoke doRollback, handling rollback exceptions properly.
	 * @param status object representing the transaction
	 * @param ex the thrown application exception or error
	 * @throws TransactionException in case of a rollback error
	 * @see #doRollback
	 */
	private void doRollbackOnCommitException(DefaultTransactionStatus status, Throwable ex)
	    throws TransactionException {
		try {
			if (status.isNewTransaction()) {
				if (status.isDebug()) {
					logger.debug("Initiating transaction rollback on commit exception", ex);
				}
				doRollback(status);
			}
		}
		catch (RuntimeException rbex) {
			logger.error("Commit exception overridden by rollback exception", ex);
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN, rbex);
			throw rbex;
		}
		catch (Error rberr) {
			logger.error("Commit exception overridden by rollback exception", ex);
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN, rberr);
			throw rberr;
		}
		triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK, ex);
	}

	/**
	 * Trigger beforeCommit callback.
	 * @param status object representing the transaction
	 */
	private void triggerBeforeCommit(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			logger.debug("Triggering beforeCommit synchronization");
			for (Iterator it = TransactionSynchronizationManager.getSynchronizations().iterator(); it.hasNext();) {
				TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
				synchronization.beforeCommit(status.isReadOnly());
			}
		}
	}

	/**
	 * Trigger beforeCompletion callback.
	 * @param status object representing the transaction
	 * @param ex the thrown application exception or error, or null
	 */
	private void triggerBeforeCompletion(DefaultTransactionStatus status, Throwable ex) {
		if (status.isNewSynchronization()) {
			logger.debug("Triggering beforeCompletion synchronization");
			try {
				for (Iterator it = TransactionSynchronizationManager.getSynchronizations().iterator(); it.hasNext();) {
					TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
					synchronization.beforeCompletion();
				}
			}
			catch (RuntimeException tsex) {
				if (ex != null) {
					logger.error("Rollback exception overridden by synchronization exception", ex);
				}
				throw tsex;
			}
			catch (Error tserr) {
				if (ex != null) {
					logger.error("Rollback exception overridden by synchronization exception", ex);
				}
				throw tserr;
			}
		}
	}

	/**
	 * Trigger afterCompletion callback, handling exceptions properly.
	 * @param status object representing the transaction
	 * @param completionStatus completion status according to TransactionSynchronization constants
	 * @param ex the thrown application exception or error, or null
	 */
	private void triggerAfterCompletion(DefaultTransactionStatus status, int completionStatus, Throwable ex) {
		if (status.isNewSynchronization()) {
			logger.debug("Triggering afterCompletion synchronization");
			try {
				for (Iterator it = TransactionSynchronizationManager.getSynchronizations().iterator(); it.hasNext();) {
					TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
					synchronization.afterCompletion(completionStatus);
				}
			}
			catch (RuntimeException tsex) {
				if (ex != null) {
					logger.error("Rollback exception overridden by synchronization exception", ex);
				}
				throw tsex;
			}
			catch (Error tserr) {
				if (ex != null) {
					logger.error("Rollback exception overridden by synchronization exception", ex);
				}
				throw tserr;
			}
		}
	}

	/**
	 * Clean up after completion, clearing synchronization if necessary,
	 * and invoking doCleanupAfterCompletion.
	 * @param status object representing the transaction
	 * @see #doCleanupAfterCompletion
	 */
	private void cleanupAfterCompletion(DefaultTransactionStatus status) {
		status.setCompleted();
		if (status.isNewSynchronization()) {
			TransactionSynchronizationManager.clearSynchronization();
			TransactionSynchronizationManager.setCurrentTransactionName(null);
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
			if (status.isNewTransaction()) {
				TransactionSynchronizationManager.setActualTransactionActive(false);
			}
		}
		if (status.isNewTransaction()) {
			doCleanupAfterCompletion(status.getTransaction());
		}
		if (status.getSuspendedResources() != null) {
			if (status.isDebug()) {
				logger.debug("Resuming suspended transaction");
			}
			resume(status.getTransaction(), (SuspendedResourcesHolder) status.getSuspendedResources());
		}
	}


	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException {
		// Rely on default serialization, just initialize state after deserialization.
		try {
			ois.defaultReadObject();
		}
		catch (ClassNotFoundException ex) {
			throw new IOException("Failed to deserialize [" + getClass().getName() + "] - " +
					"check that Spring transaction libraries are available on the client side: " + ex.getMessage());
		}

		// initialize transient fields
		this.logger = LogFactory.getLog(getClass());
	}


	//---------------------------------------------------------------------
	// Template methods to be implemented in subclasses
	//---------------------------------------------------------------------

	/**
	 * Return a transaction object for the current transaction state.
	 * <p>The returned object will usually be specific to the concrete transaction
	 * manager implementation, carrying corresponding transaction state in a
	 * modifiable fashion. This object will be passed into the other template
	 * methods (e.g. doBegin and doCommit), either directly or as part of a
	 * DefaultTransactionStatus instance.
	 * <p>The returned object should contain information about any existing
	 * transaction, that is, a transaction that has already started before the
	 * current <code>getTransaction</code> call on the transaction manager.
	 * Consequently, a <code>doGetTransaction</code> implementation will usually
	 * look for an existing transaction and store corresponding state in the
	 * returned transaction object.
	 * @return the current transaction object
	 * @throws org.springframework.transaction.CannotCreateTransactionException
	 * if transaction support is not available
	 * @throws TransactionException in case of lookup or system errors
	 * @see #doBegin
	 * @see #doCommit
	 * @see #doRollback
	 * @see DefaultTransactionStatus#getTransaction
	 */
	protected abstract Object doGetTransaction() throws TransactionException;

	/**
	 * Check if the given transaction object indicates an existing transaction
	 * (that is, a transaction which has already started).
	 * <p>The result will be evaluated according to the specified propagation
	 * behavior for the new transaction. An existing transaction might get
	 * suspended (in case of PROPAGATION_REQUIRES_NEW), or the new transaction
	 * might participate in the existing one (in case of PROPAGATION_REQUIRED).
	 * <p>Default implementation returns false, assuming that detection of or
	 * participating in existing transactions is generally not supported.
	 * Subclasses are of course encouraged to provide such support.
	 * @param transaction transaction object returned by doGetTransaction
	 * @return if there is an existing transaction
	 * @throws TransactionException in case of system errors
	 * @see #doGetTransaction
	 */
	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		return false;
	}

	/**
	 * Return whether to use a savepoint for a nested transaction. Default is true,
	 * which causes delegation to DefaultTransactionStatus for holding a savepoint.
	 * <p>Subclasses can override this to return false, causing a further invocation
	 * of <code>doBegin</code> despite an already existing transaction.
	 * @see DefaultTransactionStatus#createAndHoldSavepoint
	 * @see DefaultTransactionStatus#rollbackToHeldSavepoint
	 * @see DefaultTransactionStatus#releaseHeldSavepoint
	 * @see #doBegin
	 */
	protected boolean useSavepointForNestedTransaction() {
		return true;
	}

	/**
	 * Begin a new transaction with the given transaction definition.
	 * Does not have to care about applying the propagation behavior,
	 * as this has already been handled by this abstract manager.
	 * @param transaction transaction object returned by doGetTransaction
	 * @param definition TransactionDefinition instance, describing
	 * propagation behavior, isolation level, timeout etc.
	 * @throws TransactionException in case of creation or system errors
	 */
	protected abstract void doBegin(Object transaction, TransactionDefinition definition)
	    throws TransactionException;

	/**
	 * Suspend the resources of the current transaction.
	 * Transaction synchronization will already have been suspended.
	 * <p>Default implementation throws a TransactionSuspensionNotSupportedException,
	 * assuming that transaction suspension is generally not supported.
	 * @param transaction transaction object returned by doGetTransaction
	 * @return an object that holds suspended resources
	 * (will be kept unexamined for passing it into doResume)
	 * @throws org.springframework.transaction.TransactionSuspensionNotSupportedException
	 * if suspending is not supported by the transaction manager implementation
	 * @throws TransactionException in case of system errors
	 * @see #doResume
	 */
	protected Object doSuspend(Object transaction) throws TransactionException {
		throw new TransactionSuspensionNotSupportedException(
				"Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
	}

	/**
	 * Resume the resources of the current transaction.
	 * Transaction synchronization will be resumed afterwards.
	 * <p>Default implementation throws a TransactionSuspensionNotSupportedException,
	 * assuming that transaction suspension is generally not supported.
	 * @param transaction transaction object returned by doGetTransaction
	 * @param suspendedResources the object that holds suspended resources,
	 * as returned by doSuspend
	 * @throws org.springframework.transaction.TransactionSuspensionNotSupportedException
	 * if resuming is not supported by the transaction manager implementation
	 * @throws TransactionException in case of system errors
	 * @see #doSuspend
	 */
	protected void doResume(Object transaction, Object suspendedResources) throws TransactionException {
		throw new TransactionSuspensionNotSupportedException(
				"Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
	}

	/**
	 * Return whether to call <code>doCommit</code> on a transaction
	 * that has been marked as rollback-only in a global fashion.
	 * <p>Does not apply if an application locally sets the transaction to
	 * rollback-only via the TransactionStatus, but only to the transaction
	 * itself being marked as rollback-only by the transaction coordinator.
	 * <p>Default is "false": Local transaction strategies usually don't
	 * hold the rollback-only marker in the transaction itself, therefore
	 * they can't handle rollback-only transactions in a special manner.
	 * Hence, AbstractPlatformTransactionManager will trigger a rollback
	 * in that case, throwing an UnexpectedRollbackException afterwards.
	 * <p>Override this to return "true" if the concrete transaction manager
	 * expects a <code>doCommit</code> call even for a rollback-only transaction,
	 * allowing for special handling there. This will, for example, be the case
	 * for JTA, where <code>UserTransaction.commit</code> will check the read-only
	 * flag itself and throw a corresponding RollbackException, which might
	 * include the specific reason (such as a transaction timeout).
	 * @see #doCommit
	 * @see DefaultTransactionStatus#isGlobalRollbackOnly()
	 * @see DefaultTransactionStatus#isLocalRollbackOnly()
	 * @see org.springframework.transaction.TransactionStatus#setRollbackOnly()
	 * @see org.springframework.transaction.UnexpectedRollbackException
	 * @see javax.transaction.UserTransaction#commit()
	 * @see javax.transaction.RollbackException
	 */
	protected boolean shouldCommitOnGlobalRollbackOnly() {
		return false;
	}

	/**
	 * Perform an actual commit of the given transaction.
	 * <p>An implementation does not need to check the "new transaction" flag
	 * or the rollback-only flag; this will already have been handled before.
	 * Usually, a straight commit will be performed on the transaction object
	 * contained in the passed-in status.
	 * @param status the status representation of the transaction
	 * @throws TransactionException in case of commit or system errors
	 * @see DefaultTransactionStatus#getTransaction
	 */
	protected abstract void doCommit(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * Perform an actual rollback of the given transaction.
	 * <p>An implementation does not need to check the "new transaction" flag;
	 * this will already have been handled before. Usually, a straight rollback
	 * will be performed on the transaction object contained in the passed-in status.
	 * @param status the status representation of the transaction
	 * @throws TransactionException in case of system errors
	 * @see DefaultTransactionStatus#getTransaction
	 */
	protected abstract void doRollback(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * Set the given transaction rollback-only. Only called on rollback
	 * if the current transaction participates in an existing one.
	 * <p>Default implementation throws an IllegalTransactionStateException,
	 * assuming that participating in existing transactions is generally not
	 * supported. Subclasses are of course encouraged to provide such support.
	 * @param status the status representation of the transaction
	 * @throws TransactionException in case of system errors
	 */
	protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
		throw new IllegalTransactionStateException(
				"Participating in existing transactions is not supported - when 'isExistingTransaction' " +
				"returns true, appropriate 'doSetRollbackOnly' behavior must be provided");
	}

	/**
	 * Cleanup resources after transaction completion.
	 * Called after <code>doCommit</code> and <code>doRollback</code> execution,
	 * on any outcome. Default implementation does nothing.
	 * <p>Should not throw any exceptions but just issue warnings on errors.
	 * @param transaction transaction object returned by doGetTransaction
	 */
	protected void doCleanupAfterCompletion(Object transaction) {
	}


	/**
	 * Holder for suspended resources.
	 * Used internally by <code>suspend</code> and <code>resume</code>.
	 */
	private static class SuspendedResourcesHolder {

		private final Object suspendedResources;

		private final List suspendedSynchronizations;

		private final String name;

		private final boolean readOnly;

		public SuspendedResourcesHolder(
				Object suspendedResources, List suspendedSynchronizations, String name, boolean readOnly) {

			this.suspendedResources = suspendedResources;
			this.suspendedSynchronizations = suspendedSynchronizations;
			this.name = name;
			this.readOnly = readOnly;
		}

		public Object getSuspendedResources() {
			return suspendedResources;
		}

		public List getSuspendedSynchronizations() {
			return suspendedSynchronizations;
		}

		public String getName() {
			return name;
		}

		public boolean isReadOnly() {
			return readOnly;
		}
	}

}
