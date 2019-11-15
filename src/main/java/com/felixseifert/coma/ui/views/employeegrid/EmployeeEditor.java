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

package com.felixseifert.coma.ui.views.employeegrid;

import com.felixseifert.coma.backend.exceptions.DependencyException;
import com.felixseifert.coma.backend.model.Employee_;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.model.enums.ValueGroup;
import com.felixseifert.coma.backend.service.*;
import com.felixseifert.coma.ui.common.ChangeHandler;
import com.felixseifert.coma.ui.common.dialogs.DeleteConfirmationDialog;
import com.felixseifert.coma.ui.common.dialogs.SaveConfirmationDialog;
import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.SelectableValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringComponent
@UIScope
public class EmployeeEditor extends Dialog implements KeyNotifier {

    private static final long serialVersionUID = 1L;

    private EmployeeService employeeService;

    private SelectableValueService selectableValueService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private BusinessUnitService businessUnitService;

    @Autowired
    private PartNumberObjectService partNumberObjectService;

    private H3 heading = new H3();

    private TextField nameField = new TextField("Name");
    private EmailField emailField = new EmailField("Email Address");

    private CheckboxGroup<Role> roleCheckboxGroup = new CheckboxGroup<>();

    private ComboBox<String> teamComboBox = new ComboBox<>("Team");
    private ComboBox<String> locationComboBox = new ComboBox<>("Location");

    private Button saveButton = new Button("Save", VaadinIcon.CHECK.create());
    private Button cancelButton = new Button("Cancel");
    private Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());

    private SaveConfirmationDialog saveTeamConfirmationDialog;
    private SaveConfirmationDialog saveLocationConfirmationDialog;

    private DeleteConfirmationDialog deleteConfirmationDialog;

    private Binder<Employee> employeeBinder = new Binder<>(Employee.class);

    @Setter
    private ChangeHandler changeHandler;

    public EmployeeEditor(EmployeeService employeeService, SelectableValueService selectableValueService) {

        this.employeeService = employeeService;
        this.selectableValueService = selectableValueService;

        saveTeamConfirmationDialog =
                new SaveConfirmationDialog("Save " + ValueGroup.DEPARTMENT.getFieldDescription(),
                        "Team to save", "Do you want to create this team?");
        saveTeamConfirmationDialog.setChangeHandler(() ->
                selectableValueService.postSelectableValue(teamComboBox.getValue(), ValueGroup.DEPARTMENT));
        saveLocationConfirmationDialog =
                new SaveConfirmationDialog("Save " + ValueGroup.LOCATION.getFieldDescription(),
                        "Location to save", "Do you want to create this location?");
        saveLocationConfirmationDialog.setChangeHandler(() ->
                selectableValueService.postSelectableValue(locationComboBox.getValue(), ValueGroup.LOCATION));

        nameField.setWidthFull();
        nameField.setMaxLength(100);
        emailField.setWidthFull();
        emailField.setMaxLength(100);

        teamComboBox.setWidthFull();
        teamComboBox.setClearButtonVisible(true);
        teamComboBox.addCustomValueSetListener(e -> {
            teamComboBox.setValue(e.getDetail());
            if(!selectableValueService.exists(e.getDetail(), ValueGroup.DEPARTMENT)) {
                saveTeamConfirmationDialog.open(e.getDetail());     // Todo: Add restriction of max length (see model)
            }
        });
        locationComboBox.setWidthFull();
        locationComboBox.setClearButtonVisible(true);
        locationComboBox.addCustomValueSetListener(e -> {
            locationComboBox.setValue(e.getDetail());
            if(!selectableValueService.exists(e.getDetail(), ValueGroup.LOCATION)) {
                saveLocationConfirmationDialog.open(e.getDetail()); // Todo: Add restriction of max length (see model)
            }
        });

        roleCheckboxGroup.setLabel("Employee's Roles");
        roleCheckboxGroup.setItemLabelGenerator(Role::getName);

        VerticalLayout form =
                new VerticalLayout(nameField, emailField, teamComboBox, locationComboBox, roleCheckboxGroup);

        saveButton.getElement().getThemeList().add("primary");
        deleteButton.getElement().getThemeList().add("error");

        HorizontalLayout actionLeft = new HorizontalLayout(deleteButton);
        HorizontalLayout actionsRight = new HorizontalLayout(cancelButton, saveButton);
        HorizontalLayout actions = new HorizontalLayout(actionLeft, actionsRight);

        actionLeft.setWidth("40%");
        actionsRight.setWidth("60%");
        actionLeft.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        actionsRight.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.setWidthFull();

        form.setMinWidth("320px");
        form.setPadding(false);
        form.setMargin(false);
        form.setWidthFull();

        reloadComboBoxes(null);

        this.add(heading, form, actions);

        employeeBinder.forField(nameField).asRequired("Please fill in name.").bind(Employee_.NAME);
        employeeBinder.forField(emailField).asRequired("Please fill in email address.")
                .withValidator(new EmailValidator("Please fill in a correct email address."))
                .withValidator(email -> email.endsWith("bosch.com"), "Email address must end with bosch.com")
                .withValidator(this::emailAddressExistsValidator, "Another Employee uses email address already.")
                .bind(Employee_.EMAIL_ADDRESS);
        employeeBinder.forField(teamComboBox).asRequired("Please fill in team.").bind(Employee_.TEAM);
        employeeBinder.forField(locationComboBox).bind(Employee_.LOCATION);
        employeeBinder.forField(roleCheckboxGroup).bind(Employee_.ROLES);

        employeeBinder.addStatusChangeListener(status -> {
            Stream<String> fieldsToCheck = Stream.of(Employee_.EMAIL_ADDRESS, Employee_.NAME, Employee_.TEAM);
            boolean emptyFields = fieldsToCheck
                    .flatMap(prop -> employeeBinder.getBinding(prop).stream())
                    .anyMatch(binding -> binding.getField().isEmpty());
            saveButton.setEnabled(!status.hasValidationErrors() && !emptyFields);
        });

        // Dialog for delete confirmation and handler what to do when delete confirmed
        deleteConfirmationDialog = new DeleteConfirmationDialog("Delete Employee", "Email Address");
        deleteConfirmationDialog.setChangeHandler(this::deleteConfirmationHandler);

        // Listeners for form buttons
        addKeyPressListener(Key.ENTER, e -> saveButtonListener());
        saveButton.addClickListener(e -> saveButtonListener());
        deleteButton.addClickListener(e -> deleteButtonListener());
        cancelButton.addClickListener(e -> changeHandler.onChange());

        this.close();
        this.setCloseOnOutsideClick(false);
        this.setCloseOnEsc(false);
    }

    private void reloadComboBoxes(Employee employee) {
        teamComboBox.setItems(selectableValueService.getSelectableValuesByValueGroup(ValueGroup.DEPARTMENT)
                .stream().map(SelectableValue::getLabel));
        locationComboBox.setItems(selectableValueService.getSelectableValuesByValueGroup(ValueGroup.LOCATION)
                .stream().map(SelectableValue::getLabel));
        roleCheckboxGroup.setItems(generateRoleItems(employee));
    }

    private List<Role> generateRoleItems(Employee employee) {
        List<Role> generatedItems = new ArrayList<>(Role.values().length - 2);
        if(employee != null) generatedItems.addAll(employee.getRoles());

        Arrays.stream(Role.values()).filter(r -> !generatedItems.contains(r))
                .filter(r -> !r.equals(Role.UNKNOWN) && !r.equals(Role.REQUESTER)).forEach(generatedItems::add);

        return generatedItems;
    }

    protected void openEditor(Employee employee) {
        if(employee == null) {
            this.close();
            return;
        }
        boolean persisted = employee.getId() != null;
        deleteButton.setVisible(persisted);
        reloadComboBoxes(employee);
        if(persisted) {
            employee = employeeService.getEmployeeByEmailAddress(employee.getEmailAddress());
            heading.setText("Edit Employee");
        }
        else {
            heading.setText("New Employee");
        }
        employeeBinder.setBean(employee);
        nameField.focus();
        saveButton.setEnabled(false);
        this.open();
    }

    private void saveButtonListener() {
        Employee employee = employeeBinder.getBean();

        if(employee.getId() != null) {

            Employee oldEmployee = employeeService.getEmployeeByEmailAddress(employee.getEmailAddress());

            Set<Role> removedRoles = oldEmployee.getRoles();
            removedRoles.removeAll(employee.getRoles());

            int numberRelationshipsToPns = removedRoles.stream()
                    .mapToInt(r -> partNumberObjectService.countPartNumberObjects(employee, r)).sum();
            int numberRelationshipsToBus = removedRoles.stream().filter(r -> r.equals(Role.PRODUCT_MANAGER))
                    .mapToInt(r -> businessUnitService.count(oldEmployee)).sum();

            // If relationships exist for deleted roles, remove relationships before updating employee.
            if(numberRelationshipsToPns > 0 || numberRelationshipsToBus > 0) {
                updateRolesRemoved(employee, removedRoles, numberRelationshipsToPns, numberRelationshipsToBus);
            }
            else {
                put(employee);
            }
        }
        else {
            post(employee);
        }
        changeHandler.onChange();
    }

    private void updateRolesRemoved(Employee employee, Set<Role> removedRoles, int numberRelationshipsToPns,
                                    int numberRelationshipsToBus) {

        String question = "When you save the employee like this, the mentioned references would get removed " +
                "(PNs and BUs would NOT get deleted).";
        SaveConfirmationDialog saveConfirmationDialog = new SaveConfirmationDialog(
                "Remove Roles", "Roles to remove", question);
        saveConfirmationDialog.setChangeHandler(() -> {
            relationshipService.removeRelationships(employee, removedRoles);
            put(employeeBinder.getBean());
        });

        if(numberRelationshipsToBus > 0) {
            saveConfirmationDialog.getOptionalArea().add(new Label(String.format("You want to remove a role " +
                    "which is referenced in %d BUs.", numberRelationshipsToBus)));
        }

        if(numberRelationshipsToPns > 0) {
            saveConfirmationDialog.getOptionalArea().add(new Label(String.format("You want to remove a role " +
                    "which is referenced in %d PNs.", numberRelationshipsToPns)));
        }

        saveConfirmationDialog.open(removedRoles.stream().map(Role::getName)
                .collect(Collectors.joining(", ")));
    }

    private void put(Employee employee) {
        employeeService.putEmployee(employee);
        Notification.show(String.format("Employee with email address %s updated", employee.getEmailAddress()));
    }

    private void post(Employee employee) {
        employeeService.postEmployee(employee);
        Notification.show(String.format("Employee with email address %s created", employee.getEmailAddress()));
    }

    private void deleteButtonListener() {
        Employee employee = employeeBinder.getBean();
        int numberResponsibilities = partNumberObjectService.countPartNumberObjects(employee);
        int numberCreator = partNumberObjectService.countPartNumberObjects(employee, Role.REQUESTER);
        long numberCurrent = partNumberObjectService.countPartNumberObjectsCurrentlyResponsible(employee);
        deleteConfirmationDialog.getOptionalArea().removeAll();
        if(numberResponsibilities > 0 || numberCreator > 0 || numberCurrent > 0) {
            deleteConfirmationDialog.getOptionalArea().add(
                    new Label(String.format("The Employee has responsibilities in %d PNs, is creator of %d PNs " +
                            "and has the current responsibility in %d PNs. These references would get " +
                            "removed (PNs would NOT get deleted).",
                            numberResponsibilities, numberCreator, numberCurrent)));
        }
        if(employee.getBusinessUnitsAsPM() != null && employee.getBusinessUnitsAsPM().size() > 0) {

            int numberAssignedBusinessUnits = employee.getBusinessUnitsAsPM().size();

            deleteConfirmationDialog.getOptionalArea().add(
                    new Label(String.format("The Employee is PM in %d BUs. These BUs would not have a PM anymore " +
                            "(BUs would NOT get deleted).", numberAssignedBusinessUnits)));
        }
        deleteConfirmationDialog.open(employee.getEmailAddress());
    }

    private void deleteConfirmationHandler() {
        Employee employee = employeeBinder.getBean();
        try {
            relationshipService.removeRelationships(employee);
            employeeService.deleteEmployee(employee);
            Notification.show(String.format("Employee with email address %s deleted", employee.getEmailAddress()));
        } catch (DependencyException e) {
            new Dialog(new Text("Removing the dependencies was not possible. The Employee still exists.")).open();
            e.printStackTrace();
        }
        changeHandler.onChange();
    }

    private boolean emailAddressExistsValidator(String emailAddress) {
        Employee employee = employeeBinder.getBean();
        if(employee.getId() != null && employeeService.existsByEmailAddress(emailAddress)) {
            Employee employee1ByEmailAddress = employeeService.getEmployeeByEmailAddress(emailAddress);
            return employee.getId().equals(employee1ByEmailAddress.getId());
        }
        return !employeeService.existsByEmailAddress(emailAddress);
    }
}
