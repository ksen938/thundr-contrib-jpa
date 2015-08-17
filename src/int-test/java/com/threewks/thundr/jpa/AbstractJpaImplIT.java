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

import com.threewks.thundr.jpa.model.Beverage;
import com.threewks.thundr.jpa.model.LongKeyBeverage;
import com.threewks.thundr.jpa.model.StringKeyBeverage;
import com.threewks.thundr.jpa.repository.JpaRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractJpaImplIT<K, T extends Beverage> extends AbstractJpaTest<K, T> {

    public AbstractJpaImplIT(T bevvie1, T bevvie2) {
        super(bevvie1, bevvie2);
    }

    @Before
    public void before() {
        super.before();
    }


}
