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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LongBeverage")
public class LongBeverage {

    public LongBeverage() {
    }

    public LongBeverage(String name, boolean alcoholic) {
        this.name = name;
        this.alcoholic = alcoholic;
    }

    @Id
    @Column(name = "long_bev_id")
    private Long id = Math.round(Math.random() * 1000);

    @Column(name = "name", nullable = false)
    private String name = "";

    @Column(name = "alcoholic")
    private boolean alcoholic = false;

    @OneToMany(mappedBy = "longbeverage", fetch = FetchType.EAGER)
    private List<StringBeverage> stringBeverages = new ArrayList<>();

    public boolean isAlcoholic() {
        return alcoholic;
    }

    public void setAlcoholic(boolean alcoholic) {
        this.alcoholic = alcoholic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<StringBeverage> getStringBeverages() {
        return stringBeverages;
    }

    public void setStringBeverages(List<StringBeverage> stringBeverages) {
        this.stringBeverages = stringBeverages;
    }
    public LongBeverage withStringBeverage(StringBeverage bev) {
        this.stringBeverages.add(bev);
        return this;
    }
}
