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
    private CompoundKeyEntityId id = new CompoundKeyEntityId();

    @Column(name = "name")
    private String name;

    public EmbeddedIdCompoundKeyEntity() {
    }

    public EmbeddedIdCompoundKeyEntity(String name) {
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
