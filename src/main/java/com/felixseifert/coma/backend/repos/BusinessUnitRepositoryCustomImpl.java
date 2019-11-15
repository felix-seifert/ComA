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

import com.felixseifert.coma.backend.model.BusinessUnit;
import com.felixseifert.coma.backend.model.BusinessUnit_;
import com.felixseifert.coma.backend.model.Employee;
import org.hibernate.jpa.QueryHints;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class BusinessUnitRepositoryCustomImpl implements BusinessUnitRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<BusinessUnit> findByName(String name) {

        EntityGraph<BusinessUnit> graph = entityManager.createEntityGraph(BusinessUnit.class);
        graph.addAttributeNodes(BusinessUnit.FIELDS_TO_INITIALISE);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BusinessUnit> cq = cb.createQuery(BusinessUnit.class);
        Root<BusinessUnit> root = cq.from(BusinessUnit.class);

        cq.where(cb.equal(root.get(BusinessUnit_.NAME), name));
        TypedQuery<BusinessUnit> typedQuery = entityManager.createQuery(cq);
        typedQuery.setHint("javax.persistence.fetchgraph", graph);
        BusinessUnit response = null;
        try {
            response = typedQuery.getSingleResult();
        }
        catch(NoResultException nre) {}
        return Optional.ofNullable(response);
    }

    @Override
    public List<BusinessUnit> findAllBusinessUnits() {

        EntityGraph<BusinessUnit> graph = entityManager.createEntityGraph(BusinessUnit.class);
        graph.addAttributeNodes(BusinessUnit.FIELDS_TO_INITIALISE);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BusinessUnit> cq = cb.createQuery(BusinessUnit.class);
        cq.from(BusinessUnit.class);

        TypedQuery<BusinessUnit> typedQuery = entityManager.createQuery(cq);
        typedQuery.setHint("javax.persistence.fetchgraph", graph);
        typedQuery.setHint(QueryHints.HINT_READONLY, true);

        return typedQuery.getResultList();
    }

    @Override
    public int countByProductManager(Employee productManager) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<BusinessUnit> root = cq.from(BusinessUnit.class);

        Predicate predicate = cb.isMember(productManager, root.get(BusinessUnit_.productManagers));

        cq.select(cb.count(root)).where(predicate);

        return entityManager.createQuery(cq).getSingleResult().intValue();
    }
}
