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

/**
 * JPA transactional propagation values. The behaviours driven by these are as per the JPA spec (Spring doco below).
 * @see <a href = "http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/transaction/annotation/Propagation.html">Spring Documentation for Transaction Propagations</a>
 */
public enum Propagation {
    /**
     * Supports a current transaction, throw an exception if none exists.
     */
    Mandatory,

    /**
     * Execute non-transactionally, throw an exception if a transaction exists.
     */
    Never,

    //NotSupported,

    /**
     * Support a current transaction, create a new one if none exists.
     */
    Required,

    /**
     * Create a new transaction, and suspend the current transaction if one exists.
     */
    RequiresNew,

    /**
     * Support a current transaction, execute non-transactionally if none exists.
     */
    Supports
}
