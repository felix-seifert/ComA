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

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity(name = "business_units")
@Getter
@Setter
public class BusinessUnit extends Auditable<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String[] FIELDS_TO_INITIALISE = {BusinessUnit_.PRODUCT_MANAGERS};

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(nullable = false, name = "id")
    private Integer id;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "business_unit_employee",
            joinColumns = @JoinColumn(name = "business_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "product_manager_id"))
    private Set<Employee> productManagers = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessUnit)) return false;
        BusinessUnit that = (BusinessUnit) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "BusinessUnit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", productManagers='" + (productManagers != null ? productManagers.stream().map(Employee::getName).collect(Collectors.joining(" ,")) : "") + '\'' +
                '}';
    }
}
