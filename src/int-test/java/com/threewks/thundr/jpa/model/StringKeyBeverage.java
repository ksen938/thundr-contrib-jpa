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
@Table(name = "StringBeverage")
public class StringKeyBeverage extends Beverage<String> {

    public StringKeyBeverage(String name, boolean alcoholic) {
        this.name = name;
        this.alcoholic = alcoholic;
    }

    @Id
    @Column(name = "id")
    private String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }
}
