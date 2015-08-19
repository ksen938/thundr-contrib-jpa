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
