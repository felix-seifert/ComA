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

import com.felixseifert.coma.backend.config.BeanGetter;
import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.PartNumberObject;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.service.EmployeeService;
import com.felixseifert.coma.backend.service.PartNumberObjectService;
import com.felixseifert.coma.ui.common.ViewConstants;
import com.felixseifert.coma.ui.common.dialogs.AbstractDialog;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SelectResponsibilityDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private EmployeeService employeeService;

    private PartNumberObjectService partNumberObjectService;

    private Consumer<PartNumberObject> openEditorOperation;

    private ComboBox<Employee> employeeComboBox = new ComboBox<>();

    private LinkedHashMap<TextField, Function<PartNumberObject, String>> textFieldMap = new LinkedHashMap<>();

    SelectResponsibilityDialog(Consumer<PartNumberObject> openEditorOperation) {

        this.openEditorOperation = openEditorOperation;

        employeeService = BeanGetter.getBean(EmployeeService.class);
        partNumberObjectService = BeanGetter.getBean(PartNumberObjectService.class);

        textFieldMap.put(new TextField("PN"), PartNumberObject::getPn);
        textFieldMap.put(new TextField("Index"),
                pn -> StringUtils.isNotBlank(pn.getIdx()) ? pn.getPn() : "");
        textFieldMap.put(new TextField("Complete PN"),
                pn -> StringUtils.isNotBlank(pn.getIdx()) ? pn.getPn() + pn.getIdx() : "");
        textFieldMap.put(new TextField("Complete Source Pn"),
                pn -> StringUtils.isNotBlank(pn.getCompleteSourcePn()) ? pn.getCompleteSourcePn() : "");
        textFieldMap.put(new TextField("Request Created by (Employee)"),
                pn -> pn.getCreatedByEmployee() != null ? pn.getCreatedByEmployee().getName() : "");
        textFieldMap.put(new TextField("Request Created by (Team)"),
                pn -> StringUtils.isNotBlank(pn.getCreatedByTeamDepartment()) ? pn.getCreatedByTeamDepartment() : "");
        textFieldMap.put(new TextField("Date Request Created"),
                pn -> pn.getCreatedDate() != null ? pn.getCreatedDate().toString() : "");
        textFieldMap.put(new TextField("Due Date"),
                pn -> pn.getDueDate() != null ? pn.getDueDate().toString() : "");
        textFieldMap.put(new TextField("Business Unit"),
                pn -> pn.getBusinessUnit() != null ? pn.getBusinessUnit().getName() : "");

        addTextFields();

        employeeComboBox.setWidthFull();
        employeeComboBox.setItemLabelGenerator(Employee::getName);
        employeeComboBox.addValueChangeListener(event -> {
            if(event.getValue() != null) getOkButton().setEnabled(true);
        });
        getContent().add(employeeComboBox);

        getOkButton().setIcon(VaadinIcon.CHECK.create());
        getOkButton().getElement().getThemeList().add("primary");
    }

    private void addTextFields() {
        int entries = 0;
        HorizontalLayout horizontalLayout = null;
        for(Map.Entry<TextField, Function<PartNumberObject, String>> entry : textFieldMap.entrySet()) {
            if(entries % 2 == 0) {
                horizontalLayout = new HorizontalLayout();
                getContent().add(horizontalLayout);
            }
            entry.getKey().setReadOnly(true);
            horizontalLayout.add(entry.getKey());

            entries++;

            if(entries > 8) {
                entry.getKey().setMinWidth(
                        "calc(" + (2 * ViewConstants.MIN_WIDTH_FOR_FIELD) + "px + var(--lumo-space-m))");
                continue;
            }
            entry.getKey().setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        }
    }

    public void open(PartNumberObject partNumberObject) {
        setTextAndLabels(partNumberObject);

        loadFields(partNumberObject);

        setChangeHandler(() -> changeHandler(partNumberObject));

        getOkButton().setEnabled(false);
        this.open();
    }

    private void setTextAndLabels(PartNumberObject partNumberObject) {

        if(!isChangeRequester(partNumberObject)) {
            getHeading().setText(String.format("New %s",
                    partNumberObject.getTaskRelease().getCurrentEmployeesRole().getName()));
            employeeComboBox.setLabel(String.format("Select new %s",
                    partNumberObject.getTaskRelease().getCurrentEmployeesRole().getName()));
            getOkButton().setText(String.format("Confirm new %s",
                    partNumberObject.getTaskRelease().getCurrentEmployeesRole().getName()));
            return;
        }

        getHeading().setText("New " + Role.REQUESTER.getName());
        employeeComboBox.setLabel("Select new " + Role.REQUESTER.getName());
        getOkButton().setText("Confirm new " + Role.REQUESTER.getName());
    }

    private void loadFields(PartNumberObject partNumberObject) {
        textFieldMap.forEach((key, value) -> key.setValue(value.apply(partNumberObject)));

        if(isChangeRequester(partNumberObject)) {
            employeeComboBox.setItems(employeeService.getAllEmployees());
            return;
        }

        employeeComboBox.setItems(employeeService.getAllEmployeesByRole(
                partNumberObject.getTaskRelease().getCurrentEmployeesRole()));
    }

    private void changeHandler(PartNumberObject partNumberObject) {

        if(isChangeRequester(partNumberObject)) {
            partNumberObject.setCreatedByEmployee(employeeComboBox.getValue());
        }
        else {
            try {
                String methodName = "set" + partNumberObject.getTaskRelease().getCurrentEmployeesRole().getPnVariable()
                        .substring(0, 1).toUpperCase() +
                        partNumberObject.getTaskRelease().getCurrentEmployeesRole().getPnVariable().substring(1);
                Method setEmployee = partNumberObject.getClass().getMethod(methodName, Employee.class);
                setEmployee.invoke(partNumberObject, employeeComboBox.getValue());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        partNumberObject.getTaskRelease().setCurrentResponsibleEmployee(employeeComboBox.getValue());

        partNumberObjectService.putPartNumberObject(partNumberObject);
        openEditorOperation.accept(partNumberObject);
    }

    private boolean isChangeRequester(PartNumberObject partNumberObject) {
        return partNumberObject.getTaskRelease().getCurrentResponsibleEmployee() != null ||
                Role.REQUESTER.equals(partNumberObject.getTaskRelease().getCurrentEmployeesRole());
    }
}
