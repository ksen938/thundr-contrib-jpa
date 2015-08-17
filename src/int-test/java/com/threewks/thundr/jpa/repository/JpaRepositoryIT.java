package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.JpaBaseTest;
import com.threewks.thundr.jpa.Propagation;
import com.threewks.thundr.jpa.model.Beverage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by kaushiksen on 12/08/2015.
 */
public class JpaRepositoryIT extends JpaBaseTest {

    @Before
    public void before() {
        super.before();
    }

    @Test
    public void shouldCreateAndReadSingleEntity() {
        deleteTestData();

        createBeverages();
        Beverage localBev;
        localBev = jpaRepository.read(bevvie1.getId());

        jpa.getExistingEntityManager().refresh(localBev);

        assertThat(localBev.getName(), is(bevvie1.getName()));
        assertThat(localBev.isAlcoholic(), is(bevvie1.isAlcoholic()));
        jpa.dispose();
    }

    @Test
    public void shouldUpdateSingleEntity() {
        deleteTestData();

        createBeverages();

        jpa.begin(Propagation.Required);

        Beverage localBev = jpaRepository.read(bevvie1.getId());

        localBev.setName("Water");
        localBev.setAlcoholic(false);
        Beverage updatedBev = jpaRepository.update(localBev);
        jpa.commit();

        checkUpdated(updatedBev);

        jpa.dispose();
    }

    protected void checkUpdated(Beverage beverage) {
        assertThat(beverage.getName(), is("Water"));
        assertThat(beverage.isAlcoholic(), is(false));
    }

    @Test
    public void shouldUpdateMultipleEntities() {
        deleteTestData();
        createBeverages();

        jpa.begin(Propagation.Required);
        Beverage localBev1 = jpaRepository.read(bevvie1.getId());
        Beverage localBev2 = jpaRepository.read(bevvie2.getId());

        localBev1.setName("Water");
        localBev1.setAlcoholic(false);
        localBev2.setName("Water");
        localBev2.setAlcoholic(false);

        Beverage[] beverages = new Beverage[2];
        beverages[0] = localBev1;
        beverages[1] = localBev2;

        List<Beverage> beverages1 = jpaRepository.update(beverages);
        List<Beverage> beverages2 = jpaRepository.update(Arrays.asList(beverages));

        jpa.commit();

        for (Beverage beverage: beverages1) {
            checkUpdated(beverage);
        }

        for (Beverage beverage: beverages2) {
            checkUpdated(beverage);
        }

        jpa.dispose();
    }


    @Test
    public void shouldReturnAccurateCount() {
        deleteTestData();

        createBeverages();

        Long count = jpaRepository.count();
        assertThat(count, is(2l));
        jpa.dispose();
    }

    @Test
    public void shouldDeleteSingleEntity() {
        startDelete();
        jpaRepository.delete(bevvie1);
        finishDelete();
    }

    @Test
    public void shouldDeleteSingleEntityByKey() {
        startDelete();
        jpaRepository.deleteByKey(bevvie1.getId());
        finishDelete();
    }

    protected void startDelete() {
        deleteTestData();
        createBeverages();
        jpa.begin(Propagation.Required);
    }

    protected void finishDelete() {
        jpa.commit();
        Beverage deletedBev = jpaRepository.read(bevvie1.getId());
        Beverage remainingBev = jpaRepository.read(bevvie2.getId());
        assertThat(deletedBev, is(nullValue()));
        assertThat(remainingBev.getId(), is(bevvie2.getId()));
    }

    @Test
    public void shouldReadMultipleEntities() {
        deleteTestData();

        createBeverages();

        List<Beverage> beverageList1 = jpaRepository.read(bevvie1.getId(), bevvie2.getId());

        List<String> beverageListKeys = new ArrayList<>();

        beverageListKeys.add(bevvie1.getId());
        beverageListKeys.add(bevvie2.getId());

        List<Beverage> beverageList2 = jpaRepository.read(beverageListKeys);

        containsBeverages(beverageList1);
        containsBeverages(beverageList2);

        jpa.dispose();
    }

    protected void createBeverages() {
        jpa.begin(Propagation.Required);
        jpaRepository.create(bevvie1);
        jpaRepository.create(bevvie2);
        jpa.commit();
    }

    @Test
    public void shouldCreateMultipleEntities() {
        deleteTestData();
        List<Beverage> beverages = new ArrayList<>();
        beverages.add(bevvie1);
        beverages.add(bevvie2);

        jpa.begin(Propagation.Required);
        jpaRepository.create(beverages);
        jpa.commit();
        containsBeverages(beverages);

        deleteTestData();

        jpa.begin(Propagation.Required);
        jpaRepository.create(bevvie1, bevvie2);
        jpa.commit();
        containsBeverages(beverages);

        deleteTestData();
    }
}