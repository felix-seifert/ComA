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

import com.felixseifert.coma.backend.model.enums.*;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks_release")
@Getter
@Setter
public class TaskRelease extends Task<TaskRelease> {

    private static final long serialVersionUID = 1L;

    public static final String[] FIELDS_TO_INITIALISE = {TaskRelease_.CURRENT_RESPONSIBLE_EMPLOYEE,
            TaskRelease_.REMAINING_STEPS, TaskRelease_.COMPLETED_TASK_STEPS};

    @OneToOne(mappedBy = "taskRelease", fetch = FetchType.LAZY)
    @JoinColumn(name = "part_number_object_id")
    private PartNumberObject partNumberObject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_employee_id")
    private Employee currentResponsibleEmployee;

    @Convert(converter = RoleConverter.class)
    @Column(name = "responsible_employee_role", columnDefinition = "smallint")
    private Role currentEmployeesRole;

    @ElementCollection
    @CollectionTable(name = "roles_for_release_flow", joinColumns = @JoinColumn(name = "task_release_id"))
    @OrderColumn(name = "order_id")
    @Convert(converter = RoleConverter.class)
    @Column(name = "role", columnDefinition = "smallint")
    private List<Role> remainingSteps = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "order_id", columnDefinition = "smallint")
    List<TaskStepRelease> completedTaskSteps = new ArrayList<>();

    @Column(columnDefinition = "smallint")
    @Convert(converter = ProgressConverter.class)
    private Progress status;

    @Column(columnDefinition = "smallint")
    @Convert(converter = CustomerNotificationConverter.class)
    private CustomerNotification customerNotification;

    private String rejectionReasonOrFollowUp;

    @Transient
    private boolean stepFinished = false;

    public void addTaskStep(TaskStepRelease taskStepRelease) {
        completedTaskSteps.add(taskStepRelease);
        taskStepRelease.setTask(this);
    }

    public void removeTaskStep(TaskStepRelease taskStepRelease) {
        completedTaskSteps.remove(taskStepRelease);
        taskStepRelease.setTask(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskRelease)) return false;
        TaskRelease that = (TaskRelease) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return 85;
    }
}
