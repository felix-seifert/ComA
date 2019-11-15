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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity(name = "coos")
@Data
public class Coo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(nullable = false)
    private Integer id;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(nullable = false, unique = true, length = 60)
    @NaturalId
    private String name;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(nullable = false, unique = true, length = 3)
    private String abbreviation;

    @OneToMany(mappedBy = "coo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Plant> plants = new HashSet<>();

    public void addPlant(Plant plant) {
        plants.add(plant);
        plant.setCoo(this);
    }

    public void removePlant(Plant plant) {
        plants.remove(plant);
        plant.setCoo(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coo)) return false;
        Coo coo = (Coo) o;
        return Objects.equals(name, coo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Coo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", abbreviation='" + abbreviation + '\'' +
                ", plants=" + (plants != null ? plants.stream().map(p -> p.getName()).collect(Collectors.joining(", ")) : null) +
                '}';
    }
}
