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

    public IdClassCompoundKeyEntity() {
    }

    public IdClassCompoundKeyEntity(String name) {
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
}
