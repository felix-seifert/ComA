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

package com.felixseifert.coma.ui.views.bugrid;

import com.felixseifert.coma.backend.exceptions.DependencyException;
import com.felixseifert.coma.backend.model.BusinessUnit;
import com.felixseifert.coma.backend.model.BusinessUnit_;
import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.service.BusinessUnitService;
import com.felixseifert.coma.backend.service.EmployeeService;
import com.felixseifert.coma.backend.service.PartNumberObjectService;
import com.felixseifert.coma.backend.service.RelationshipService;
import com.felixseifert.coma.ui.common.ChangeHandler;
import com.felixseifert.coma.ui.common.dialogs.DeleteConfirmationDialog;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Setter;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.Set;

@SpringComponent
@UIScope
public class BuEditor extends Dialog implements KeyNotifier {

    private static final long serialVersionUID = 1L;

    private BusinessUnitService businessUnitService;

    private PartNumberObjectService partNumberObjectService;

    private EmployeeService employeeService;

    private RelationshipService relationshipService;

    private H3 heading = new H3();

    private TextField nameField = new TextField("Business Unit's Name");
    private MultiselectComboBox<Employee> pmMultiselect = new MultiselectComboBox<>();

    private Button saveButton = new com.vaadin.flow.component.button.Button("Save", VaadinIcon.CHECK.create());
    private Button cancelButton = new com.vaadin.flow.component.button.Button("Cancel");
    private Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());

    private DeleteConfirmationDialog deleteConfirmationDialog;

    private Binder<BusinessUnit> businessUnitBinder = new Binder<>(BusinessUnit.class);

    @Setter
    private ChangeHandler changeHandler;

    private Set<String> mandatoryAttributesOfBusinessUnits = Set.of(BusinessUnit_.NAME);

    public BuEditor(BusinessUnitService businessUnitService, PartNumberObjectService partNumberObjectService,
                    EmployeeService employeeService, RelationshipService relationshipService) {

        this.businessUnitService = businessUnitService;
        this.partNumberObjectService = partNumberObjectService;
        this.employeeService = employeeService;
        this.relationshipService = relationshipService;

        nameField.setWidthFull();
        nameField.setMaxLength(50);
        nameField.focus();
        pmMultiselect.setLabel("Assigned Product Managers");
        pmMultiselect.setWidthFull();
        pmMultiselect.setItemLabelGenerator(Employee::getName);
        VerticalLayout form = new VerticalLayout(nameField, pmMultiselect);

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

        this.add(heading, form, actions);

        businessUnitBinder.forField(nameField)
                .asRequired("Please fill in name.")
                .withValidator(n -> !businessUnitService.exists(n), "Business Unit already exists.")
                .bind("name");
        businessUnitBinder.forField(pmMultiselect).bind(BusinessUnit_.PRODUCT_MANAGERS);

        businessUnitBinder.addStatusChangeListener(status -> {
            boolean emptyFields = mandatoryAttributesOfBusinessUnits.stream()
                    .flatMap(prop -> businessUnitBinder.getBinding(prop).stream())
                    .anyMatch(binding -> binding.getField().isEmpty());
            saveButton.setEnabled(!status.hasValidationErrors() && !emptyFields);
        });

        // Dialog for delete confirmation and handler what to do when delete confirmed
        deleteConfirmationDialog =
                new DeleteConfirmationDialog("Delete Business Unit", "Business Unit's Name");
        deleteConfirmationDialog.setChangeHandler(this::deleteConfirmationChangeHandler);

        // Listeners for form buttons
        addKeyPressListener(Key.ENTER, e -> saveButtonListener());
        saveButton.addClickListener(e -> saveButtonListener());
        deleteButton.addClickListener(e -> deleteButtonListener());
        cancelButton.addClickListener(e -> changeHandler.onChange());

        reloadComboBoxes();

        this.close();
        this.setCloseOnOutsideClick(false);
        this.setCloseOnEsc(false);
    }

    protected void openEditor(BusinessUnit businessUnit) {
        if(businessUnit == null) {
            this.close();
        }
        else {
            boolean persisted = businessUnit.getId() != null;
            deleteButton.setVisible(persisted);

            if(persisted) {
                businessUnit = businessUnitService.getBusinessUnitByName(businessUnit.getName());
                heading.setText("Edit Business Unit");
            }
            else {
                heading.setText("New Business Unit");
            }
            reloadComboBoxes();
            businessUnitBinder.setBean(businessUnit);

            saveButton.setEnabled(false);
            this.open();
        }
    }

    private void reloadComboBoxes() {
        pmMultiselect.setItems(employeeService.getAllProductManagers());
    }

    private void deleteButtonListener() {
        BusinessUnit businessUnit = businessUnitBinder.getBean();
        int numberReferences = partNumberObjectService.countPartNumberObjects(businessUnit);
        deleteConfirmationDialog.getOptionalArea().removeAll();
        if(numberReferences > 0) {
            deleteConfirmationDialog.getOptionalArea().add(new Label(String.format("The Business Unit is referenced " +
                    "in %d PNs. These references would get removed (PNs would NOT get deleted).", numberReferences)));
        }
        deleteConfirmationDialog.open(businessUnit.getName());
    }

    private void saveButtonListener() {
        BusinessUnit businessUnit = businessUnitBinder.getBean();
        if(businessUnit.getId() != null) {
            businessUnitService.putBusinessUnit(businessUnit);
            Notification.show(String.format("Business Unit %s updated", businessUnit.getName()));
        }
        else {
            businessUnitService.postBusinessUnit(businessUnit);
            Notification.show(String.format("Business Unit %s created", businessUnit.getName()));
        }
        changeHandler.onChange();
    }

    private void deleteConfirmationChangeHandler() {
        BusinessUnit businessUnit = businessUnitBinder.getBean();
        relationshipService.removeRelationships(businessUnit);
        try {
            businessUnitService.deleteBusinessUnit(businessUnit);
        } catch (DependencyException e) {
            e.printStackTrace();
        }
        Notification.show(String.format("Business Unit %s deleted", businessUnit.getName()));
        changeHandler.onChange();
    }
}
