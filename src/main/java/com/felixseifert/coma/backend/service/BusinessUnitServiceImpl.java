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
import com.felixseifert.coma.backend.repos.BusinessUnitRepository;
import com.felixseifert.coma.backend.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class BusinessUnitServiceImpl implements BusinessUnitService {

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    @Autowired
    private PartNumberObjectService partNumberObjectService;

    @Autowired
    private EmployeeService employeeService;

    @Override
    public List<BusinessUnit> getAllBusinessUnits() {
        log.debug("Get all Business Units");
        return businessUnitRepository.findAllBusinessUnits();
    }

    @Override
    public BusinessUnit getBusinessUnitByName(String name) throws EntityIDNotFoundException {
        Optional<BusinessUnit> businessUnit = businessUnitRepository.findByName(name);
        if(businessUnit.isEmpty()) {
            throw new EntityIDNotFoundException(ErrorMessages.BUSINESS_UNIT_NOT_FOUND);
        }
        log.debug("Get Business Unit with name={}", name);
        return businessUnit.get();
    }

    @Override
    @Transactional
    public BusinessUnit postBusinessUnit(BusinessUnit businessUnit)
            throws BlankValueNotAllowedException, EntityAlreadyExistsException {

        if(StringUtils.isBlank(businessUnit.getName())) {
            throw new BlankValueNotAllowedException(ErrorMessages.NAME_NOT_SPECIFIED);
        }

        boolean exists = businessUnitRepository.existsByName(businessUnit.getName());
        if(exists) {
            throw new EntityAlreadyExistsException(ErrorMessages.BUSINESS_UNIT_ALREADY_EXISTS);
        }

        Set<Employee> productManagers = new HashSet<>(businessUnit.getProductManagers());
        businessUnit.getProductManagers().clear();

        productManagers.forEach(pm ->
                employeeService.getEmployeeByEmailAddress(pm.getEmailAddress()).addBusinessUnitAsPM(businessUnit));

        log.info("Create Business Unit: {}", businessUnit);
        return businessUnitRepository.save(businessUnit);
    }

    @Override
    public BusinessUnit putBusinessUnit(BusinessUnit businessUnit)
            throws BlankValueNotAllowedException, EntityIDNotFoundException {

        if(StringUtils.isBlank(businessUnit.getName())) {
            throw new BlankValueNotAllowedException(ErrorMessages.NAME_NOT_SPECIFIED);
        }

        boolean exists = businessUnit.getId() != null;
        if(!exists) {
            throw new EntityIDNotFoundException(ErrorMessages.BUSINESS_UNIT_NOT_FOUND);
        }

        log.info("Update Business Unit: {}", businessUnit);
        return businessUnitRepository.save(businessUnit);
    }

    @Override
    public void deleteBusinessUnit(BusinessUnit businessUnit) throws EntityIDNotFoundException, DependencyException {

        if(!businessUnitRepository.existsByName(businessUnit.getName())) {
            throw new EntityIDNotFoundException(ErrorMessages.BUSINESS_UNIT_NOT_FOUND);
        }
        if(hasDependencies(businessUnit)) {
            throw new DependencyException(ErrorMessages.BUSINESS_UNIT_HAS_DEPENDENCIES);
        }

        log.info("Delete Business Unit {}", businessUnit);
        businessUnitRepository.delete(businessUnit);
    }

    private boolean hasDependencies(BusinessUnit businessUnit) {
        return partNumberObjectService.countPartNumberObjects(businessUnit) > 0;
    }

    @Override
    @Transactional
    public void removeBusinessUnitsForProductManager(Employee productManager) {
        log.info("Remove Product Manager {} from all BusinessUnits", productManager);
        Employee productManagerFromDatabase = employeeService
                .getEmployeeByEmailAddress(productManager.getEmailAddress());

        Set<BusinessUnit> businessUnits = new HashSet<>(productManagerFromDatabase.getBusinessUnitsAsPM());
        businessUnits.forEach(productManagerFromDatabase::removeBusinessUnitAsPM);

        employeeService.putEmployee(productManagerFromDatabase);
    }

    @Override
    public int count(Employee productManager) {
        return businessUnitRepository.countByProductManager(productManager);
    }

    @Override
    public boolean exists(String name) {
        boolean exists = businessUnitRepository.existsByName(name);
        log.debug("Business Unit with name {} is persisted: {}", name, exists);
        return exists;
    }
}
