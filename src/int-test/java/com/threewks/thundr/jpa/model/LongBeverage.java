package com.threewks.thundr.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by kaushiksen on 17/08/2015.
 */
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
    @Column(name = "id")
    private Long id = Math.round(Math.random() * 1000);

    @Column(name = "name", nullable = false)
    public String name = "";

    @Column(name = "alcoholic")
    public boolean alcoholic = false;

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
}
