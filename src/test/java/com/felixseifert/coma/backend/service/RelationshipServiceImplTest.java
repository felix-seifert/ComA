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
import com.felixseifert.coma.backend.model.PartNumberObject;
import com.felixseifert.coma.backend.model.enums.Role;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RelationshipServiceImplTest {

    @Autowired
    private RelationshipServiceImpl relationshipService;

    @MockBean
    private PartNumberObjectService partNumberObjectService;

    @MockBean
    private BusinessUnitService businessUnitService;

    private Employee employee1;
    private Employee employee2;
    private BusinessUnit businessUnit;
    private PartNumberObject partNumberObject1;
    private PartNumberObject partNumberObject2;
    private PartNumberObject partNumberObject3;

    @Before
    public void setUp() {

        employee1 = new Employee();
        employee1.addRole(Role.PRODUCT_SPECIALIST);
        employee1.addRole(Role.PRODUCT_MANAGER);
        employee2 = new Employee();
        employee2.addRole(Role.PRODUCT_SPECIALIST);

        partNumberObject1 = new PartNumberObject();
        partNumberObject1.setPn("0123456789");
        partNumberObject1.setStartOfProduction(LocalDate.parse("2019-11-01"));
        partNumberObject1.setProductSpecialist(employee1);
        partNumberObject1.setProductManager(employee1);
        partNumberObject2 = new PartNumberObject();
        partNumberObject2.setPn("0123456798");
        partNumberObject2.setStartOfProduction(LocalDate.parse("2019-12-01"));
        partNumberObject2.setProductSpecialist(employee1);
        partNumberObject3 = new PartNumberObject();
        partNumberObject3.setPn("0123456978");
        partNumberObject3.setStartOfProduction(LocalDate.parse("2019-10-01"));
        partNumberObject3.setProductSpecialist(employee2);
        partNumberObject3.setProductManager(employee1);

        businessUnit = new BusinessUnit();
        businessUnit.setName("BU");
        employee1.addBusinessUnitAsPM(businessUnit);
    }

    @Test
    public void removeRelationshipsTest_employee() {
        relationshipService.removeRelationships(employee1);
        verify(businessUnitService).removeBusinessUnitsForProductManager(employee1);
        employee1.getRoles().forEach(r -> verify(partNumberObjectService).removePNsForEmployeeByRole(employee1, r));
    }

    @Test
    public void removeRelationshipsTest_employeeAndRoles() {
        relationshipService.removeRelationships(employee1, Set.of(Role.PRODUCT_SPECIALIST));
        verify(businessUnitService, never()).removeBusinessUnitsForProductManager(employee1);
        verify(partNumberObjectService).removePNsForEmployeeByRole(employee1, Role.PRODUCT_SPECIALIST);
    }

    @Test
    public void removeRelationshipsTest_employeeNoRelationships() {
        employee2.getRoles().clear();
        relationshipService.removeRelationships(employee2);
        verify(businessUnitService, never()).removeBusinessUnitsForProductManager(any(Employee.class));
        verify(partNumberObjectService, times(1)).removePNsForEmployeeByRole(employee2, Role.REQUESTER);
    }

    @Test
    public void removeRelationshipsTest_businessUnit() {
        relationshipService.removeRelationships(businessUnit);
        verify(partNumberObjectService).removePNsForBusinessUnit(businessUnit);
    }
}