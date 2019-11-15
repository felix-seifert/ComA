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

import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.repos.EmployeeRepository;
import com.felixseifert.coma.backend.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PartNumberObjectService partNumberObjectService;

    @Autowired
    private BusinessUnitService businessUnitService;

    @Override
    public List<Employee> getAllEmployees() {
        log.debug("Get all Employees");
        return employeeRepository.findAllEmployees();
    }

    @Override
    public List<Employee> getAllEmployeesByRole(Role role) {
        log.debug("Get all Employees with Role {}", role);
        return employeeRepository.findByRole(role);
    }

    @Override
    public List<Employee> getAllProductManagers() {
        log.debug("Get all Product Managers");
        return employeeRepository.findByRole(Role.PRODUCT_MANAGER);
    }

    @Override
    public List<Employee> getAllProductSpecialists() {
        log.debug("Get all Product Specialists");
        return employeeRepository.findByRole(Role.PRODUCT_SPECIALIST);
    }

    @Override
    public Employee getEmployeeByEmailAddress(String emailAddress) throws EntityNotFoundException {
        Optional<Employee> employee = employeeRepository.findByEmailAddress(emailAddress);
        if(employee.isEmpty()) {
            throw new EntityNotFoundException(ErrorMessages.EMAIL_ADDRESS_NOT_FOUND);
        }
        log.debug("Get Employee with email address={}", emailAddress);
        return employee.get();
    }

    @Override
    public List<Employee> getEmployeesByNameStartsWithIgnoreCase(String filterText) {
        log.debug("Get all Employees whos name start with {}", filterText);
        return employeeRepository.findByNameStartsWithIgnoreCase(filterText);
    }

    @Override
    public Employee postEmployee(Employee employee)
            throws BlankValueNotAllowedException, EntityAlreadyExistsException {

        checkForBlankFields(employee);

        if(employee.getId() != null) {
            throw new EntityAlreadyExistsException(ErrorMessages.EMPLOYEE_ALREADY_EXISTS);
        }
        if(employeeRepository.existsByEmailAddress(employee.getEmailAddress())) {
            throw new EntityAlreadyExistsException(ErrorMessages.EMAIL_ADDRESS_ALREADY_EXISTS_NO_NEW_ENTITY);
        }
        log.info("Create Employee {}", employee);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee putEmployee(Employee employee) throws BlankValueNotAllowedException, EntityIDNotFoundException {

        checkForBlankFields(employee);

        boolean exists = employeeRepository.existsById(employee.getId());
        if(!exists) {
            throw new EntityIDNotFoundException(ErrorMessages.EMPLOYEE_NOT_FOUND);
        }
        Optional<Employee> employeeByEmailAddress = employeeRepository.findByEmailAddress(employee.getEmailAddress());
        if(employeeByEmailAddress.isPresent() && !employeeByEmailAddress.get().getId().equals(employee.getId())) {
            throw new EntityAlreadyExistsException(ErrorMessages.EMAIL_ADDRESS_ALREADY_EXISTS_FOR_OTHER_ENTITY);
        }
        log.info("Update Employee {}", employee);
        return employeeRepository.save(employee);
    }

    @Override
    public void deleteEmployee(Employee employee) throws EntityIDNotFoundException, DependencyException {
        boolean exists = employee.getId() != null;
        if(!exists) {
            throw new EntityIDNotFoundException(ErrorMessages.EMPLOYEE_NOT_FOUND);
        }
        if(hasRelationships(employee)) {
            throw new DependencyException(ErrorMessages.EMPLOYEE_HAS_RELATIONSHIPS);
        }
        log.info("Delete Employee {}", employee);
        employeeRepository.delete(employee);
    }

    private boolean hasRelationships(Employee employee) {
        return partNumberObjectService.countPartNumberObjects(employee) > 0 || businessUnitService.count(employee) > 0;
    }

    @Override
    public boolean existsById(int id) {
        boolean exists = employeeRepository.existsById(id);
        log.debug("Employee with id {} is persisted: {}", id, exists);
        return exists;
    }

    @Override
    public boolean existsByEmailAddress(String emailAddress) {
        boolean exists = employeeRepository.existsByEmailAddress(emailAddress);
        log.debug("Employee with email address {} is persisted: {}", emailAddress, exists);
        return exists;
    }

    private void checkForBlankFields(Employee employee) throws BlankValueNotAllowedException {
        if(StringUtils.isBlank(employee.getEmailAddress())) {
            throw new BlankValueNotAllowedException(ErrorMessages.EMAIL_ADDRESS_NOT_SPECIFIED);
        }
        if(StringUtils.isBlank(employee.getName())) {
            throw new BlankValueNotAllowedException(ErrorMessages.NAME_NOT_SPECIFIED);
        }
    }
}
