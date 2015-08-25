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
package com.threewks.thundr.jpa.intercept;

import com.threewks.thundr.jpa.Jpa;
import com.threewks.thundr.jpa.JpaUnsafe;
import com.threewks.thundr.route.controller.Interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by kaushiksen on 25/08/2015.
 */
public class TransactionalInterceptor implements Interceptor<Transactional> {

    private final JpaUnsafe jpa;

    public TransactionalInterceptor(JpaUnsafe jpa) {
        this.jpa = jpa;
    }

    @Override
    public <T> T before(Transactional annotation, HttpServletRequest req, HttpServletResponse resp) {
        jpa.startTransaction(annotation.value());
        return null;
    }

    @Override
    public <T> T after(Transactional annotation, Object view, HttpServletRequest req, HttpServletResponse resp) {
        jpa.finishTransaction();
        return null;
    }

    @Override
    public <T> T exception(Transactional annotation, Exception e, HttpServletRequest req, HttpServletResponse resp) {
        jpa.rollbackTransaction();
        return null;
    }
}
