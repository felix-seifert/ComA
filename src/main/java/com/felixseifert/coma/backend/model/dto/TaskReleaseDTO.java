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

import com.felixseifert.coma.backend.model.enums.Role;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskReleaseDTO {       // Also used in coma-notification microservice. Attention when changing.

    private Long id;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private String currentEmployeesName;

    private String currentEmployeesEmailAddress;

    private String currentEmployeesLocation;

    private String currentEmployeesTeam;

    private String currentEmployeesRole;

    private LocalDateTime lastStepFinishedAt;

    public TaskReleaseDTO(Long id, LocalDateTime startedAt, LocalDateTime finishedAt, String currentEmployeesName,
                          String currentEmployeesEmailAddress, String currentEmployeesLocation,
                          String currentEmployeesTeam, Role currentEmployeesRole, LocalDateTime lastStepFinishedAt) {
        this.id = id;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.currentEmployeesName = currentEmployeesName;
        this.currentEmployeesEmailAddress = currentEmployeesEmailAddress;
        this.currentEmployeesLocation = currentEmployeesLocation;
        this.currentEmployeesTeam = currentEmployeesTeam;
        this.currentEmployeesRole = currentEmployeesRole.getName();
        this.lastStepFinishedAt = lastStepFinishedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskReleaseDTO)) return false;
        TaskReleaseDTO that = (TaskReleaseDTO) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 24;
    }
}
