package com.threewks.thundr.jpa;

import javax.persistence.EntityManager;

public interface Action {
    public void run(EntityManager em);
}
