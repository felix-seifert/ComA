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

import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.model.enums.RoleConverter;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity(name = "employees")
@Getter
@Setter
public class Employee extends Auditable<String> {

    public static final String[] FIELDS_TO_INITIALISE = {Employee_.ROLES, Employee_.BUSINESS_UNITS_AS_PM};

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(nullable = false, name = "id")
    private Integer id;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(unique = true, nullable = false, length = 100)
    private String emailAddress;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String team;

    @Column(length = 100)
    private String location;

    @ElementCollection
    @CollectionTable(name = "roles_of_employees", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(columnDefinition = "smallint")
    @Convert(converter = RoleConverter.class)
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(mappedBy = "productManagers")
    private Set<BusinessUnit> businessUnitsAsPM = new HashSet<>();

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public void addBusinessUnitAsPM(BusinessUnit businessUnit) {
        businessUnitsAsPM.add(businessUnit);
        businessUnit.getProductManagers().add(this);
    }

    public void removeBusinessUnitAsPM(BusinessUnit businessUnit) {
        businessUnitsAsPM.remove(businessUnit);
        businessUnit.getProductManagers().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return id != null && id.equals(employee.id);
    }

    @Override
    public int hashCode() {
        return 49;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", emailAddress='" + emailAddress + '\'' +
                ", name='" + name + '\'' +
                ", roles='" + roles + '\'' +
                ", businessUnitsAsPM='" + (businessUnitsAsPM != null ? businessUnitsAsPM.stream().map(BusinessUnit::getName).collect(Collectors.joining(", ")) : "") + '\'' +
                '}';
    }
}
