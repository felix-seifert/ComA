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
import com.felixseifert.coma.backend.model.BusinessUnit;
import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.repos.BusinessUnitRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BusinessUnitServiceImplTest {

    @Autowired
    private BusinessUnitServiceImpl businessUnitService;

    @MockBean
    private BusinessUnitRepository businessUnitRepository;

    @MockBean
    private PartNumberObjectService partNumberObjectService;

    private static BusinessUnit businessUnitExpected1;
    private static BusinessUnit businessUnitExpected2;
    private static Employee employee;
    private static List<BusinessUnit> listExpected;

    @BeforeClass
    public static void setup() {
        employee = new Employee();
        employee.setName("Andree Andersen");
        employee.setEmailAddress("andree.andersen@bosch.com");
        employee.addRole(Role.PRODUCT_MANAGER);
        businessUnitExpected1 = new BusinessUnit();
        businessUnitExpected1.setId(1);
        businessUnitExpected1.setName("PS-G");
        employee.addBusinessUnitAsPM(businessUnitExpected1);
        businessUnitExpected2 = new BusinessUnit();
        businessUnitExpected2.setName("AE");
        listExpected = List.of(businessUnitExpected1, businessUnitExpected2);
    }

    @Test
    public void getAllBusinessUnitsTest() {
        when(businessUnitRepository.findAllBusinessUnits()).thenReturn(listExpected);
        List<BusinessUnit> actual = businessUnitService.getAllBusinessUnits();
        assertEquals(listExpected, actual);
    }

    @Test
    public void getAllBusinessUnitsTest_noBUFound() {
        when(businessUnitRepository.findAllBusinessUnits()).thenReturn(List.of());
        List<BusinessUnit> actual = businessUnitService.getAllBusinessUnits();
        assertEquals(List.of(), actual);
    }

    @Test
    public void getBusinessUnitByNameTest() {
        when(businessUnitRepository.findByName(businessUnitExpected1.getName()))
                .thenReturn(Optional.of(businessUnitExpected1));
        BusinessUnit actual = businessUnitService.getBusinessUnitByName(businessUnitExpected1.getName());
        assertEquals(businessUnitExpected1, actual);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void getBusinessUnitByName_noBUFound() {
        when(businessUnitRepository.findByName(anyString())).thenReturn(Optional.empty());
        businessUnitService.getBusinessUnitByName("blub");
    }

    @Test
    public void postBusinessUnitTest() {
        when(businessUnitRepository.save(businessUnitExpected2)).thenReturn(businessUnitExpected2);
        assertEquals(businessUnitExpected2, businessUnitService.postBusinessUnit(businessUnitExpected2));
        verify(businessUnitRepository).save(businessUnitExpected2);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postBusinessUnitTest_nameNull() {
        BusinessUnit businessUnitEmptyName = new BusinessUnit();
        businessUnitEmptyName.setName(null);
        businessUnitService.postBusinessUnit(businessUnitEmptyName);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postBusinessUnitTest_nameBlank() {
        BusinessUnit businessUnitBlankName = new BusinessUnit();
        businessUnitBlankName.setName(" ");
        businessUnitService.postBusinessUnit(businessUnitBlankName);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void postBusinessUnitTest_nameExists() {
        when(businessUnitRepository.existsByName(businessUnitExpected1.getName())).thenReturn(true);
        businessUnitService.postBusinessUnit(businessUnitExpected1);
    }

    @Test
    public void putBusinessUnitTest() {
        when(businessUnitRepository.save(businessUnitExpected1)).thenReturn(businessUnitExpected1);
        assertEquals(businessUnitExpected1, businessUnitService.putBusinessUnit(businessUnitExpected1));
        verify(businessUnitRepository).save(businessUnitExpected1);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putBusinessUnitTest_nameNull() {
        BusinessUnit businessUnitEmptyName = new BusinessUnit();
        businessUnitEmptyName.setName(null);
        businessUnitService.putBusinessUnit(businessUnitEmptyName);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putBusinessUnitTest_nameBlank() {
        BusinessUnit businessUnitBlankName = new BusinessUnit();
        businessUnitBlankName.setName(" ");
        businessUnitService.putBusinessUnit(businessUnitBlankName);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void putBusinessUnitTest_nameDoesNotExist() {
        businessUnitService.putBusinessUnit(businessUnitExpected2);
    }

    @Test
    public void deleteBusinessUnitTest() throws DependencyException {
        when(businessUnitRepository.existsByName(businessUnitExpected1.getName())).thenReturn(true);
        when(partNumberObjectService.countPartNumberObjects(businessUnitExpected1)).thenReturn(0);
        businessUnitService.deleteBusinessUnit(businessUnitExpected1);
        verify(businessUnitRepository).delete(businessUnitExpected1);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void deleteBusinessUnitTest_buDoesNotExists() throws DependencyException {
        when(businessUnitRepository.existsByName(businessUnitExpected1.getName())).thenReturn(false);
        businessUnitService.deleteBusinessUnit(businessUnitExpected1);
    }

    @Test(expected = DependencyException.class)
    public void deleteBusinessUnitTest_hasDependencies() throws DependencyException {
        when(businessUnitRepository.existsByName(businessUnitExpected1.getName())).thenReturn(true);
        when(partNumberObjectService.countPartNumberObjects(businessUnitExpected1)).thenReturn(2);
        businessUnitService.deleteBusinessUnit(businessUnitExpected1);
    }

    @Test
    public void existsTest_true() {
        when(businessUnitRepository.existsByName(businessUnitExpected1.getName())).thenReturn(true);
        assertTrue(businessUnitService.exists(businessUnitExpected1.getName()));
    }

    @Test
    public void existsTest_false() {
        when(businessUnitRepository.existsByName(businessUnitExpected1.getName())).thenReturn(false);
        assertFalse(businessUnitService.exists(businessUnitExpected1.getName()));
    }
}
