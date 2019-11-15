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
import com.felixseifert.coma.backend.model.BusinessUnit;
import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.PartNumberObject;
import com.felixseifert.coma.backend.model.SelectableValue;
import com.felixseifert.coma.backend.service.*;
import com.vaadin.flow.component.UI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PartNumberObjectEditorTest {

    @Mock
    private PartNumberObjectService partNumberObjectService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private SelectableValueService selectableValueService;

    @Mock
    private CustomerService customerService;

    @Mock
    private BusinessUnitService businessUnitService;

    @Mock
    private CooPlantService cooPlantService;

    @Mock
    private ProductDescriptionService productDescriptionService;

    private PartNumberObjectEditor partNumberObjectEditor;

    private PartNumberObject partNumberObjectPersisted;
    private PartNumberObjectGridDTO partNumberObjectGridDTOPersisted;
    private BusinessUnit businessUnit;
    private Employee employee;
    private SelectableValue selectableValue;

    @Mock
    private UI ui;

    @Before
    public void setup() {
        when(ui.getLocale()).thenReturn(Locale.ENGLISH);
        UI.setCurrent(ui);

        partNumberObjectEditor = new PartNumberObjectEditor(partNumberObjectService, employeeService,
                selectableValueService, customerService, businessUnitService, cooPlantService,
                productDescriptionService);

        businessUnit = new BusinessUnit();
        businessUnit.setName("New Unit");

        employee = new Employee();
        employee.setEmailAddress("integration@bosch.com");
        employee.setName("Beautiful Name");
        employee.setTeam("RBR");
        employee.addRole(Role.PRODUCT_MANAGER);
        employee.addBusinessUnitAsPM(businessUnit);

        selectableValue = new SelectableValue();
        selectableValue.setLabel("Good Value");

        partNumberObjectPersisted = new PartNumberObject();

        partNumberObjectPersisted.setId(1);
        partNumberObjectPersisted.setPn("0123456789");
        partNumberObjectPersisted.setIdx("abc");
        partNumberObjectPersisted.setCreatedByTeamDepartment("SMS");
        partNumberObjectPersisted.setCreatedByEmployee(employee);
        partNumberObjectPersisted.setStartOfProduction(LocalDate.parse("2020-01-01"));
        partNumberObjectPersisted.setProductDescription(selectableValue.getLabel());
        partNumberObjectPersisted.setCooAbbreviation("IC");
        partNumberObjectPersisted.setCooName("Interesting Country");
        partNumberObjectPersisted.setProductManager(employee);
        partNumberObjectPersisted.setBusinessUnit(businessUnit);

        partNumberObjectPersisted.getTaskRelease().setCurrentResponsibleEmployee(employee);
        partNumberObjectPersisted.getTaskRelease().setCurrentEmployeesRole(
                employee.getRoles().stream().findFirst().get());

        String employeeName = "Heinrich Hanzen";

        partNumberObjectGridDTOPersisted = new PartNumberObjectGridDTO(
                partNumberObjectPersisted.getId(), partNumberObjectPersisted.getPn(),
                partNumberObjectPersisted.getDueDate(), partNumberObjectPersisted.getCreatedByTeamDepartment(),
                partNumberObjectPersisted.getCreatedDate(), employeeName, Role.REQUESTER, null, null, null);
    }

    @Test
    public void openEditorNullTest() {
        partNumberObjectEditor.openEditor(null);
        assertFalse(partNumberObjectEditor.isOpened());
    }

    @Test
    public void openEditorPersistedTest() {
        when(partNumberObjectService.getPartNumberObjectById(partNumberObjectPersisted.getId()))
                .thenReturn(partNumberObjectPersisted);
        partNumberObjectEditor.openEditor(partNumberObjectGridDTOPersisted);
        assertTrue(partNumberObjectEditor.getDeleteButton().isVisible());
        assertEquals(partNumberObjectPersisted.getPn() + partNumberObjectPersisted.getIdx(),
                partNumberObjectEditor.getCompletePnField().getValue());
        Assert.assertEquals(businessUnit, partNumberObjectEditor.getBusinessUnitComboBox().getValue());
        Assert.assertEquals(employee, partNumberObjectEditor.getEmployeeFields().get(Role.PRODUCT_MANAGER).getValue());
        assertEquals(String.format("%s (%s)", partNumberObjectPersisted.getCooAbbreviation(),
                partNumberObjectPersisted.getCooName()), partNumberObjectEditor.getCooComboBox().getValue());
        // Assert disabled saveButton when no changes in editor
        assertFalse(partNumberObjectEditor.getSaveButton().isEnabled());
        assertTrue(partNumberObjectEditor.getStartOfProductionField().getValue()
                .isBefore(partNumberObjectEditor.getEndOfProductionField().getMin()));
        commonAssertions();
        assertTrue(partNumberObjectEditor.getPredecessorField().isEnabled());
        partNumberObjectEditor.getPredecessorField().setValue("0123");
        assertTrue(partNumberObjectEditor.getPredecessorField().isInvalid());
        partNumberObjectEditor.getPredecessorField().setValue("0123456789123");
        assertFalse(partNumberObjectEditor.getPredecessorField().isInvalid());
    }

    @Test
    public void openEditorFreshTest() {
        partNumberObjectEditor.openEditor(new PartNumberObjectGridDTO());
        partNumberObjectEditor.getPnField().setValue("0123456789");
        partNumberObjectEditor.getCreatedByComboBox().setValue(employee);
        partNumberObjectEditor.getStartOfProductionField().setValue(LocalDate.parse("2050-11-01"));
        assertFalse(partNumberObjectEditor.getDeleteButton().isVisible());
        assertTrue(partNumberObjectEditor.getCompletePnField().getValue().toLowerCase().contains("generated"));
        assertTrue(partNumberObjectEditor.getCreatedDateField().getValue().isEqual(LocalDate.now()));
        assertTrue(partNumberObjectEditor.getDueDateField().getMin().isEqual(LocalDate.now()));
        commonAssertions();
        partNumberObjectEditor.getPnField().setValue("012345678");
        assertTrue(partNumberObjectEditor.getPnField().isInvalid());
    }

    private void commonAssertions() {
        checkRequiredFields();

        partNumberObjectEditor.getIdxField().setValue("de");
        assertTrue(partNumberObjectEditor.getIdxField().isInvalid());
        partNumberObjectEditor.getIdxField().setValue("def");
        // Enabled saveButton after changes
        assertTrue(partNumberObjectEditor.getSaveButton().isEnabled());
        // Disable saveButton when required field is empty
        partNumberObjectEditor.getCreatedByField().clear();
        assertFalse(partNumberObjectEditor.getSaveButton().isEnabled());
    }

    private void checkRequiredFields() {
        assertTrue(partNumberObjectEditor.getPnField().isRequired());
        assertTrue(partNumberObjectEditor.getCreatedByComboBox().isRequired());
    }
}
