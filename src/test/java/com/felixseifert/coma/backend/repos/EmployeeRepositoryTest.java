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
import com.felixseifert.coma.backend.model.enums.Role;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@DataJpaTest
@RunWith(SpringRunner.class)
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employeeExpectedPm;
    private Employee employeeExpectedPs;

    @Before
    public void setDatabase() {
        employeeExpectedPm = new Employee();
        employeeExpectedPm.setName("Berta Bold");
        employeeExpectedPm.setEmailAddress("berta.bold@jp.bosch.com");
        employeeExpectedPm.addRole(Role.PRODUCT_MANAGER);
        employeeExpectedPs = new Employee();
        employeeExpectedPs.setName("Carlos Creeds");
        employeeExpectedPs.setEmailAddress("carlos.creeds3@jp.bosch.com");
        employeeExpectedPs.addRole(Role.PRODUCT_SPECIALIST);

        testEntityManager.persist(employeeExpectedPm);
        testEntityManager.persist(employeeExpectedPs);
        testEntityManager.flush();
    }

    @Test
    public void findAllTest() {
        List<Employee> employeeListActual = employeeRepository.findAllEmployees();
        assertEquals(List.of(employeeExpectedPm, employeeExpectedPs), employeeListActual);
    }

    @Test
    public void findAllProductManagersTest() {
        List<Employee> employeeListActual = employeeRepository.findByRole(Role.PRODUCT_MANAGER);
        assertEquals(List.of(employeeExpectedPm), employeeListActual);
    }

    @Test
    public void findAllProductSpecialistsTest() {
        List<Employee> employeeListActual = employeeRepository.findByRole(Role.PRODUCT_SPECIALIST);
        assertEquals(List.of(employeeExpectedPs), employeeListActual);
    }

    @Test
    public void findByNameStartsWithIgnoreCaseTest() {
        List<Employee> employeeListActual =
                employeeRepository.findByNameStartsWithIgnoreCase("r");
        List<Employee> employeeListActualEmpty =
                employeeRepository.findByNameStartsWithIgnoreCase("zz");

        assertEquals(2, employeeListActual.size());
        assertTrue(employeeListActual.contains(employeeExpectedPs));
        assertTrue(employeeListActual.contains(employeeExpectedPm));
        assertTrue(employeeListActualEmpty.isEmpty());
    }

    @Test
    public void findByIdTest() {
        Optional<Employee> employeeActual = employeeRepository.findById(employeeExpectedPm.getId());
        Optional<Employee> employeeActualEmpty = employeeRepository.findById(99);

        assertTrue(employeeActual.isPresent());
        assertEquals(employeeExpectedPm, employeeActual.get());
        assertFalse(employeeActualEmpty.isPresent());
    }

    @Test
    public void findByEmailAddressTest() {
        Optional<Employee> employeeActual =
                employeeRepository.findByEmailAddress(employeeExpectedPm.getEmailAddress());
        Optional<Employee> employeeActualEmpty = employeeRepository.findByEmailAddress("testig.test@bosch.com");

        assertTrue(employeeActual.isPresent());
        assertEquals(employeeExpectedPm, employeeActual.get());
        assertFalse(employeeActualEmpty.isPresent());
    }

    @Test
    public void saveTest() {
        Employee employeeToSave = new Employee();
        employeeToSave.setName("Daniel Dudley");
        employeeToSave.setEmailAddress("daniel.dudley@jp.bosch.com");
        employeeToSave.addRole(Role.PRODUCT_SPECIALIST);

        Employee employeeActual = employeeRepository.save(employeeToSave);
        List<Employee> employeeListActual = employeeRepository.findAllEmployees();
        boolean employeeExists = employeeListActual.contains(employeeToSave);
        List<Employee> psListActual = employeeRepository.findByRole(Role.PRODUCT_SPECIALIST);
        boolean psExists = psListActual.contains(employeeToSave);

        assertEquals(employeeToSave, employeeActual);
        assertTrue(employeeExists);
        assertEquals(3, employeeListActual.size());
        assertEquals(employeeToSave, employeeListActual.get(2));
        assertTrue(psExists);
        assertEquals(2, psListActual.size());
    }

    @Test
    public void deleteTest() {
        employeeRepository.delete(employeeExpectedPs);

        List<Employee> employeeListActual = employeeRepository.findAllEmployees();
        boolean employeeExists = employeeListActual.contains(employeeExpectedPs);

        assertFalse(employeeExists);
        assertEquals(1, employeeListActual.size());
    }

    @Test
    public void existsByIdTest() {
        assertTrue(employeeRepository.existsById(employeeExpectedPm.getId()));
        assertFalse(employeeRepository.existsById(99));
    }

    @Test
    public void existsByEmailAddressTest() {
        assertTrue(employeeRepository.existsByEmailAddress(employeeExpectedPm.getEmailAddress()));
        assertFalse(employeeRepository.existsByEmailAddress("unknown@jp.bosch.com"));
    }
}
