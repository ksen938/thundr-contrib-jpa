package com.threewks.thundr.jpa.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by kaushiksen on 18/08/2015.
 */

@Entity
@Table(name = "compound_key_entity")
public class CompoundKeyEntity implements Serializable {

    /*@Id
    @Column(name = "pk_1")
    private Long pk1 = Math.round(Math.random() * 1000);
    @Id
    @Column(name = "pk_2")
    private Long pk2 = Math.round(Math.random() * 1000);*/

    @EmbeddedId
    private CompoundKeyEntityId id = new CompoundKeyEntityId();

    @Column(name = "name")
    private String name;

    public CompoundKeyEntity() {
    }

    public CompoundKeyEntity(String name) {
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
}
