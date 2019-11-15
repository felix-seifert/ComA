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

import com.felixseifert.coma.backend.exceptions.BlankValueNotAllowedException;
import com.felixseifert.coma.backend.exceptions.EntityAlreadyExistsException;
import com.felixseifert.coma.backend.exceptions.EntityIDNotFoundException;
import com.felixseifert.coma.backend.exceptions.ErrorMessages;
import com.felixseifert.coma.backend.model.dto.PartNumberObjectGridDTO;
import com.felixseifert.coma.backend.model.enums.CustomerNotification;
import com.felixseifert.coma.backend.model.enums.Progress;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.repos.PartNumberLobRepository;
import com.felixseifert.coma.backend.repos.PartNumberObjectRepository;
import com.felixseifert.coma.backend.repos.TaskReleaseRepository;
import com.felixseifert.coma.backend.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PartNumberObjectServiceImpl implements PartNumberObjectService {

    @Autowired
    private PartNumberObjectRepository partNumberObjectRepository;

    @Autowired
    private PartNumberLobRepository partNumberLobRepository;

    @Autowired
    private TaskReleaseRepository taskReleaseRepository;

    @Override
    public PartNumberObject getPartNumberObjectById(Integer id) throws EntityIDNotFoundException {
        log.debug("Get PartNumberObject with id {}", id);
        Optional<PartNumberObject> pnFoundOptional = partNumberObjectRepository.findById(id);
        if(pnFoundOptional.isEmpty()) {
            throw new EntityIDNotFoundException(ErrorMessages.PART_NUMBER_ID_NOT_FOUND);
        }
        PartNumberObject pnFound = pnFoundOptional.get();
        if(pnFound.getLobs() != null) {
            pnFound.setComments(pnFound.getLobs().getComments());
        }
        return pnFound;
    }

    @Override
    public List<PartNumberObjectGridDTO> getPartNumberObjectsByPnStartsWithIgnoreCase(
            String pn, int limit, int offset, boolean descending, String sortProperty) {

        log.debug("Get all ProductNumberObjects with pn starting with {}, limit={} and offset={}", pn, limit, offset);

        if(StringUtils.isBlank(sortProperty)) {
            sortProperty = PartNumberObject_.LAST_MODIFIED_DATE_TIME;
            descending = true;
        }

        if(StringUtils.isBlank(pn)) {
            return partNumberObjectRepository.findAllPnGridDtos(limit, offset, descending, sortProperty);
        }
        return partNumberObjectRepository.findByPnStartsWithIgnoreCase(pn, limit, offset, descending, sortProperty);
    }

    @Override
    public PartNumberObject postPartNumberObject(PartNumberObject partNumberObject)
            throws EntityAlreadyExistsException, BlankValueNotAllowedException {

        if(StringUtils.isBlank(partNumberObject.getPn())) {
            throw new BlankValueNotAllowedException(ErrorMessages.PART_NUMBER_NOT_SPECIFIED);
        }

        boolean exists = partNumberObject.getId() != null;
        if(exists) {
            throw new EntityAlreadyExistsException(ErrorMessages.PART_NUMBER_ALREADY_EXISTS);
        }

        if(isAFinishedTaskStepInvalid(partNumberObject)) {
            throw new IllegalArgumentException(ErrorMessages.NO_EMPLOYEE_RESPONSIBLE);
        }

        if(partNumberObject.getTaskRelease() == null) {
            partNumberObject.setTaskRelease(new TaskRelease());
        }

        partNumberObject.getTaskRelease().setStatus(Progress.IN_PROGRESS);
        partNumberObject.getTaskRelease().setCustomerNotification(CustomerNotification.NOT_NOTIFIED);

        partNumberObject.getTaskRelease().setCurrentEmployeesRole(Role.REQUESTER);
        partNumberObject.getTaskRelease().setCurrentResponsibleEmployee(partNumberObject.getCreatedByEmployee());

        if(partNumberObject.getTaskRelease().isStepFinished()) {
            transferTaskToNextEmployee(partNumberObject);
        }

        if(StringUtils.isNotBlank(partNumberObject.getComments())) {
            partNumberObject.setLobs(new PartNumberLob());
            partNumberObject.getLobs().setComments(partNumberObject.getComments());
            log.info("Create PartNumberLob and PartNumberObject: {}", partNumberObject);
        }
        else {
            log.info("Create PartNumberObject: {}", partNumberObject);
        }
        return partNumberObjectRepository.save(partNumberObject);
    }

    @Override
    public PartNumberObject putPartNumberObject(PartNumberObject partNumberObject)
            throws EntityIDNotFoundException, BlankValueNotAllowedException {

        if(StringUtils.isBlank(partNumberObject.getPn())) {
            throw new BlankValueNotAllowedException(ErrorMessages.PART_NUMBER_NOT_SPECIFIED);
        }

        boolean exists = partNumberObject.getId() != null;
        if(!exists) {
            throw new EntityIDNotFoundException(ErrorMessages.PART_NUMBER_ID_NOT_FOUND);
        }

        if(partNumberObject.getLobs() != null && partNumberObject.getLobs().getId() == null) {
            throw new EntityIDNotFoundException(ErrorMessages.PART_NUMBER_LOB_ID_NOT_FOUND);
        }

        if(isAFinishedTaskStepInvalid(partNumberObject)) {
            throw new IllegalArgumentException(ErrorMessages.NO_EMPLOYEE_RESPONSIBLE);
        }

        if(partNumberObject.getTaskRelease() != null && partNumberObject.getTaskRelease().isStepFinished()) {
            transferTaskToNextEmployee(partNumberObject);
        }

        if(partNumberObject.getLobs() == null && StringUtils.isNotBlank(partNumberObject.getComments())) {
            partNumberObject.setLobs(new PartNumberLob());
            partNumberObject.getLobs().setComments(partNumberObject.getComments());
            log.info("Create PartNumberLob: {}", partNumberObject.getLobs());
            partNumberLobRepository.save(partNumberObject.getLobs());
        }
        else if(partNumberObject.getLobs() != null && StringUtils.isBlank(partNumberObject.getComments())) {
            log.info("Delete PartNumberLob: {} ", partNumberObject.getLobs());
            partNumberLobRepository.delete(partNumberObject.getLobs());
            partNumberObject.setLobs(null);
        }
        else if(partNumberObject.getLobs() != null && StringUtils.isNotBlank(partNumberObject.getComments())) {
            partNumberObject.getLobs().setComments(partNumberObject.getComments());
            log.info("Update PartNumberLob and PartNumberObject: {}", partNumberObject);
            return partNumberObjectRepository.save(partNumberObject);
        }
        log.info("Update PartNumberObject: {}", partNumberObject);
        return partNumberObjectRepository.save(partNumberObject);
    }

    @Override
    public PartNumberObject denyRequest(PartNumberObject partNumberObject) {

        if(StringUtils.isBlank(partNumberObject.getComments()) &&
                (partNumberObject.getLobs() == null ||
                        StringUtils.isBlank(partNumberObject.getLobs().getComments()))) {
            throw new IllegalArgumentException(ErrorMessages.REQUEST_DENIAL_NEEDS_COMMENTS);
        }

        partNumberObject.getTaskRelease().setStatus(Progress.DENIED);
        partNumberObject.getTaskRelease().setStepFinished(true);

        return putPartNumberObject(partNumberObject);
    }

    private boolean isAFinishedTaskStepInvalid(PartNumberObject partNumberObject) {

        if(partNumberObject.getTaskRelease() == null ||
                partNumberObject.getTaskRelease().getCompletedTaskSteps() == null) {
            return false;
        }

        Role role;
        for(TaskStepRelease taskStepRelease : partNumberObject.getTaskRelease().getCompletedTaskSteps()) {
            role = Role.roleOfName(taskStepRelease.getRoleOfEmployee());
            if(Role.REQUESTER.equals(role)) continue;
            if(getResponsibleEmployeeForRole(role, partNumberObject) == null) return true;
        }

        return false;
    }

    private void transferTaskToNextEmployee(PartNumberObject partNumberObject) {

        TaskRelease taskRelease = partNumberObject.getTaskRelease();

        taskRelease.addTaskStep(createFinishedTaskStep(partNumberObject));

        if(Progress.DENIED.equals(taskRelease.getStatus()) &&
                CustomerNotification.NOT_NOTIFIED.equals(taskRelease.getCustomerNotification())) {
            taskRelease.setFinishedAt(LocalDateTime.now());
            taskRelease.setCurrentResponsibleEmployee(partNumberObject.getCreatedByEmployee());
            taskRelease.setCurrentEmployeesRole(Role.REQUESTER);
            return;
        }

        if(Progress.DENIED.equals(taskRelease.getStatus())) {
            taskRelease.setCurrentResponsibleEmployee(null);
            taskRelease.setCurrentEmployeesRole(null);
            return;
        }

        if((taskRelease.getRemainingSteps() == null || taskRelease.getRemainingSteps().isEmpty()) &&
                CustomerNotification.NOT_NOTIFIED.equals(taskRelease.getCustomerNotification())) {
            taskRelease.setFinishedAt(LocalDateTime.now());
            taskRelease.setStatus(Progress.RELEASED);
            taskRelease.setCurrentResponsibleEmployee(partNumberObject.getCreatedByEmployee());
            taskRelease.setCurrentEmployeesRole(Role.REQUESTER);
            return;
        }

        if(taskRelease.getRemainingSteps() == null || taskRelease.getRemainingSteps().isEmpty()) {
            taskRelease.setCurrentResponsibleEmployee(null);
            taskRelease.setCurrentEmployeesRole(null);
            return;
        }

        Role role = taskRelease.getRemainingSteps().get(0);
        Employee employee = getResponsibleEmployeeForRole(role, partNumberObject);

        if(employee == null) {
            throw new IllegalArgumentException(ErrorMessages.EMPLOYEE_IS_NOT_SET);
        }

        taskRelease.setCurrentResponsibleEmployee(employee);
        taskRelease.setCurrentEmployeesRole(role);
        taskRelease.getRemainingSteps().remove(0);
    }

    private TaskStepRelease createFinishedTaskStep(PartNumberObject partNumberObject) {
        TaskStepRelease taskStepRelease = new TaskStepRelease();
        taskStepRelease.setCompletedAt(LocalDateTime.now());
        taskStepRelease.setRoleOfEmployee(partNumberObject.getTaskRelease().getCurrentEmployeesRole().getName());
        Employee responsibleEmployee = partNumberObject.getTaskRelease().getCurrentResponsibleEmployee();
        if(responsibleEmployee == null) {
            responsibleEmployee = getResponsibleEmployeeForRole(
                    partNumberObject.getTaskRelease().getCurrentEmployeesRole(), partNumberObject);
        }
        if(responsibleEmployee == null) {
            throw new IllegalArgumentException(ErrorMessages.EMPLOYEE_IS_NOT_SET);
        }
        taskStepRelease.setAssignedEmployee(responsibleEmployee);
        return taskStepRelease;
    }

    @Override
    public Employee getResponsibleEmployeeForRole(Role role, PartNumberObject partNumberObject) {

        if(role.equals(Role.REQUESTER)) {
            return partNumberObject.getCreatedByEmployee();
        }

        String methodName = "get" + role.getPnVariable().substring(0, 1).toUpperCase() +
                role.getPnVariable().substring(1);

        Employee employee = null;
        try {
            employee = (Employee) partNumberObject.getClass().getMethod(methodName)
                    .invoke(partNumberObject);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return employee;
    }

    @Override
    public void deletePartNumberObject(PartNumberObject partNumberObject) throws EntityIDNotFoundException {

        if(!partNumberObjectRepository.existsById(partNumberObject.getId())) {
            throw new EntityIDNotFoundException(ErrorMessages.PART_NUMBER_ID_NOT_FOUND);
        }

        if(partNumberObject.getTaskRelease() != null && partNumberObject.getTaskRelease().getCompletelyFinished()) {
            TaskRelease taskRelease = partNumberObject.getTaskRelease();
            partNumberObject.setTaskRelease(null);

            log.info("Remove PartNumberObject association for {}", taskRelease);
            taskReleaseRepository.save(taskRelease);
        }
        else if(partNumberObject.getTaskRelease() != null &&
                !partNumberObject.getTaskRelease().getCompletelyFinished()) {
            TaskRelease taskRelease = partNumberObject.getTaskRelease();
            partNumberObject.setTaskRelease(null);

            log.info("Delete {} because it is not finished.", taskRelease);
            taskReleaseRepository.delete(taskRelease);
        }

        if(partNumberObject.getLobs() != null) {
            log.info("Delete PartNumberLob and PartNumberObject: {}", partNumberObject);
        }
        else {
            log.info("Delete PartNumberObject: {}", partNumberObject);
        }
        partNumberObjectRepository.delete(partNumberObject);
    }

    @Override
    public void removePNsForEmployeeByRole(Employee employee, Role role) {
        log.info("Remove {} with role {} for all PNs", employee, role.getName());
        partNumberObjectRepository.removePNsForEmployeeByRole(employee, role);
    }

    @Override
    public void removeCurrentResponsibilities(Employee employee) {
        log.info("Remove all current Responsibilities for {}", employee);
        taskReleaseRepository.removeCurrentResponsibilities(employee);
    }

    @Override
    public void removeResponsibilitiesInTaskStepsRelease(Employee employee) {
        log.info("Remove all relationships to {} in all TaskStepsRelease", employee);
        taskReleaseRepository.removeResponsibilitiesInTaskStepsRelease(employee);
    }

    @Override
    public void removePNsForBusinessUnit(BusinessUnit businessUnit) {
        log.info("Remove {} for all PNs", businessUnit);
        partNumberObjectRepository.removePNsForBusinessUnit(businessUnit);
    }

    @Override
    public boolean existsPartNumberObjectById(Integer id) {
        boolean exists = (id != null) && partNumberObjectRepository.existsById(id);
        log.debug("PartNumber with the id {} is persisted: {}", id, exists);
        return exists;
    }

    @Override
    public boolean existsPartNumberObjectByCompletePn(String pn) {
        boolean exists = (pn != null) && partNumberObjectRepository.existsByCompletePn(pn);
        log.debug("PartNumber with the complete PN {} is persisted: {}", pn, exists);
        return exists;
    }

    @Override
    public int countPartNumberObjects() {
        // Typecast possible because PartNumberObject has only Integer id
        return (int) partNumberObjectRepository.count();
    }

    @Override
    public int countPartNumberObjects(Employee employee) {
        return partNumberObjectRepository.countPartNumberObjects(employee);
    }

    @Override
    public int countPartNumberObjects(Employee employee, Role role) {
        return partNumberObjectRepository.countPartNumberObjects(employee, role);
    }

    @Override
    public int countPartNumberObjects(BusinessUnit businessUnit) {
        return partNumberObjectRepository.countPartNumberObjects(businessUnit);
    }

    @Override
    public long countPartNumberObjectsCurrentlyResponsible(Employee employee) {
        return taskReleaseRepository.countPartNumberObjectsCurrentlyResponsible(employee);
    }

    @Override
    public int countPartNumberObjectsByPnStartsWithIgnoreCase(String pn) {
        if(StringUtils.isNotBlank(pn)) {
            return partNumberObjectRepository.countByPnStartsWithIgnoreCase(pn);
        }
        return countPartNumberObjects();
    }
}
