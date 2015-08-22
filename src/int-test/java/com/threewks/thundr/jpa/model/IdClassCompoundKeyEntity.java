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
package com.threewks.thundr.jpa.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by kaushiksen on 18/08/2015.
 */

@Entity
@Table(name = "compound_key_entity")
@IdClass(value = CompoundKeyEntityId.class)
public class IdClassCompoundKeyEntity implements Serializable {

    @Id
    @Column(name = "pk_1")
    private Long pk1 = Math.round(Math.random() * 1000);
    @Id
    @Column(name = "pk_2")
    private Long pk2 = Math.round(Math.random() * 1000);

    @Column(name = "name")
    private String name;

    @Column(name = "alcoholic")
    private boolean alcoholic;

    public IdClassCompoundKeyEntity() {
    }

    public IdClassCompoundKeyEntity(String name) {
        this.name = name;
    }

    public IdClassCompoundKeyEntity(boolean alcoholic, String name) {
        this.alcoholic = alcoholic;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPk1() {
        return pk1;
    }

    public void setPk1(Long pk1) {
        this.pk1 = pk1;
    }

    public Long getPk2() {
        return pk2;
    }

    public void setPk2(Long pk2) {
        this.pk2 = pk2;
    }

    public CompoundKeyEntityId getId() {
        return new CompoundKeyEntityId(pk1,pk2);
    }

    public boolean isAlcoholic() {
        return alcoholic;
    }

    public void setAlcoholic(boolean alcoholic) {
        this.alcoholic = alcoholic;
    }
}
