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

import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.Employee_;
import com.felixseifert.coma.backend.model.enums.Role;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.QueryHints;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeRepositoryCustomImpl implements EmployeeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Employee> findByRole(Role role) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> root = cq.from(Employee.class);

        cq.select(root)
                .where(root.join(Employee_.ROLES, JoinType.INNER).in(List.of(role)))
                .orderBy(cb.asc(root.get(Employee_.name)));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public Optional<Employee> findByEmailAddress(String emailAddress) {
        EntityGraph<Employee> graph = entityManager.createEntityGraph(Employee.class);
        graph.addAttributeNodes(Employee.FIELDS_TO_INITIALISE);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee>  root = cq.from(Employee.class);

        cq.where(cb.equal(root.get(Employee_.EMAIL_ADDRESS), emailAddress));
        TypedQuery<Employee> typedQuery = entityManager.createQuery(cq);
        typedQuery.setHint("javax.persistence.fetchgraph", graph);
        Employee response = null;
        try {
            response = typedQuery.getSingleResult();
        }
        catch(NoResultException nre) {}
        return Optional.ofNullable(response);
    }

    @Override
    public List<Employee> findAllEmployees() {
        return findByNameStartsWithIgnoreCase(null);
    }

    @Override
    public List<Employee> findByNameStartsWithIgnoreCase(String filterText) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> root = cq.from(Employee.class);
        root.fetch(Employee_.ROLES, JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        if(StringUtils.isNotBlank(filterText)) {
            predicates.add(cb.like(cb.lower(root.get(Employee_.name)), "%" + filterText.toLowerCase() + "%"));
        }

        cq.select(root).where(cb.and(predicates.toArray(new Predicate[predicates.size()]))).distinct(true);

        return entityManager.createQuery(cq).setHint(QueryHints.HINT_READONLY, true).getResultList();
    }
}
