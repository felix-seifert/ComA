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

package com.felixseifert.coma.backend.model.dto;

import com.felixseifert.coma.backend.model.Employee_;
import com.felixseifert.coma.backend.model.PartNumberObject_;
import com.felixseifert.coma.backend.model.TaskRelease_;
import com.felixseifert.coma.backend.model.enums.CustomerNotification;
import com.felixseifert.coma.backend.model.enums.Progress;
import com.felixseifert.coma.backend.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PartNumberObjectGridDTO {

    public static final Map<String, String> FIELD_MAPPING_DTO_TO_ORIGIN = new HashMap<>();

    static {
        FIELD_MAPPING_DTO_TO_ORIGIN.put("id", PartNumberObject_.ID);
        FIELD_MAPPING_DTO_TO_ORIGIN.put("b10", PartNumberObject_.PN);
        FIELD_MAPPING_DTO_TO_ORIGIN.put("dueDate", PartNumberObject_.DUE_DATE);
        FIELD_MAPPING_DTO_TO_ORIGIN.put("createdByTeamDepartment", PartNumberObject_.CREATED_BY_TEAM_DEPARTMENT);
        FIELD_MAPPING_DTO_TO_ORIGIN.put("createdDate", PartNumberObject_.CREATED_DATE);
        FIELD_MAPPING_DTO_TO_ORIGIN.put("currentEmployeeName", Employee_.NAME);
        FIELD_MAPPING_DTO_TO_ORIGIN.put("currentEmployeeRole", TaskRelease_.CURRENT_EMPLOYEES_ROLE);
        FIELD_MAPPING_DTO_TO_ORIGIN.put("releaseCompletedAt", TaskRelease_.FINISHED_AT);
        FIELD_MAPPING_DTO_TO_ORIGIN.put("status", TaskRelease_.STATUS);
        FIELD_MAPPING_DTO_TO_ORIGIN.put("customerNotification", TaskRelease_.CUSTOMER_NOTIFICATION);
    }

    private Integer id;

    private String pn;

    private LocalDate dueDate;

    private String createdByTeamDepartment;

    private LocalDate createdDate;

    private String currentEmployeeName;

    private Role currentEmployeeRole;

    private LocalDateTime releaseCompletedAt;

    private Progress status;

    private CustomerNotification customerNotification;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartNumberObjectGridDTO)) return false;
        PartNumberObjectGridDTO that = (PartNumberObjectGridDTO) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 76;
    }
}
