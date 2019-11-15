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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "task_steps_release")
@Getter
@Setter
public class TaskStepRelease extends TaskStep<TaskStepRelease> {

    private static final long serialVersionUID = 1L;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(length = 50)
    private String roleOfEmployee;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @Column(length = 100)
    @Setter(AccessLevel.NONE)
    private String teamOfEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id")
    private Employee assignedEmployee;

    @NotNull(message = ConstraintViolationMessages.MUST_BE_SPECIFIED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_release_id")
    private TaskRelease task;

    @Column(name = "order_id", columnDefinition = "smallint")
    @Setter(AccessLevel.NONE)
    private Integer orderId;        // Used for ordering task steps in its task

    @PrePersist
    @PreUpdate
    private void prepareTaskStepRelease() {
        if(task != null) {
            orderId = task.getCompletedTaskSteps().indexOf(this);
        }

        if(assignedEmployee != null && StringUtils.isBlank(teamOfEmployee)) {
            teamOfEmployee = assignedEmployee.getTeam();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskStepRelease)) return false;
        TaskStepRelease that = (TaskStepRelease) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return 69;
    }
}
