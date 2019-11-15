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

package com.felixseifert.coma.backend.service;

import com.felixseifert.coma.backend.model.BusinessUnit;
import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Slf4j
public class RelationshipServiceImpl implements RelationshipService {

    @Autowired
    private PartNumberObjectService partNumberObjectService;

    @Autowired
    private BusinessUnitService businessUnitService;

    @Override
    public void removeRelationships(Employee employee) {
        removeRelationships(employee, employee.getRoles());
        log.debug("Remove relationships to PNs as REQUESTER");
        partNumberObjectService.removePNsForEmployeeByRole(employee, Role.REQUESTER);
        partNumberObjectService.removeCurrentResponsibilities(employee);
        partNumberObjectService.removeResponsibilitiesInTaskStepsRelease(employee);
    }

    @Override
    @Transactional
    public void removeRelationships(Employee employee, Set<Role> roles) {
        for(Role role : roles) {
            if(role.equals(Role.PRODUCT_MANAGER)) {
                log.debug("Remove relationships to Business Units");
                businessUnitService.removeBusinessUnitsForProductManager(employee);
            }
            log.debug("Remove relationships to PNs (except of REQUESTER)");
            partNumberObjectService.removePNsForEmployeeByRole(employee, role);
        }
        log.info("Removed relationships for {} of {}", roles, employee);
    }

    @Override
    public void removeRelationships(BusinessUnit businessUnit) {
        log.debug("Remove relationships to PNs");
        partNumberObjectService.removePNsForBusinessUnit(businessUnit);
        log.info("Removed relationships for {}", businessUnit);
    }
}
