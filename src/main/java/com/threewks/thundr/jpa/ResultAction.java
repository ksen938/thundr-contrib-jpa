package com.threewks.thundr.jpa;

import javax.persistence.EntityManager;

public interface ResultAction<R> {
    public R run(EntityManager em);
}
