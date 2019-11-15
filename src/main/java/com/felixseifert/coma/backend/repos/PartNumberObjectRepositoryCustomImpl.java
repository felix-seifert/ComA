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

import com.felixseifert.coma.backend.model.dto.PartNumberObjectGridDTO;
import com.felixseifert.coma.backend.model.enums.Progress;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.model.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.QueryHints;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PartNumberObjectRepositoryCustomImpl implements PartNumberObjectRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<PartNumberObject> findById(Integer id) {
        EntityGraph<PartNumberObject> graph = entityManager.createEntityGraph(PartNumberObject.class);

        for(String field : PartNumberObject.FIELDS_TO_INITIALISE) {
            if(field.equals(PartNumberObject_.TASK_RELEASE)) {
                Subgraph<TaskRelease> subgraph = graph.addSubgraph(field);
                subgraph.addAttributeNodes(TaskRelease.FIELDS_TO_INITIALISE);
                continue;
            }
            if(field.equals(PartNumberObject_.PRODUCT_MANAGER)) {
                Subgraph<Employee> subgraph = graph.addSubgraph(field);
                subgraph.addAttributeNodes(Employee.FIELDS_TO_INITIALISE);
                continue;
            }
            graph.addAttributeNodes(field);
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PartNumberObject> cq = cb.createQuery(PartNumberObject.class);
        Root<PartNumberObject> root = cq.from(PartNumberObject.class);

        cq.where(cb.equal(root.get(PartNumberObject_.ID), id));
        TypedQuery<PartNumberObject> typedQuery = entityManager.createQuery(cq);
        typedQuery.setHint("javax.persistence.fetchgraph", graph);
        PartNumberObject response = null;
        try {
            response = typedQuery.getSingleResult();
        }
        catch(NoResultException nre) {}
        return Optional.ofNullable(response);
    }

    @Override
    public List<PartNumberObjectGridDTO> findAllPnGridDtos(int limit, int offset, boolean descending,
                                                           String sortProperty) {
        return findByPnStartsWithIgnoreCase(null, limit, offset, descending, sortProperty);
    }

    @Override
    public List<PartNumberObjectGridDTO> findByPnStartsWithIgnoreCase(String pn, int limit, int offset,
                                                                      boolean descending, String sortProperty) throws IllegalArgumentException {

        if (offset < 0) {
            throw new IllegalArgumentException("Offset index must not be less than zero!");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must not be less than one!");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PartNumberObjectGridDTO> cq = cb.createQuery(PartNumberObjectGridDTO.class);
        Root<PartNumberObject> root = cq.from(PartNumberObject.class);
        Join<PartNumberObject, TaskRelease> joinTask = root.join(PartNumberObject_.TASK_RELEASE);
        Join<TaskRelease, Employee> joinEmployee =
                joinTask.join(TaskRelease_.CURRENT_RESPONSIBLE_EMPLOYEE, JoinType.LEFT);


        cq.select(cb.construct(PartNumberObjectGridDTO.class,
                root.get(PartNumberObject_.id),
                root.get(PartNumberObject_.pn),
                root.get(PartNumberObject_.dueDate),
                root.get(PartNumberObject_.createdByTeamDepartment),
                root.get(PartNumberObject_.createdDate),
                joinEmployee.get(Employee_.name),
                joinTask.get(TaskRelease_.currentEmployeesRole),
                joinTask.get(TaskRelease_.finishedAt),
                joinTask.get(TaskRelease_.status),
                joinTask.get(TaskRelease_.customerNotification)));

        if(StringUtils.isNotBlank(pn)) {
            cq.where(cb.like(cb.lower(root.get(PartNumberObject_.pn)), "%" + pn.toLowerCase() + "%"));
        }

        sortProperty = PartNumberObjectGridDTO.FIELD_MAPPING_DTO_TO_ORIGIN.get(sortProperty);
        if(StringUtils.isBlank(sortProperty)) {
            sortProperty = PartNumberObject_.LAST_MODIFIED_DATE_TIME;
        }

        Expression orderBy;

        switch(sortProperty) {
            case Employee_.NAME:
                orderBy = joinEmployee.get(sortProperty);
                break;
            case TaskRelease_.FINISHED_AT:
                orderBy = joinTask.get(sortProperty);
                break;
            default:
                orderBy = root.get(sortProperty);
        }

        if(descending) cq.orderBy(cb.desc(orderBy));
        else cq.orderBy(cb.asc(orderBy));

        TypedQuery<PartNumberObjectGridDTO> typedQuery = entityManager.createQuery(cq);

        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(limit);

        typedQuery.setHint(QueryHints.HINT_READONLY, true);

        return typedQuery.getResultList();
    }

    @Override
    @Transactional
    public void removePNsForEmployeeByRole(Employee employee, Role role) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<PartNumberObject> update = cb.createCriteriaUpdate(PartNumberObject.class);
        Root<PartNumberObject>  root = update.from(PartNumberObject.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(role.getPnVariable()), employee));

        update.set(root.get(role.getPnVariable()), (Employee) null)
                .where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        entityManager.createQuery(update).executeUpdate();
    }

    @Override
    @Transactional
    public void removePNsForBusinessUnit(BusinessUnit businessUnit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<PartNumberObject> update = cb.createCriteriaUpdate(PartNumberObject.class);
        Root<PartNumberObject>  root = update.from(PartNumberObject.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(PartNumberObject_.BUSINESS_UNIT), businessUnit));

        update.set(root.get(PartNumberObject_.BUSINESS_UNIT), (BusinessUnit) null)
                .where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        entityManager.createQuery(update).executeUpdate();
    }

    @Override
    public Integer countPartNumberObjects(Employee employee) {
        return countPartNumberObjects(employee, null);
    }

    @Override
    public Integer countPartNumberObjects(Employee employee, Role role) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PartNumberObject> root = cq.from(PartNumberObject.class);

        List<Predicate> predicates = new ArrayList<>();
        if(role != null) {
            predicates.add(cb.equal(root.get(role.getPnVariable()), employee));
        }
        else {
            employee.getRoles().forEach(r -> predicates.add(cb.equal(root.get(r.getPnVariable()), employee)));
        }

        cq.select(cb.count(root)).where(cb.or(predicates.toArray(new Predicate[predicates.size()])));

        return entityManager.createQuery(cq).getSingleResult().intValue();
    }

    @Override
    public Integer countPartNumberObjects(BusinessUnit businessUnit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PartNumberObject> root = cq.from(PartNumberObject.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(PartNumberObject_.businessUnit), businessUnit));

        cq.select(cb.count(root)).where(cb.or(predicates.toArray(new Predicate[predicates.size()])));

        return entityManager.createQuery(cq).getSingleResult().intValue();
    }

    @Override
    public boolean existsByCompletePn(String completePn) {
        String pn = completePn.substring(0, 10);
        String idx = completePn.substring(completePn.length() - 3);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PartNumberObject>  root = cq.from(PartNumberObject.class);
        Join<PartNumberObject, TaskRelease> join = root.join(PartNumberObject_.TASK_RELEASE);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(PartNumberObject_.PN), pn));
        predicates.add(cb.equal(root.get(PartNumberObject_.IDX), idx));
        predicates.add(cb.or(
                cb.equal(join.get(TaskRelease_.STATUS), Progress.RELEASED),
                cb.equal(join.get(TaskRelease_.STATUS), Progress.IN_PROGRESS)));

        cq.select(cb.count(root)).where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        Long count = entityManager.createQuery(cq).getSingleResult();

        return count > 0;
    }
}
