package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.JpaTestSetup;
import com.threewks.thundr.jpa.model.Beverage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by kaushiksen on 12/08/2015.
 */
public class JpaRepositoryIT extends JpaTestSetup {

    @Before
    public void before() {
        super.before();
    }

    @Test
    public void shouldCreateAndReadSingleObject() {
        jpaRepository.create(bevvie1);
        Beverage localBev = new Beverage();
        localBev.setId(bevvie1.getId());
        localBev = jpaRepository.read(localBev);
        assertThat(localBev.getName(), is(bevvie1.getName()));
        assertThat(localBev.isAlcoholic(), is(bevvie1.isAlcoholic()));
    }

    @Test
    public void shouldCreateAndUpdateSingleObject() {
        jpaRepository.create(bevvie1);
        Beverage localBev = new Beverage();
        localBev.setId(bevvie1.getId());
        localBev = jpaRepository.read(localBev);
        assertThat(localBev.getName(), is(bevvie1.getName()));
        assertThat(localBev.isAlcoholic(), is(bevvie1.isAlcoholic()));

        localBev.setName("Wine");
        Beverage updatedBev = jpaRepository.update(localBev);
        assertThat(updatedBev.getName(), is("Wine"));

        //Validate that the updated beverage has been stored in the database
        Beverage checkBevUpdated = new Beverage();
        checkBevUpdated.setId(bevvie1.getId());
        checkBevUpdated = jpaRepository.read(checkBevUpdated);
        assertThat(checkBevUpdated.getName(), is("Wine"));
    }

    @Test
    public void shouldReturnAccurateCount() {
        jpaRepository.create(bevvie1);
        jpaRepository.create(bevvie2);
        Long count = jpaRepository.count();
        assertThat(count, is(2l));
    }

    @Test
    public void shouldDeleteSingleObject() {
        jpaRepository.create(bevvie1);
        jpaRepository.create(bevvie2);
        jpaRepository.delete(bevvie1);

        Beverage deletedBev = jpaRepository.read(bevvie1);
        Beverage remainingBev = jpaRepository.read(bevvie2);

        assertThat(deletedBev, is(nullValue()));
        assertThat(remainingBev.getId(), is (bevvie2.getId()));
    }

    @Test
    public void shouldReturnListOfAllEntitiesOfType() {
        jpaRepository.create(bevvie1);
        jpaRepository.create(bevvie2);

        List<Beverage> beverageList = jpaRepository.readAll();
        assertThat(beverageList.size(), is(2));
        assertThat(beverageList.get(0).getId(), is(bevvie1.getId()));
        assertThat(beverageList.get(1).getId(), is(bevvie2.getId()));
    }
}