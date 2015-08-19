package com.threewks.thundr.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
    public Long id = Math.round(Math.random() * 1000);

    @Column(name = "name", nullable = false)
    private String name = "";

    @Column(name = "alcoholic")
    private boolean alcoholic = false;

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
