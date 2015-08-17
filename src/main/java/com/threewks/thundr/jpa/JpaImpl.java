package com.threewks.thundr.jpa;


import com.threewks.thundr.jpa.exception.JpaException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnitUtil;
import java.util.Deque;
import java.util.LinkedList;

public class JpaImpl implements Jpa {
    private EntityManagerFactory entityManagerFactory;
    private PersistenceUnitUtil persistenceUnitUtil;
    private ThreadLocal<Deque<EntityManager>> threadLocal = new ThreadLocal<>();

    public JpaImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        persistenceUnitUtil = entityManagerFactory.getPersistenceUnitUtil();
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
        boolean needsNew = em == null || (Propagation.RequiresNew == propagation && em.getTransaction().isActive());
        if (needsNew) {
            em = createNewEntityManager();
        }
        EntityTransaction transaction = configureTransaction(em, propagation);
        try {
            return action.run(em);
        } catch (RuntimeException e) {
            transaction.setRollbackOnly();
            throw e;
        } finally {
            if (needsNew) {
                disposeOfTransaction(em, propagation);
                disposeOfEntityManager(em);
            }
        }
    }

    private void validateInternalState() {
        if(!entityManagerFactory.isOpen()){
            throw new JpaException("%s is not open - cannot perform any JPA actions", EntityManagerFactory.class.getSimpleName());
        }
    }

    public EntityManager getExistingEntityManager() {
        Deque<EntityManager> current = threadLocal.get();
        if (current == null) {
            current = new LinkedList<EntityManager>();
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

    public EntityManager getOrCreateEntityManager(Propagation propagation) {
        EntityManager em = getExistingEntityManager();
        boolean needsNew = em == null || (Propagation.RequiresNew == propagation && em.getTransaction().isActive());
        if (needsNew) {
            em = createNewEntityManager();
        }
        return em;
    }

    public EntityTransaction configureTransaction(EntityManager em, Propagation propagation) {
        EntityTransaction transaction = em.getTransaction();
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
            }
        }
        return transaction;
    }

    private void disposeOfEntityManager(EntityManager em) {
        Deque<EntityManager> current = threadLocal.get();
        current.pop();
        em.close();
    }


    protected void disposeOfTransaction(EntityManager em, Propagation propagation) {
        EntityTransaction transaction = em.getTransaction();

        if (transaction.isActive()) {
            if (Propagation.RequiresNew == propagation || Propagation.Required == propagation || Propagation.Mandatory == propagation) {
                em.getTransaction().commit();
            }
        }
    }

    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return persistenceUnitUtil;
    }

    @Override
    public void begin(Propagation propagation) {
        EntityManager em = getOrCreateEntityManager(propagation);
        EntityTransaction et = em.getTransaction();

        if (!et.isActive()) {
            em.getTransaction().begin();
        }
    }

    @Override
    public void commit() {
        EntityTransaction et = getExistingEntityManager().getTransaction();
        et.commit();
    }

    @Override
    public void rollback() {
        EntityTransaction et = getExistingEntityManager().getTransaction();
        et.rollback();
    }

    @Override
    public void dispose() {
        disposeOfEntityManager(getExistingEntityManager());
    }
}
