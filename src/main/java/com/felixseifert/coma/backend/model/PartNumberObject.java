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
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

@Entity
@Table(name = "part_numbers")
@Getter
@Setter
public class PartNumberObject extends Auditable<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String[] EMPLOYEE_FIELDS = Arrays.stream(Role.values())
            .filter(role -> role.getPnVariable() != null).map(Role::getPnVariable).toArray(String[]::new);

    private static final String[] OTHER_FIELDS = {PartNumberObject_.CHANNEL_PROFILES, PartNumberObject_.CUSTOMER,
            PartNumberObject_.BUSINESS_UNIT, PartNumberObject_.LOBS, PartNumberObject_.TASK_RELEASE};

    public static final String[] FIELDS_TO_INITIALISE = ArrayUtils.addAll(EMPLOYEE_FIELDS, OTHER_FIELDS);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(nullable = false, name = "id")
    private Integer id;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(nullable = false, length = 10)
    private String pn;

    @Column(length = 3)
    private String idx;

    private String productDescription;

    @Column(length = 20)
    private String productCode;

    @Column(length = 13)
    private String completeSourcePn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "create_by_employee_id")
    private Employee createdByEmployee;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(length = 100)
    private String createdByTeamDepartment;

    @ElementCollection
    @CollectionTable(name = "request_types", joinColumns = @JoinColumn(name = "part_number_object_id"))
    @Column(name = "request_type", length = 100)
    private Set<String> requestType;

    @ElementCollection
    @CollectionTable(name = "channel_profiles", joinColumns = @JoinColumn(name = "part_number_object_id"))
    @Column(name = "channel_profile", length = 100)
    private Set<String> channelProfiles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_code")
    private Customer customer;

    @Column(length = 20)
    private String customerPartNumber;

    @Column(columnDefinition = "Date")
    private LocalDate dueDate;

    @Column(columnDefinition = "Date")
    private LocalDate startOfProduction;

    @Column(columnDefinition = "Date")
    private LocalDate endOfProduction;

    @Column(length = 13)
    private String predecessor;

    @Column(length = 60)
    private String cooName;

    @Column(length = 3)
    private String cooAbbreviation;

    @Column(length = 60)
    private String plant;

    @Column(length = 20)
    private String plantCode;

    private Double cataloguePrice;

    @Column(columnDefinition = "boolean default false")
    private Boolean transferPrice;

    @Column(columnDefinition = "boolean default false")
    private Boolean ppc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_name")
    private BusinessUnit businessUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_manager_id")
    private Employee productManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_specialist_id")
    private Employee productSpecialist;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private TaskRelease taskRelease;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PartNumberLob lobs;

    @Transient
    private String comments;        // Filled when PartNumberObjectService.getPartNumberObjectById(int id) is called

    public PartNumberObject() {
        this.setTaskRelease(new TaskRelease());     // Every PartNumberObject MUST have a TaskRelease
    }

    public void setTaskRelease(TaskRelease taskRelease) {
        if(taskRelease == null) {
            if(this.taskRelease != null) {
                this.taskRelease.setPartNumberObject(null);
            }
        }
        else {
            taskRelease.setPartNumberObject(this);
        }
        this.taskRelease = taskRelease;
    }

    public void setLobs(PartNumberLob lobs) {
        if(lobs == null) {
            if(this.lobs != null) {
                this.lobs.setPartNumberObject(null);
            }
        }
        else {
            lobs.setPartNumberObject(this);
        }
        this.lobs = lobs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartNumberObject)) return false;
        PartNumberObject that = (PartNumberObject) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 76;
    }

    @Override
    public String toString() {
        return "PartNumberObject{" +
                "id=" + id +
                ", pn='" + pn + '\'' +
                ", idx='" + idx + '\'' +
                ", completeSourcePn='" + completeSourcePn + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", createdDate='" + getCreatedDate() + '\'' +
                (createdByEmployee != null ? ", createdByEmployee='" + createdByEmployee.getEmailAddress() + '\'' : "") +
                ", createdByTeamDepartment='" + createdByTeamDepartment + '\'' +
                ", dueDate=" + dueDate +
                ", startOfProduction=" + startOfProduction +
                ", endOfProduction=" + endOfProduction +
                ", channelProfiles=" + channelProfiles +
                ", customer=" + (customer != null ? customer.getCode() : null) +
                ", customerPartNumber='" + customerPartNumber + '\'' +
                ", predecessor='" + predecessor + '\'' +
                ", cooName='" + cooName + '\'' +
                ", cooAbbreviation='" + cooAbbreviation + '\'' +
                ", plant='" + plant + '\'' +
                ", plantCode='" + plantCode + '\'' +
                ", cataloguePrice='" + cataloguePrice + '\'' +
                ", transferPrice='" + transferPrice + '\'' +
                ", ppc='" + ppc + '\'' +
                (businessUnit != null ? ", businessUnit='" + businessUnit.getName() + '\'' : "") +
                (productManager != null ? ", productManager='" + productManager.getEmailAddress() + '\'' : "") +
                (productSpecialist != null ? ", productSpecialist='" + productSpecialist.getEmailAddress() + '\'' : "") +
                ", taskRelease='" + taskRelease + '\'' +
                ", lobs='" + (lobs != null ? lobs.getId() : null) + '\'' +
                '}';
    }
}
