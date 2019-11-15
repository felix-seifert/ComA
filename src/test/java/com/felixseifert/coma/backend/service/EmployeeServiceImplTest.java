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
import com.felixseifert.coma.backend.exceptions.DependencyException;
import com.felixseifert.coma.backend.exceptions.EntityAlreadyExistsException;
import com.felixseifert.coma.backend.exceptions.EntityIDNotFoundException;
import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.PartNumberObject;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.repos.EmployeeRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EmployeeServiceImplTest {

    @Autowired
    private EmployeeServiceImpl employeeService;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private PartNumberObjectService partNumberObjectService;

    @MockBean
    private BusinessUnitService businessUnitService;

    private static Employee employeeExpectedPm;
    private static Employee employeeExpectedPs;
    private static Employee employeeNew;

    private static PartNumberObject partNumberObject;

    @BeforeClass
    public static void setup() {
        employeeExpectedPm = new Employee();
        employeeExpectedPm.setName("Berta Bold");
        employeeExpectedPm.setEmailAddress("berta.bold@jp.bosch.com");
        employeeExpectedPm.addRole(Role.PRODUCT_MANAGER);
        employeeExpectedPm.setTeam("RBR");
        employeeExpectedPm.setId(1);
        employeeExpectedPs = new Employee();
        employeeExpectedPs.setName("Carlos Creeds");
        employeeExpectedPs.setEmailAddress("carlos.creeds3@jp.bosch.com");
        employeeExpectedPs.addRole(Role.PRODUCT_SPECIALIST);
        employeeExpectedPs.setTeam("SMS");
        employeeExpectedPs.setId(2);
        employeeNew = new Employee();
        employeeNew.setName("Erle Erlang");
        employeeNew.setEmailAddress("erle.erlang@sg.bosch.com");
        employeeNew.addRole(Role.PRODUCT_SPECIALIST);
        employeeNew.setTeam("SMS4");

        partNumberObject = new PartNumberObject();
        partNumberObject.setPn("0123456789");
        partNumberObject.setStartOfProduction(LocalDate.parse("2019-11-01"));
        partNumberObject.setEndOfProduction(LocalDate.parse("2022-08-31"));
        partNumberObject.setProductManager(employeeExpectedPm);
    }

    @Test
    public void getAllEmployeesTest() {
        when(employeeRepository.findAllEmployees()).thenReturn(List.of(employeeExpectedPs, employeeExpectedPm));
        List<Employee> actual = employeeService.getAllEmployees();
        assertEquals(List.of(employeeExpectedPs, employeeExpectedPm), actual);
    }

    @Test
    public void getAllProductManagersTest() {
        when(employeeRepository.findByRole(any(Role.class))).thenReturn(List.of(employeeExpectedPm));
        List<Employee> actual = employeeService.getAllProductManagers();
        assertEquals(List.of(employeeExpectedPm), actual);
    }

    @Test
    public void getAllProductSpecialistsTest() {
        when(employeeRepository.findByRole(any(Role.class))).thenReturn(List.of(employeeExpectedPs));
        List<Employee> actual = employeeService.getAllProductSpecialists();
        assertEquals(List.of(employeeExpectedPs), actual);
    }

    @Test
    public void getEmployeeByEmailAddressTest() {
        when(employeeRepository.findByEmailAddress(employeeExpectedPm.getEmailAddress()))
                .thenReturn(Optional.of(employeeExpectedPm));
        Employee actual = employeeService.getEmployeeByEmailAddress(employeeExpectedPm.getEmailAddress());
        assertEquals(employeeExpectedPm, actual);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getEmployeeByEmailAddressTest_noEmployeeFound() {
        when(employeeRepository.findByEmailAddress(anyString())).thenReturn(Optional.empty());
        employeeService.getEmployeeByEmailAddress(anyString());
    }

    @Test
    public void getEmployeesByNameStartsWithIgnoreCaseTest() {
        when(employeeRepository.findByNameStartsWithIgnoreCase("e"))
                .thenReturn(List.of(employeeExpectedPs, employeeExpectedPm));
        List<Employee> actual =
                employeeService.getEmployeesByNameStartsWithIgnoreCase("e");
        assertTrue(actual.contains(employeeExpectedPs));
        assertTrue(actual.contains(employeeExpectedPm));
    }

    @Test
    public void getEmployeeByNameStartsWithIgnoreCaseTest_noEmployeeFound() {
        when(employeeRepository.findByNameStartsWithIgnoreCase(anyString())).thenReturn(List.of());
        List<Employee> actual = employeeService.getEmployeesByNameStartsWithIgnoreCase("zz");
        assertEquals(List.of(), actual);
    }

    @Test
    public void postEmployeeTest() {
        when(employeeRepository.save(employeeNew)).thenReturn(employeeNew);
        assertEquals(employeeNew, employeeService.postEmployee(employeeNew));
        verify(employeeRepository).save(employeeNew);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void postEmployeeTest_idExists() {
        employeeService.postEmployee(employeeExpectedPm);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void postEmployeeTest_emailAddressExists() {
        when(employeeRepository.existsByEmailAddress(employeeExpectedPs.getEmailAddress())).thenReturn(true);
        employeeService.postEmployee(employeeExpectedPs);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postEmployeeTest_emailAddressNull() {
        Employee employeeEmptyEmailAddress = new Employee();
        employeeEmptyEmailAddress.setEmailAddress(null);
        employeeEmptyEmailAddress.setName("Emil Err");
        employeeService.postEmployee(employeeEmptyEmailAddress);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postEmployeeTest_emailAddressBlank() {
        Employee employeeBlankEmailAddress = new Employee();
        employeeBlankEmailAddress.setEmailAddress(" ");
        employeeBlankEmailAddress.setName("Emil Err");
        employeeService.postEmployee(employeeBlankEmailAddress);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postEmployeeTest_nameNull() {
        Employee employeeEmptyName = new Employee();
        employeeEmptyName.setEmailAddress("address@bosch.com");
        employeeEmptyName.setName(null);
        employeeService.postEmployee(employeeEmptyName);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postEmployeeTest_nameBlank() {
        Employee employeeBlankName = new Employee();
        employeeBlankName.setEmailAddress("address@bosch.com");
        employeeBlankName.setName(" ");
        employeeService.postEmployee(employeeBlankName);
    }

    @Test
    public void putEmployeeTest() {
        when(employeeRepository.existsById(employeeExpectedPm.getId())).thenReturn(true);
        when(employeeRepository.save(employeeExpectedPm)).thenReturn(employeeExpectedPm);
        assertEquals(employeeExpectedPm, employeeService.putEmployee(employeeExpectedPm));
        verify(employeeRepository).save(employeeExpectedPm);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void putEmployeeTest_idDoesNotExist() {
        when(employeeRepository.existsById(employeeExpectedPm.getId())).thenReturn(false);
        employeeService.putEmployee(employeeExpectedPm);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void putEmployeeTest_emailAddressExistsForOtherEmployee() {
        when(employeeRepository.existsById(employeeExpectedPm.getId())).thenReturn(true);
        when(employeeRepository.findByEmailAddress(employeeExpectedPm.getEmailAddress()))
                .thenReturn(Optional.of(employeeExpectedPs));
        employeeService.putEmployee(employeeExpectedPm);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putEmployeeTest_emailAddressNull() {
        Employee employeeEmptyEmailAddress = new Employee();
        employeeEmptyEmailAddress.setEmailAddress(null);
        employeeEmptyEmailAddress.setName("Emil Err");
        employeeService.putEmployee(employeeEmptyEmailAddress);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putEmployeeTest_emailAddressBlank() {
        Employee employeeBlankEmailAddress = new Employee();
        employeeBlankEmailAddress.setEmailAddress(" ");
        employeeBlankEmailAddress.setName("Emil Err");
        employeeService.putEmployee(employeeBlankEmailAddress);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putEmployeeTest_nameNull() {
        Employee employeeEmptyName = new Employee();
        employeeEmptyName.setEmailAddress("address@bosch.com");
        employeeEmptyName.setName(null);
        employeeService.putEmployee(employeeEmptyName);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putEmployeeTest_nameBlank() {
        Employee employeeBlankName = new Employee();
        employeeBlankName.setEmailAddress("address@bosch.com");
        employeeBlankName.setName(" ");
        employeeService.putEmployee(employeeBlankName);
    }

    @Test
    public void deleteEmployeeTest() throws DependencyException {
        when(partNumberObjectService.countPartNumberObjects(employeeExpectedPm)).thenReturn(0);
        when(businessUnitService.count(employeeExpectedPm)).thenReturn(0);
        employeeService.deleteEmployee(employeeExpectedPm);
        verify(employeeRepository).delete(employeeExpectedPm);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void deleteEmployeeTest_IdDoesNotExist() throws DependencyException {
        employeeService.deleteEmployee(employeeNew);
    }

    @Test(expected = DependencyException.class)
    public void deleteEmployeeTest_dependenciesExist() throws DependencyException {
        when(partNumberObjectService.countPartNumberObjects(employeeExpectedPm)).thenReturn(0);
        when(businessUnitService.count(employeeExpectedPm)).thenReturn(1);
        employeeService.deleteEmployee(employeeExpectedPm);
    }

    @Test
    public void existsByIdTest() {
        when(employeeRepository.existsById(employeeExpectedPm.getId())).thenReturn(true);
        assertTrue(employeeService.existsById(employeeExpectedPm.getId()));
    }

    @Test
    public void existsByIdTest_false() {
        when(employeeRepository.existsById(employeeExpectedPm.getId())).thenReturn(false);
        assertFalse(employeeService.existsById(employeeExpectedPm.getId()));
    }

    @Test
    public void existsByEmailAddressTest() {
        when(employeeRepository.existsByEmailAddress(employeeExpectedPm.getEmailAddress())).thenReturn(true);
        assertTrue(employeeService.existsByEmailAddress(employeeExpectedPm.getEmailAddress()));
    }

    @Test
    public void existsByEmailAddressTest_false() {
        when(employeeRepository.existsByEmailAddress(employeeExpectedPm.getEmailAddress())).thenReturn(false);
        assertFalse(employeeService.existsByEmailAddress(employeeExpectedPm.getEmailAddress()));
    }
}
