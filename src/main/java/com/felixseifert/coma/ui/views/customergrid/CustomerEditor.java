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

package com.felixseifert.coma.ui.views.customergrid;

import com.felixseifert.coma.backend.exceptions.DependencyException;
import com.felixseifert.coma.backend.model.enums.Currency;
import com.felixseifert.coma.backend.model.Customer;
import com.felixseifert.coma.backend.service.CustomerService;
import com.felixseifert.coma.backend.service.PartNumberObjectService;
import com.felixseifert.coma.ui.common.ChangeHandler;
import com.felixseifert.coma.ui.common.dialogs.DeleteConfirmationDialog;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Setter;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringComponent
@UIScope
public class CustomerEditor extends Dialog implements KeyNotifier {

    private static final long serialVersionUID = 1L;

    private CustomerService customerService;

    private H3 heading = new H3();

    private TextField codeField = new TextField("Customer Code");
    private TextField nameField = new TextField("Customer Name");
    private TextField deliveryLocationField = new TextField("Delivery Location");
    private ComboBox<String> currencyComboBox = new ComboBox<>("Currency");

    private Button saveButton = new com.vaadin.flow.component.button.Button("Save", VaadinIcon.CHECK.create());
    private Button cancelButton = new com.vaadin.flow.component.button.Button("Cancel");
    private Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());

    private DeleteConfirmationDialog deleteConfirmationDialog;

    private Binder<Customer> customerBinder = new Binder<>(Customer.class);

    @Setter
    private ChangeHandler changeHandler;

    public CustomerEditor(CustomerService customerService, PartNumberObjectService partNumberObjectService) {

        this.customerService = customerService;

        codeField.setWidthFull();
        codeField.setMaxLength(10);
        codeField.setValueChangeMode(ValueChangeMode.EAGER);
        nameField.setWidthFull();
        nameField.setMaxLength(60);
        deliveryLocationField.setWidthFull();
        deliveryLocationField.setMaxLength(60);
        currencyComboBox.setWidthFull();
        currencyComboBox.setItems(Arrays.stream(Currency.values()).map(Enum::toString).collect(Collectors.toList()));
        VerticalLayout form = new VerticalLayout(codeField, nameField, deliveryLocationField, currencyComboBox);

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

        customerBinder.forField(codeField).asRequired("Please fill in customer code.")
                .withValidator(new CustomerCodeValidator(customerService))
                .bind("code");
        customerBinder.forField(nameField).asRequired("Please fill in customer name.").bind("name");
        customerBinder.forField(deliveryLocationField).asRequired("Please fill in delivery location.")
                .bind("deliveryLocation");
        customerBinder.forField(currencyComboBox).bind("currency");

        customerBinder.addStatusChangeListener(status -> {
            boolean emptyFields = Stream.of("code", "name", "deliveryLocation")
                    .flatMap(prop -> customerBinder.getBinding(prop).stream())
                    .anyMatch(binding -> binding.getField().isEmpty());
            saveButton.setEnabled(!status.hasValidationErrors() && !emptyFields);
        });

        // Dialog for delete confirmation and handler what to do when delete confirmed
        deleteConfirmationDialog = new DeleteConfirmationDialog("Delete Customer",
                "Customer Name (Customer Code)");
        deleteConfirmationDialog.setChangeHandler(() -> {
            Customer customer = customerBinder.getBean();

            try {
                if(!deleteConfirmationDialog.getCheckbox().getValue()) {
                    customer.getPartNumberObjects().forEach(pn -> {
                        pn.setCustomer(null);
                        partNumberObjectService.putPartNumberObject(pn);
                    });
                    customer.getPartNumberObjects().clear();
                }
                else {
                    customer.getPartNumberObjects().forEach(partNumberObjectService::deletePartNumberObject);
                    customer = customerService.getCustomerByCode(customer.getCode());
                }
                customerService.deleteCustomer(customer);
            } catch (DependencyException e) {
                e.printStackTrace();
            }
            Notification.show(String.format("Customer with code %s deleted", customer.getCode()));
            changeHandler.onChange();
        });

        // Listeners for form buttons
        addKeyPressListener(Key.ENTER, e -> saveButtonListener());
        saveButton.addClickListener(e -> saveButtonListener());
        deleteButton.addClickListener(e -> deleteButtonListener());
        cancelButton.addClickListener(e -> changeHandler.onChange());

        this.close();
        this.setCloseOnOutsideClick(false);
        this.setCloseOnEsc(false);
    }

    protected void openEditor(Customer customer) {
        if(customer == null) {
            this.close();
        }
        else {
            boolean persisted = customer.getCode() != null && customerService.exists(customer.getCode());
            deleteButton.setVisible(persisted);
            codeField.setReadOnly(persisted);

            if(persisted) {
                customer = customerService.getCustomerByCode(customer.getCode());
                heading.setText("Edit Customer");
                nameField.focus();
            }
            else {
                heading.setText("New Customer");
                codeField.focus();
            }
            customerBinder.setBean(customer);

            saveButton.setEnabled(false);
            this.open();
        }
    }

    private void deleteButtonListener() {
        Customer customer = customerBinder.getBean();
        if(!customer.getPartNumberObjects().isEmpty()) {
            fillOptionalArea(customer);
        }
        deleteConfirmationDialog.open(String.format("%s (%s)", customer.getName(), customer.getCode()));
    }

    private void fillOptionalArea(Customer customer) {
        Checkbox checkbox = deleteConfirmationDialog.getCheckbox();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setPadding(false);
        Label label = new Label(String.format("%d PNs are assigned to this customer.",
                customer.getPartNumberObjects().size()));
        verticalLayout.add(label);
        verticalLayout.add(checkbox);
        checkbox.setLabel("Mark the checkbox to delete the assigned PNs. " +
                "Like this, only the references and not the PNs would get removed.");
        checkbox.setValue(false);
        checkbox.addValueChangeListener(event -> {
            if(event.getValue()) checkbox.setLabel("The assigned PNs would get deleted.");
            else checkbox.setLabel("Mark the checkbox to delete the assigned PNs. " +
                    "Like this, only the references and not the PNs would get removed.");
        });

        deleteConfirmationDialog.getOptionalArea().removeAll();
        deleteConfirmationDialog.getOptionalArea().add(verticalLayout);
    }

    private void saveButtonListener() {
        Customer customer = customerBinder.getBean();
        if(customerService.exists(customer.getCode())) {
            customerService.putCustomer(customer);
            Notification.show(String.format("Customer with code %s updated", customer.getCode()));
        }
        else {
            customerService.postCustomer(customer);
            Notification.show(String.format("Customer with code %s created", customer.getCode()));
        }
        changeHandler.onChange();
    }
}
