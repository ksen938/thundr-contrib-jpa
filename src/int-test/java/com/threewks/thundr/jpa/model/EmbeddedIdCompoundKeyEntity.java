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
@Table(name = "embedded_id_compound_key_entity")
public class EmbeddedIdCompoundKeyEntity implements Serializable {

    @EmbeddedId
    public CompoundKeyEntityId id = new CompoundKeyEntityId();

    @Column(name = "name")
    private String name;

    @Column(name = "alcoholic")
    private boolean alcoholic;

    public EmbeddedIdCompoundKeyEntity() {
    }

    public EmbeddedIdCompoundKeyEntity(String name) {
        this.name = name;
    }

    public EmbeddedIdCompoundKeyEntity(boolean alcoholic, String name) {
        this.alcoholic = alcoholic;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CompoundKeyEntityId getId() {
        return id;
    }

    public void setId(CompoundKeyEntityId id) {
        this.id = id;
    }

    public boolean isAlcoholic() {
        return alcoholic;
    }

    public void setAlcoholic(boolean alcoholic) {
        this.alcoholic = alcoholic;
    }
}
