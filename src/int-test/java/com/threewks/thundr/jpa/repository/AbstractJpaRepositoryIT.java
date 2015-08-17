package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.Action;
import com.threewks.thundr.jpa.AbstractJpaTest;
import com.threewks.thundr.jpa.Propagation;
import com.threewks.thundr.jpa.ResultAction;
import com.threewks.thundr.jpa.model.Beverage;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.*;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractJpaRepositoryIT<K, T extends Beverage> extends AbstractJpaTest<K,T> {

    public AbstractJpaRepositoryIT(T bevvie1, T bevvie2) {
        super(bevvie1, bevvie2);
    }

    @Before
    public void before() {
        super.before();


    }


}