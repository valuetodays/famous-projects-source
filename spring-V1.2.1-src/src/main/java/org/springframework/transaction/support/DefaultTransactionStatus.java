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

import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionUsageException;

/**
 * Default implementation of the TransactionStatus interface,
 * used by AbstractPlatformTransactionManager.
 *
 * <p>Holds all status information that AbstractPlatformTransactionManager
 * needs internally, including a generic transaction object determined by
 * the concrete transaction manager implementation.
 *
 * <p>Supports delegating savepoint-related methods to a transaction object
 * that implements the SavepointManager interface.
 *
 * @author Juergen Hoeller
 * @since 19.01.2004
 * @see AbstractPlatformTransactionManager
 * @see org.springframework.transaction.SavepointManager
 * @see #createSavepoint
 * @see #rollbackToSavepoint
 * @see #releaseSavepoint
 * @see #getTransaction
 */
public class DefaultTransactionStatus implements TransactionStatus {

	private final Object transaction;

	private final boolean newTransaction;

	private final boolean newSynchronization;

	private final boolean readOnly;

	private final boolean debug;

	private final Object suspendedResources;

	private boolean rollbackOnly = false;

	private boolean completed = false;

	private Object savepoint;


	/**
	 * Create a new TransactionStatus instance.
	 * @param transaction underlying transaction object that can hold
	 * state for the internal transaction implementation
	 * @param newTransaction if the transaction is new,
	 * else participating in an existing transaction
	 * @param newSynchronization if a new transaction synchronization
	 * has been opened for the given transaction
	 * @param readOnly whether the transaction is read-only
	 * @param debug should debug logging be enabled for the handling of this transaction?
	 * Caching it in here can prevent repeated calls to ask the logging system whether
	 * debug logging should be enabled.
	 * @param suspendedResources a holder for resources that have been suspended
	 * for this transaction, if any
	 */
	public DefaultTransactionStatus(
	    Object transaction, boolean newTransaction, boolean newSynchronization,
	    boolean readOnly, boolean debug, Object suspendedResources) {

		this.transaction = transaction;
		this.newTransaction = newTransaction;
		this.newSynchronization = newSynchronization;
		this.readOnly = readOnly;
		this.debug = debug;
		this.suspendedResources = suspendedResources;
	}

	/**
	 * Return the underlying transaction object.
	 */
	public Object getTransaction() {
		return transaction;
	}

	/**
	 * Return whether there is actual transaction active.
	 */
	public boolean hasTransaction() {
		return (this.transaction != null);
	}

	public boolean isNewTransaction() {
		return (hasTransaction() && this.newTransaction);
	}

	/**
	 * Return if a new transaction synchronization has been opened
	 * for this transaction.
	 */
	public boolean isNewSynchronization() {
		return newSynchronization;
	}

	/**
	 * Return if this transaction is defined as read-only transaction.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Return whether the progress of this transaction is debugged. This is used
	 * by AbstractPlatformTransactionManager as an optimization, to prevent repeated
	 * calls to logger.isDebug(). Not really intended for client code.
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Return the holder for resources that have been suspended for this transaction,
	 * if any.
	 */
	public Object getSuspendedResources() {
		return suspendedResources;
	}


	//---------------------------------------------------------------------
	// Handling of current transaction state
	//---------------------------------------------------------------------

	/**
	 * Set a savepoint for this transaction. Used for PROPAGATION_NESTED.
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NESTED
	 */
	protected void setSavepoint(Object savepoint) {
		this.savepoint = savepoint;
	}

	/**
	 * Get the savepoint for this transaction, if any.
	 */
	public Object getSavepoint() {
		return savepoint;
	}

	public boolean hasSavepoint() {
		return (savepoint != null);
	}

	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}

	/**
	 * Determine the rollback-only flag via checking both this TransactionStatus
	 * and the transaction object, provided that the latter implements the
	 * SmartTransactionObject interface.
	 * @see SmartTransactionObject#isRollbackOnly
	 */
	public boolean isRollbackOnly() {
		return (isLocalRollbackOnly() || isGlobalRollbackOnly());
	}

	/**
	 * Determine the rollback-only flag via checking this TransactionStatus.
	 * <p>Will only return "true" if the application called <code>setRollbackOnly</code>
	 * on this TransactionStatus object.
	 */
	public boolean isLocalRollbackOnly() {
		return this.rollbackOnly;
	}

	/**
	 * Determine the rollback-only flag via checking both the transaction object,
	 * provided that the latter implements the SmartTransactionObject interface.
	 * <p>Will return "true" if the transaction itself has been marked rollback-only
	 * by the transaction coordinator, for example in case of a timeout.
	 * @see SmartTransactionObject#isRollbackOnly
	 */
	public boolean isGlobalRollbackOnly() {
		return ((this.transaction instanceof SmartTransactionObject) &&
		 ((SmartTransactionObject) this.transaction).isRollbackOnly());
	}

	/**
	 * Mark this transaction as completed, that is, committed or rolled back.
	 */
	public void setCompleted() {
		this.completed = true;
	}

	public boolean isCompleted() {
		return completed;
	}


	//---------------------------------------------------------------------
	// Implementation of SavepointManager
	//---------------------------------------------------------------------

	/**
	 * This implementation delegates to the transaction object
	 * if it implements the SavepointManager interface.
	 * @throws org.springframework.transaction.NestedTransactionNotSupportedException
	 * if the underlying transaction does not support savepoints
	 * @see #getTransaction
	 * @see org.springframework.transaction.SavepointManager
	 */
	public Object createSavepoint() throws TransactionException {
		return getSavepointManager().createSavepoint();
	}

	/**
	 * This implementation delegates to the transaction object
	 * if it implements the SavepointManager interface.
	 * @see #getTransaction
	 * @see org.springframework.transaction.SavepointManager
	 */
	public void rollbackToSavepoint(Object savepoint) throws TransactionException {
		getSavepointManager().rollbackToSavepoint(savepoint);
	}

	/**
	 * This implementation delegates to the transaction object
	 * if it implements the SavepointManager interface.
	 * @see #getTransaction
	 * @see org.springframework.transaction.SavepointManager
	 */
	public void releaseSavepoint(Object savepoint) throws TransactionException {
		getSavepointManager().releaseSavepoint(savepoint);
	}

	/**
	 * Create a savepoint and hold it for the transaction.
	 * @throws org.springframework.transaction.NestedTransactionNotSupportedException
	 * if the underlying transaction does not support savepoints
	 */
	public void createAndHoldSavepoint() throws TransactionException {
		setSavepoint(getSavepointManager().createSavepoint());
	}

	/**
	 * Roll back to the savepoint that is held for the transaction.
	 */
	public void rollbackToHeldSavepoint() throws TransactionException {
		if (!hasSavepoint()) {
			throw new TransactionUsageException("No savepoint associated with current transaction");
		}
		getSavepointManager().rollbackToSavepoint(getSavepoint());
	}

	/**
	 * Release the savepoint that is held for the transaction.
	 */
	public void releaseHeldSavepoint() throws TransactionException {
		if (!hasSavepoint()) {
			throw new TransactionUsageException("No savepoint associated with current transaction");
		}
		getSavepointManager().releaseSavepoint(getSavepoint());
	}

	/**
	 * Return the underlying transaction as SavepointManager,
	 * if possible.
	 * @throws org.springframework.transaction.NestedTransactionNotSupportedException
	 * if the underlying transaction does not support savepoints
	 */
	protected SavepointManager getSavepointManager() {
		if (!isTransactionSavepointManager()) {
			throw new NestedTransactionNotSupportedException(
			    "Transaction object [" + getTransaction() + "] does not support savepoints");
		}
		return (SavepointManager) getTransaction();
	}

	/**
	 * Return whether the underlying transaction implements the
	 * SavepointManager interface.
	 * @see #getTransaction
	 * @see org.springframework.transaction.SavepointManager
	 */
	public boolean isTransactionSavepointManager() {
		return (getTransaction() instanceof SavepointManager);
	}

}
