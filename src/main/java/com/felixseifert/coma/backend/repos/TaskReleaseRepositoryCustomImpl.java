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

package com.felixseifert.coma.backend.repos;

import com.felixseifert.coma.backend.model.*;
import com.felixseifert.coma.backend.model.dto.TaskReleaseDTO;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.QueryHints;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class TaskReleaseRepositoryCustomImpl implements TaskReleaseRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<TaskReleaseDTO> findAllUnfinishedWithResponsibleEmployee() {
        return findAllUnfinishedByEmployeeEmailAddress(null);
    }

    @Override
    public List<TaskReleaseDTO> findAllUnfinishedByEmployeeEmailAddress(String emailAddress) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TaskReleaseDTO> cq = cb.createQuery(TaskReleaseDTO.class);
        Root<TaskRelease> root = cq.from(TaskRelease.class);
        Join<TaskRelease, TaskStepRelease> joinSteps = root.join(TaskRelease_.COMPLETED_TASK_STEPS, JoinType.INNER);
        Join<TaskRelease, Employee> joinEmployee = root.join(TaskRelease_.CURRENT_RESPONSIBLE_EMPLOYEE, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isNull(root.get(TaskRelease_.finishedAt)));

        if(StringUtils.isNotBlank(emailAddress)) {
            predicates.add(cb.equal(joinEmployee.get(Employee_.emailAddress), emailAddress));
        }

        cq.select(cb.construct(TaskReleaseDTO.class,
                root.get(TaskRelease_.id),
                root.get(TaskRelease_.startedAt),
                root.get(TaskRelease_.finishedAt),
                joinEmployee.get(Employee_.name),
                joinEmployee.get(Employee_.emailAddress),
                joinEmployee.get(Employee_.location),
                joinEmployee.get(Employee_.team),
                root.get(TaskRelease_.currentEmployeesRole),
                cb.greatest(joinSteps.get(TaskStepRelease_.completedAt))));

        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])))
                .groupBy(root.get(TaskRelease_.id));

        return entityManager.createQuery(cq).setHint(QueryHints.READ_ONLY, true).getResultList();
    }

    @Override
    public List<String> findAllResponsibleEmployeeEmailAddresses() {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<TaskRelease> root = cq.from(TaskRelease.class);
        Join<TaskRelease, Employee> join = root.join(TaskRelease_.currentResponsibleEmployee, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isNull(root.get(TaskRelease_.finishedAt)));
        predicates.add(cb.isNotEmpty(root.get(TaskRelease_.completedTaskSteps)));

        cq.select(cb.construct(String.class, join.get(Employee_.emailAddress)))
                .where(cb.and(predicates.toArray(new Predicate[predicates.size()])))
                .distinct(true);

        return entityManager.createQuery(cq).setHint(QueryHints.READ_ONLY, true).getResultList();
    }

    @Override
    public Long countPartNumberObjectsCurrentlyResponsible(Employee employee) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PartNumberObject> root = cq.from(PartNumberObject.class);
        Join<PartNumberObject, TaskRelease> join = root.join(PartNumberObject_.TASK_RELEASE);

        Predicate predicate = cb.equal(join.get(TaskRelease_.CURRENT_RESPONSIBLE_EMPLOYEE), employee);

        cq.select(cb.count(root)).where(predicate);

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    @Transactional
    public void removeCurrentResponsibilities(Employee employee) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<TaskRelease> update = cb.createCriteriaUpdate(TaskRelease.class);
        Root<TaskRelease>  root = update.from(TaskRelease.class);

        Predicate predicate = cb.equal(root.get(TaskRelease_.CURRENT_RESPONSIBLE_EMPLOYEE), employee);

        update.set(root.get(TaskRelease_.CURRENT_RESPONSIBLE_EMPLOYEE), (Employee) null).where(predicate);

        entityManager.createQuery(update).executeUpdate();
    }

    @Override
    @Transactional
    public void removeResponsibilitiesInTaskStepsRelease(Employee employee) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<TaskStepRelease> update = cb.createCriteriaUpdate(TaskStepRelease.class);
        Root<TaskStepRelease>  root = update.from(TaskStepRelease.class);

        Predicate predicate = cb.equal(root.get(TaskStepRelease_.ASSIGNED_EMPLOYEE), employee);

        update.set(root.get(TaskStepRelease_.ASSIGNED_EMPLOYEE), (Employee) null).where(predicate);

        entityManager.createQuery(update).executeUpdate();
    }
}
