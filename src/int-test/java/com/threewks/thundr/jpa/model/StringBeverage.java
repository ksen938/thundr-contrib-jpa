package com.threewks.thundr.jpa.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by kaushiksen on 17/08/2015.
 */

@Entity
@Table(name = "StringBeverage")
public class StringBeverage {

    public StringBeverage() {
    }

    public StringBeverage(String name, boolean alcoholic) {
        this.name = name;
        this.alcoholic = alcoholic;
    }

    @Id
    @Column(name = "id")
    public String id = UUID.randomUUID().toString();

    @Column(name = "name", nullable = false)
    public String name = "";

    @Column(name = "alcoholic")
    public boolean alcoholic = false;

    @ManyToOne
    @JoinColumn(name = "long_beverage_id")
    private LongBeverage longbeverage;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LongBeverage getLongBeverage() {
        return longbeverage;
    }

    public void setLongBeverage(LongBeverage longBeverage) {
        this.longbeverage = longBeverage;
    }
}
