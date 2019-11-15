/*
 * Copyright 2019 Felix Seifert <mail@felix-seifert.com> (https://felix-seifert.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.felixseifert.coma.backend.model;

import lombok.Data;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

@Entity(name = "customers")
@Data
public class Customer {

    @Id
    @NaturalId
    @Column(nullable = false, length = 10)
    private String code;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(nullable = false, length = 60)
    private String name;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(nullable = false, length = 60)
    private String deliveryLocation;

    @Column(length = 3)
    private String currency;

    @OneToMany(mappedBy = "customer")
    private Set<PartNumberObject> partNumberObjects;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return Objects.equals(code, customer.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", deliveryLocation='" + deliveryLocation + '\'' +
                ", currency='" + currency + '\'' +
                ", partNumberObjects=" + (partNumberObjects != null ? partNumberObjects.stream().map(PartNumberObject::getId) : null) +
                '}';
    }
}
