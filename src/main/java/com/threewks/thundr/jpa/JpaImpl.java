/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.jpa;


import com.threewks.thundr.jpa.exception.JpaException;
import com.threewks.thundr.logger.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.metamodel.Metamodel;
import java.util.Deque;
import java.util.LinkedList;

// TODO - Shutdown hooks when module.stop is called
public class JpaImpl implements Jpa, JpaUnsafe {
    private EntityManagerFactory entityManagerFactory;
    private ThreadLocal<Deque<EntityManager>> threadLocal = new ThreadLocal<>();

    public JpaImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Metamodel getMetamodel() {
        return entityManagerFactory.getMetamodel();
    }

    @Override
    public void run(Action action) {
        run(Propagation.Required, action);
    }

    @Override
    public void run(Propagation propagation, final Action action) {
        run(propagation, new ResultAction<Void>() {
            @Override
            public Void run(EntityManager em) {
                action.run(em);
                return null;
            }
        });
    }

    @Override
    public <R> R run(ResultAction<R> action) {
        return run(Propagation.Required, action);
    }

    @Override
    public <R> R run(Propagation propagation, ResultAction<R> action) {
        validateInternalState();
        EntityManager em = getExistingEntityManager();
        boolean ownsEntityManager = em == null || (Propagation.RequiresNew == propagation && em.getTransaction().isActive());
        if (ownsEntityManager) {
            em = createNewEntityManager();
        }
        EntityTransaction transaction = em.getTransaction();
        boolean ownsTransaction = participateInTransactionState(transaction, propagation);
        try {
            R result = action.run(em);
            commitOrRollback(transaction, ownsTransaction);
            return result;
        } catch (RuntimeException e) {
            ensureRollback(transaction, ownsTransaction);
            throw e;
        } finally {
            if (ownsEntityManager) {
                disposeOfEntityManager(em);
            }
        }
    }

    @Override
    public void startTransaction(Propagation propagation) {
        validateInternalState();
        EntityManager em = getExistingEntityManager();
        boolean ownsEntityManager = em == null || (Propagation.RequiresNew == propagation && em.getTransaction().isActive());
        if (ownsEntityManager) {
            em = createNewEntityManager();
        }
        EntityTransaction transaction = em.getTransaction();
        participateInTransactionState(transaction, propagation);
    }

    @Override
    public void finishTransaction() {
        EntityManager em = getExistingEntityManager();
        if (em == null || !em.getTransaction().isActive()) {
            Logger.warn("Unable to finish transaction. There is no open transaction (ignoring)");
            return;
        }
        EntityTransaction transaction = em.getTransaction();
        try {
            commitOrRollback(transaction, true);
        } catch (RuntimeException e) {
            ensureRollback(transaction, true);
            throw e;
        } finally {
            disposeOfEntityManager(em);
        }
    }

    @Override
    public void rollbackTransaction() {
        EntityManager em = getExistingEntityManager();
        if (em == null || !em.getTransaction().isActive()) {
            Logger.warn("Unable to rollback transaction. There is no open transaction (ignoring)");
            return;
        }
        EntityTransaction transaction = em.getTransaction();
        try {
            ensureRollback(transaction, true);
        } finally {
            disposeOfEntityManager(em);
        }
    }

    protected void ensureRollback(EntityTransaction transaction, boolean ownsTransaction) {
        transaction.setRollbackOnly();
        if (ownsTransaction) {
            transaction.rollback();
        }
    }

    private void validateInternalState() {
        if (!entityManagerFactory.isOpen()) {
            throw new JpaException("%s is not open - cannot perform any JPA actions", EntityManagerFactory.class.getSimpleName());
        }
    }

    public EntityManager getExistingEntityManager() {
        Deque<EntityManager> current = threadLocal.get();
        if (current == null) {
            current = new LinkedList<>();
            threadLocal.set(current);
        }
        return current.peek();
    }

    public EntityManager createNewEntityManager() {
        EntityManager em = entityManagerFactory.createEntityManager();
        Deque<EntityManager> current = threadLocal.get();
        current.push(em);
        return em;
    }

    public boolean participateInTransactionState(EntityTransaction transaction, Propagation propagation) {
        if (transaction.isActive()) {
            if (Propagation.Never == propagation) {
                throw new JpaException("Transaction already underway");
            }
        } else {
            if (Propagation.Mandatory == propagation) {
                throw new JpaException("Existing transaction required - none exists");
            }
            if (Propagation.Required == propagation || Propagation.RequiresNew == propagation) {
                transaction.begin();
                return true;
            }
        }
        return false;
    }

    private void disposeOfEntityManager(EntityManager em) {
        try {
            Deque<EntityManager> current = threadLocal.get();
            current.pop();
            em.close();
            threadLocal.remove();
        } catch (Exception e) {
            Logger.error("Failed to close EntityManager: %s\n%s", e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
    }

    protected void commitOrRollback(EntityTransaction transaction, boolean ownsTransaction) {
        if (ownsTransaction) {
            if (transaction.getRollbackOnly()) {
                transaction.rollback();
            } else {
                transaction.commit();
            }
        }
    }
}
