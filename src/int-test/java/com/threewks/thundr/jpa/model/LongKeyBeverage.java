package com.threewks.thundr.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * Created by kaushiksen on 17/08/2015.
 */
@Entity
@Table(name = "LongBeverage")
public class LongKeyBeverage extends Beverage<Long> {

    public LongKeyBeverage(String name, boolean alcoholic) {
        this.name = name;
        this.alcoholic = alcoholic;
    }

    @Id
    @Column(name = "id")
    private Long id = Math.round(Math.random() * 1000);

    public Long getId() {
        return id;
    }
}
