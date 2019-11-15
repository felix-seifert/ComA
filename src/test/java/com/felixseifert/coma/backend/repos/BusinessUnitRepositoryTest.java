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
public class BusinessUnitRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    private BusinessUnit businessUnitExpected1;
    private BusinessUnit businessUnitExpected2;
    private Employee employee;

    @Before
    public void setDatabase() {
        employee = new Employee();
        employee.setName("Andree Andersen");
        employee.setEmailAddress("andree.andersen@bosch.com");
        employee.addRole(Role.PRODUCT_MANAGER);
        businessUnitExpected1 = new BusinessUnit();
        businessUnitExpected1.setName("PS-G");
        employee.addBusinessUnitAsPM(businessUnitExpected1);
        businessUnitExpected2 = new BusinessUnit();
        businessUnitExpected2.setName("AE");

        testEntityManager.persist(employee);
        testEntityManager.persist(businessUnitExpected1);
        testEntityManager.persist(businessUnitExpected2);
        testEntityManager.flush();
    }

    @Test
    public void findAllTest() {
        List<BusinessUnit> listActual = businessUnitRepository.findAllBusinessUnits();
        assertEquals(List.of(businessUnitExpected1, businessUnitExpected2), listActual);
    }

    @Test
    public void findByNameTest() {
        Optional<BusinessUnit> businessUnitActual = businessUnitRepository.findByName(businessUnitExpected1.getName());
        Optional<BusinessUnit> businessUnitActualEmpty = businessUnitRepository.findByName("strange name");

        assertTrue(businessUnitActual.isPresent());
        assertEquals(businessUnitExpected1, businessUnitActual.get());
        assertFalse(businessUnitActualEmpty.isPresent());
    }

    @Test
    public void saveTest() {
        BusinessUnit businessUnitToSave = new BusinessUnit();
        businessUnitToSave.setName("ED");
        employee.addBusinessUnitAsPM(businessUnitToSave);
        assertNull(businessUnitToSave.getId());

        businessUnitRepository.save(businessUnitToSave);
        boolean exists = businessUnitRepository.existsByName(businessUnitToSave.getName());
        List<BusinessUnit> listActual = businessUnitRepository.findAllBusinessUnits();

        assertTrue(exists);
        assertNotNull(businessUnitToSave.getId());
        assertEquals(3, listActual.size());
        assertTrue(listActual.contains(businessUnitToSave));
    }

    @Test
    public void deleteTest() {
        businessUnitRepository.delete(businessUnitExpected1);

        boolean exists = businessUnitRepository.existsByName(businessUnitExpected1.getName());
        List<BusinessUnit> listActual = businessUnitRepository.findAllBusinessUnits();

        assertFalse(exists);
        assertEquals(1, listActual.size());
    }

    @Test
    public void existsByNameTest() {
        assertTrue(businessUnitRepository.existsByName(businessUnitExpected1.getName()));
        assertFalse(businessUnitRepository.existsByName("strange name"));
    }

    @Test
    public void existsByIdTest() {
        assertTrue(businessUnitRepository.existsById(businessUnitExpected1.getId()));
        assertFalse(businessUnitRepository.existsById(99));
    }
}
