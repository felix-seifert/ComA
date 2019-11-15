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

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity(name = "plants")
@Data
public class Plant implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(nullable = false)
    private Integer id;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(nullable = false, length = 60)
    private String name;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coo_id", nullable = false)
    private Coo coo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plant)) return false;
        Plant plant = (Plant) o;
        return id != null && id.equals(plant.id);
    }

    @Override
    public int hashCode() {
        return 57;
    }
}
