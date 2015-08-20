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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by kaushiksen on 18/08/2015.
 */
@Embeddable
public class CompoundKeyEntityId implements Serializable {
    Long pk1 =Math.round(Math.random() * 1000);
    Long pk2 = Math.round(Math.random() * 1000);

    public CompoundKeyEntityId() {
    }

    public CompoundKeyEntityId(Long pk1, Long pk2) {
        this.pk1 = pk1;
        this.pk2 = pk2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CompoundKeyEntityId that = (CompoundKeyEntityId) o;

        return new EqualsBuilder()
                .append(pk1, that.pk1)
                .append(pk2, that.pk2)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(pk1)
                .append(pk2)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("pk1", pk1)
                .append("pk2", pk2)
                .toString();
    }

    public Long getPk1() {
        return pk1;
    }

    public Long getPk2() {
        return pk2;
    }
}
