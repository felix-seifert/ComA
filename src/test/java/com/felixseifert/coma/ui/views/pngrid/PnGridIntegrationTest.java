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

package com.felixseifert.coma.ui.views.pngrid;

import com.felixseifert.coma.backend.model.dto.PartNumberObjectGridDTO;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.model.enums.ValueGroup;
import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.PartNumberObject;
import com.felixseifert.coma.backend.model.SelectableValue;
import com.felixseifert.coma.backend.service.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PnGridIntegrationTest {

    @Autowired
    private PartNumberObjectService partNumberObjectService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SelectableValueService selectableValueService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private BusinessUnitService businessUnitService;

    @Autowired
    private CooPlantService cooPlantService;

    @Autowired
    private ProductDescriptionService productDescriptionService;

    private PartNumberObjectEditor partNumberObjectEditor;

    private PnGrid pnGrid;

    private PartNumberObject partNumberObject1;
    private PartNumberObject partNumberObject2;
    private PartNumberObject partNumberObject3;
    private Employee employee;
    private SelectableValue selectableValue;

    private static PartNumberObjectGridDTO partNumberObject1Dto;
    private static PartNumberObjectGridDTO partNumberObject2Dto;
    private static PartNumberObjectGridDTO partNumberObject3Dto;

    @Mock
    private UI ui;

    @Before
    public void setup() {
        when(ui.getLocale()).thenReturn(Locale.ENGLISH);
        UI.setCurrent(ui);

        // Save Employee and update after opening editor
        employee = new Employee();
        employee.setEmailAddress("integration@bosch.com");
        employee.setName("Old Name");
        employee.setTeam("SMS");
        employee.addRole(Role.PRODUCT_SPECIALIST);
        employeeService.postEmployee(employee);

        partNumberObjectEditor = new PartNumberObjectEditor(partNumberObjectService, employeeService,
                selectableValueService, customerService, businessUnitService, cooPlantService,
                productDescriptionService);
        pnGrid = new PnGrid(partNumberObjectEditor, partNumberObjectService);

        partNumberObject1 = new PartNumberObject();
        partNumberObject1.setPn("0123456789");
        partNumberObject1.setIdx("abc");
        partNumberObject1.setStartOfProduction(LocalDate.parse("2019-11-01"));
        partNumberObject1.setCreatedByEmployee(employee);
        partNumberObject1.setCreatedByTeamDepartment(employee.getTeam());
        partNumberObject2 = new PartNumberObject();
        partNumberObject2.setPn("0123456789");
        partNumberObject2.setStartOfProduction(LocalDate.parse("2019-11-01"));
        partNumberObject2.setCreatedByEmployee(employee);
        partNumberObject2.setCreatedByTeamDepartment(employee.getTeam());
        partNumberObject3 = new PartNumberObject();
        partNumberObject3.setPn("0987654321");
        partNumberObject3.setStartOfProduction(LocalDate.parse("2019-11-01"));
        partNumberObject3.setCreatedByEmployee(employee);
        partNumberObject3.setCreatedByTeamDepartment(employee.getTeam());
        partNumberObjectService.postPartNumberObject(partNumberObject1);
        partNumberObjectService.postPartNumberObject(partNumberObject2);
        partNumberObjectService.postPartNumberObject(partNumberObject3);

        String employeeName = "Heinrich Hanzen";

        partNumberObject1Dto = new PartNumberObjectGridDTO(partNumberObject1.getId(),
                partNumberObject1.getPn(), partNumberObject1.getDueDate(),
                partNumberObject1.getCreatedByTeamDepartment(), partNumberObject1.getCreatedDate(),
                employeeName, Role.REQUESTER, null, null, null);
        partNumberObject2Dto = new PartNumberObjectGridDTO(partNumberObject2.getId(),
                partNumberObject2.getPn(), partNumberObject2.getDueDate(),
                partNumberObject2.getCreatedByTeamDepartment(), partNumberObject2.getCreatedDate(),
                employeeName, Role.REQUESTER, null, null, null);
        partNumberObject3Dto = new PartNumberObjectGridDTO(partNumberObject3.getId(),
                partNumberObject3.getPn(), partNumberObject3.getDueDate(),
                partNumberObject3.getCreatedByTeamDepartment(), partNumberObject3.getCreatedDate(),
                employeeName, Role.REQUESTER, null, null, null);

        // Create Entity after creation of PartNumberObjectEditor
        selectableValue = new SelectableValue();
        selectableValue.setLabel("Good Value");
        selectableValue.setValueGroup(ValueGroup.CHANNEL_PROFILE);
        selectableValueService.postSelectableValue(selectableValue);

        employee.setName("New Name");
        employeeService.putEmployee(employee);
    }

    @Test
    @Transactional
    public void callCreateButtonAndSaveTest() {
        pnGrid.getCreateButton().click();

        assertTrue(partNumberObjectEditor.isOpened());
        assertFalse(partNumberObjectEditor.getPnField().isReadOnly());
        assertTrue(partNumberObjectEditor.getDueDateField().getMin().isEqual(LocalDate.now()));

        partNumberObjectEditor.getPnField().setValue("0123456789");
        partNumberObjectEditor.getStartOfProductionField().setValue(LocalDate.parse("2019-11-01"));
        partNumberObjectEditor.getCreatedByComboBox().setValue(employee);
        partNumberObjectEditor.getCooComboBox().setValue("IC (Interesting Country)");
        partNumberObjectEditor.getPredecessorField().setValue("0123456789123");

        commonAssertions();

        partNumberObjectEditor.getIdxField().setValue("new");
        partNumberObjectEditor.getSaveButton().click();

        assertEquals(4, pnGrid.getGrid().getDataProvider().size(new Query<>()));
        assertTrue(pnGrid.getGrid().getDataProvider().fetch(new Query<>())
                .filter(pnDto -> pnDto.getPn().equals("0123456789"))
                .anyMatch(pnDto -> pnDto.getCreatedByTeamDepartment().equals("SMS")));
    }

    @Test
    @Transactional
    public void editTest() {
        partNumberObjectEditor.openEditor(partNumberObject2Dto);
        assertTrue(partNumberObjectEditor.isOpened());
        assertTrue(partNumberObjectEditor.getPnField().isReadOnly());

        commonAssertions();

        partNumberObjectEditor.getIdxField().setValue("old");
        partNumberObjectEditor.getEmployeeFields().get(Role.PRODUCT_SPECIALIST).setValue(employee);
        partNumberObjectEditor.getSaveButton().click();

        assertEquals(3, pnGrid.getGrid().getDataProvider().size(new Query<>()));
        assertEquals(employee, partNumberObject2.getProductSpecialist());
    }

    private void commonAssertions() {
        assertEquals(List.of(employee),
                partNumberObjectEditor.getEmployeeFields().get(Role.PRODUCT_SPECIALIST).getDataProvider()
                .fetch(new Query<>()).collect(Collectors.toList()));
        assertEquals(List.of(selectableValue.getLabel()), partNumberObjectEditor.getProfileMultiselect()
                .getDataProvider().fetch(new Query<>()).collect(Collectors.toList()));
        // Resulting complete PN is already used
        partNumberObjectEditor.getIdxField().setValue("abc");
        assertTrue(partNumberObjectEditor.getIdxField().isInvalid());
    }

    @Test
    @Transactional
    public void callCreateAndCancelTest() {
        pnGrid.getCreateButton().click();
        assertTrue(partNumberObjectEditor.isOpened());
        partNumberObjectEditor.getCancelButton().click();
        assertFalse(partNumberObjectEditor.isOpened());
    }

    @Test
    @Transactional
    public void deleteConfirmTest() {
        partNumberObjectEditor.openEditor(partNumberObject2Dto);
        partNumberObjectEditor.getDeleteButton().click();
        assertTrue(partNumberObjectEditor.getDeleteConfirmationDialog().isOpened());
        assertTrue(partNumberObjectEditor.getDeleteConfirmationDialog().getHeading().getText()
                .toLowerCase().contains("delete"));
        assertTrue(partNumberObjectEditor.getDeleteConfirmationDialog().getTextField().getValue()
                .toLowerCase().contains(partNumberObject2.getPn()));
        partNumberObjectEditor.getDeleteConfirmationDialog().getConfirmationButton().click();
        assertFalse(partNumberObjectEditor.isOpened());
        assertEquals(2, pnGrid.getGrid().getDataProvider().size(new Query<>()));
    }

    @Test
    @Transactional
    public void deleteCancelTest() {
        partNumberObjectEditor.openEditor(partNumberObject2Dto);
        partNumberObjectEditor.getDeleteButton().click();
        partNumberObjectEditor.getDeleteConfirmationDialog().getCancelButton().click();
        assertFalse(partNumberObjectEditor.getDeleteConfirmationDialog().isOpened());
        partNumberObjectEditor.getCancelButton().click();
        assertEquals(3, pnGrid.getGrid().getDataProvider().size(new Query<>()));
    }

    @Test
    @Transactional
    public void filterTest() {
        pnGrid.getFilter().setValue("01");
        assertEquals(2,
                pnGrid.getGrid().getDataProvider().fetch(new Query<>()).count());
        assertTrue(pnGrid.getGrid().getDataProvider().fetch(new Query<>()).collect(Collectors.toList())
                .containsAll(List.of(partNumberObject2Dto, partNumberObject1Dto)));
        pnGrid.getFilter().setValue("");
        assertEquals(3,
                pnGrid.getGrid().getDataProvider().fetch(new Query<>()).count());
        assertTrue(pnGrid.getGrid().getDataProvider().fetch(new Query<>()).collect(Collectors.toList())
                .containsAll(List.of(partNumberObject3Dto, partNumberObject2Dto, partNumberObject1Dto)));
        pnGrid.getFilter().setValue(" ");
        assertEquals(3,
                pnGrid.getGrid().getDataProvider().fetch(new Query<>()).count());
        assertTrue(pnGrid.getGrid().getDataProvider().fetch(new Query<>()).collect(Collectors.toList())
                .containsAll(List.of(partNumberObject3Dto, partNumberObject2Dto, partNumberObject1Dto)));
    }
}
