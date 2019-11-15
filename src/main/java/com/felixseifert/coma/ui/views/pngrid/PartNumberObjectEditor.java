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
import com.felixseifert.coma.backend.model.enums.CustomerNotification;
import com.felixseifert.coma.backend.model.enums.Progress;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.model.enums.ValueGroup;
import com.felixseifert.coma.backend.service.*;
import com.felixseifert.coma.ui.common.ChangeHandler;
import com.felixseifert.coma.ui.common.dialogs.DeleteConfirmationDialog;
import com.felixseifert.coma.ui.common.StyledText;
import com.felixseifert.coma.ui.common.ViewConstants;
import com.felixseifert.coma.ui.common.dialogs.DenyConfirmationDialog;
import com.felixseifert.coma.backend.model.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
@Getter
public class PartNumberObjectEditor extends Dialog implements KeyNotifier {

    private static final long serialVersionUID = 1L;

    @Getter(AccessLevel.NONE)
    private PartNumberObjectService partNumberObjectService;

    @Getter(AccessLevel.NONE)
    private EmployeeService employeeService;

    @Getter(AccessLevel.NONE)
    private CooPlantService cooPlantService;

    @Getter(AccessLevel.NONE)
    private SelectableValueService selectableValueService;

    @Getter(AccessLevel.NONE)
    private CustomerService customerService;

    @Getter(AccessLevel.NONE)
    private BusinessUnitService businessUnitService;

    @Getter(AccessLevel.NONE)
    private ProductDescriptionService productDescriptionService;

    private H3 heading = new H3();

    private Div responsibleEmployeeDiv = new Div();

    private Button contactCustomerButton = new Button("Contact Customer");

    private TextField pnField = new TextField("PN");
    private TextField idxField = new TextField("Index");
    private TextField completePnField = new TextField("Complete PN");
    private TextField completeSourcePnField = new TextField("Complete Source PN");

    private DatePicker dueDateField = new DatePicker("Due Date");
    private DatePicker startOfProductionField = new DatePicker("Start of Production");
    private DatePicker endOfProductionField = new DatePicker("End of Production");

    private ComboBox<String> productDescriptionComboBox = new ComboBox<>("Product Description");
    private TextField productDescriptionTextHidden = new TextField("HIDDEN");
    private TextField productDescriptionCodeHidden = new TextField("HIDDEN");
    private TextField predecessorField = new TextField("Predecessor");

    private DatePicker createdDateField = new DatePicker("Date Request Created");
    private ComboBox<Employee> createdByComboBox = new ComboBox<>("Request Created by (Employee)");
    private TextField createdByField = new TextField("Request Created by (Team)");

    private MultiselectComboBox<String> requestTypeMultiselect = new MultiselectComboBox<>();
    private MultiselectComboBox<String> profileMultiselect = new MultiselectComboBox<>();

    private ComboBox<Customer> customerComboBox = new ComboBox<>("Customer Name");
    private TextField customerField = new TextField("Customer Code");
    private TextField deliveryField = new TextField("Delivery Location");
    private TextField customerPartNumberField = new TextField("Customer P/N");

    private ComboBox<String> cooComboBox = new ComboBox<>("Country of Origin");
    private TextField cooAbbreviationFieldHidden = new TextField("HIDDEN");
    private TextField cooNameFieldHidden = new TextField("HIDDEN");
    private ComboBox<String> sourceComboBox = new ComboBox<>("Product Source (e.g. plant, warehouse)");
    private TextField sourceCodeField = new TextField("Product Source Code");

    private NumberField cataloguePriceField = new NumberField("List/Catalogue Price");
    private Checkbox transferPriceCheckbox = new Checkbox("Transfer Price Known");
    private Checkbox ppcCheckbox = new Checkbox("PPC Known");

    private ReleaseFlowField<Role> releaseFlowField = new ReleaseFlowField<>();

    private ComboBox<BusinessUnit> businessUnitComboBox = new ComboBox<>("Business Unit");

    private Map<Role, ComboBox<Employee>> employeeFields = new HashMap<>();

    private TextArea commentsField = new TextArea("Comments");

    private Button saveButton = new Button("Save (edit later)", VaadinIcon.TIME_FORWARD.create());
    private Button submitButton = new Button(VaadinIcon.CHECK.create());
    private Button cancelButton = new Button("Cancel");
    private Button denyButton = new Button("Deny", VaadinIcon.CLOSE.create());
    private Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());

    private DenyConfirmationDialog denyConfirmationDialog =
            new DenyConfirmationDialog("Deny PN Request", "Part Number");

    private DeleteConfirmationDialog deleteConfirmationDialog =
            new DeleteConfirmationDialog("Delete PN", "Part Number");

    private SubmitConfirmationDialog submitConfirmationDialog = new SubmitConfirmationDialog();

    private SelectResponsibilityDialog selectResponsibilityDialog = new SelectResponsibilityDialog(this::openEditor);

    private ContactCustomerDialog contactCustomerDialog =
            new ContactCustomerDialog(() -> this.changeHandler.onChange());

    @Getter(AccessLevel.NONE)
    private Binder<PartNumberObject> partNumberObjectBinder = new Binder<>(PartNumberObject.class);

    private final Set<String> finalMandatoryAttributesOfPartNumbers = Set.of(PartNumberObject_.PN,
            PartNumberObject_.CREATED_BY_TEAM_DEPARTMENT);

    private Set<String> mandatoryAttributesOfPartNumbers = new HashSet<>(finalMandatoryAttributesOfPartNumbers);

    @Setter
    private ChangeHandler changeHandler;

    public PartNumberObjectEditor(PartNumberObjectService partNumberObjectService,
                                  EmployeeService employeeService,
                                  SelectableValueService selectableValueService,
                                  CustomerService customerService,
                                  BusinessUnitService businessUnitService,
                                  CooPlantService cooPlantService,
                                  ProductDescriptionService productDescriptionService) {

        this.partNumberObjectService = partNumberObjectService;
        this.employeeService = employeeService;
        this.selectableValueService = selectableValueService;
        this.customerService = customerService;
        this.businessUnitService = businessUnitService;
        this.cooPlantService = cooPlantService;
        this.productDescriptionService = productDescriptionService;

        submitButton.getElement().getThemeList().add("primary");
        saveButton.getElement().getThemeList().add("secondary");
        cancelButton.getElement().getThemeList().add("tertiary");
        denyButton.getElement().getThemeList().add("error");
        deleteButton.getElement().getThemeList().add("error");

        HorizontalLayout actionLeft = new HorizontalLayout(deleteButton, denyButton);
        HorizontalLayout actionsRight = new HorizontalLayout(cancelButton, saveButton, submitButton);
        HorizontalLayout actions = new HorizontalLayout(actionLeft, actionsRight);

        actionLeft.setWidth("40%");
        actionsRight.setWidth("60%");
        actionLeft.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        actionsRight.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout form = createForm();
        form.setPadding(false);
        form.setMargin(false);
        reloadComboBoxes(null);

        this.add(createHeadingLayout(), form, actions);

        // Bind fields to database entity with rules for filling form
        bindFields();

        // Change listener to enable/disable save and submit button when form content changes
        partNumberObjectBinder.addStatusChangeListener(status -> {
            boolean emptyFields = mandatoryAttributesOfPartNumbers.stream()
                    .flatMap(prop -> partNumberObjectBinder.getBinding(prop).stream())
                    .anyMatch(binding -> binding.getField().isEmpty());
            saveButton.setEnabled(!status.hasValidationErrors() && !emptyFields &&
                    saveButton.getText().toLowerCase().contains("save"));
            submitButton.setEnabled(!status.hasValidationErrors() && !emptyFields &&
                    submitButton.getText().toLowerCase().contains("submit"));
        });

        // Dialog for submit confirmation and handler what to do when submit confirmed
        submitConfirmationDialog.setChangeHandler(() -> {
            PartNumberObject partNumberObject = partNumberObjectBinder.getBean();
            partNumberObject.getTaskRelease().setStepFinished(true);
            saveButtonListener(partNumberObject);
        });

        // Dialog for deny confirmation and handler what to do when deny confirmed
        denyConfirmationDialog.setChangeHandler(() -> {
            PartNumberObject partNumberObject = partNumberObjectBinder.getBean();
            partNumberObjectService.denyRequest(partNumberObject);
            Notification.show(String.format("Request for PN %s (created at %s) denied",
                    partNumberObject.getPn(), partNumberObject.getCreatedDate()));
            changeHandler.onChange();
        });

        // Dialog for delete confirmation and handler what to do when delete confirmed
        deleteConfirmationDialog.setChangeHandler(() -> {
            PartNumberObject partNumberObject = partNumberObjectBinder.getBean();
            this.partNumberObjectService.deletePartNumberObject(partNumberObject);
            Notification.show(String.format("PN %s (created at %s) deleted", partNumberObject.getPn(),
                    partNumberObject.getCreatedDate()));
            changeHandler.onChange();
        });

        // Listeners for form buttons
        saveButton.addClickListener(e -> saveButtonListener(partNumberObjectBinder.getBean()));
        submitButton.addClickListener(e -> submitButtonListener(partNumberObjectBinder.getBean()));
        denyButton.addClickListener(e -> denyButtonListener(partNumberObjectBinder.getBean()));
        deleteButton.addClickListener(e ->
                deleteConfirmationDialog.open(partNumberObjectBinder.getBean().getPn() +
                        " created at " + partNumberObjectBinder.getBean().getCreatedDate()));
        cancelButton.addClickListener(e -> changeHandler.onChange());

        this.close();
        this.setCloseOnOutsideClick(false);
        this.setCloseOnEsc(false);
    }

    private HorizontalLayout createHeadingLayout() {

        responsibleEmployeeDiv.getStyle().set("text-align", "right");

        HorizontalLayout left = new HorizontalLayout(heading);
        HorizontalLayout right = new HorizontalLayout(responsibleEmployeeDiv);
        HorizontalLayout complete = new HorizontalLayout(left, right);

        left.setWidth("40%");
        right.setWidth("60%");
        left.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        right.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        complete.setAlignItems(FlexComponent.Alignment.CENTER);
        complete.setPadding(true);

        return complete;

    }

    private VerticalLayout createForm() {

        contactCustomerButton.getElement().getThemeList().add("error");
        contactCustomerButton.setWidthFull();
        contactCustomerButton.addClickListener(e ->
                contactCustomerDialog.open(partNumberObjectBinder.getBean().getId()));

        VerticalLayout requestBlock = createFieldSetWithBorder("Request Information");

        createdDateField.setReadOnly(true);
        createdDateField.addValueChangeListener(event -> {
            if(event.getValue() != null) dueDateField.setMin(createdDateField.getValue());
        });
        createdByComboBox.setItemLabelGenerator(e -> String.format("%s (%s)", e.getName(), e.getTeam()));
        createdByComboBox.addValueChangeListener(event -> {
            if(event.getValue() != null &&
                    StringUtils.isBlank(partNumberObjectBinder.getBean().getCreatedByTeamDepartment())) {
                createdByField.setValue(event.getValue().getTeam());
            }
        });
        createdByField.setReadOnly(true);
        createdByField.setPlaceholder("Filled based on selected Employee");
        requestBlock.add(createRowToAdd(createdDateField, createdByComboBox, createdByField));

        endOfProductionField.setClearButtonVisible(true);
        startOfProductionField.addValueChangeListener(event -> {
            if(event.getValue() != null) endOfProductionField.setMin(event.getValue().plusDays(1));
        });
        requestBlock.add(createRowToAdd(dueDateField, startOfProductionField, endOfProductionField));

        requestTypeMultiselect.setLabel("Request Type");
        requestBlock.add(requestTypeMultiselect);

        profileMultiselect.setLabel("Channel Profile");
        requestBlock.add(profileMultiselect);

        VerticalLayout productBlock = createFieldSetWithBorder("Product Details");

        pnField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        pnField.setMaxLength(10);
        pnField.setValueChangeMode(ValueChangeMode.EAGER);
        completePnField.setReadOnly(true);
        completePnField.setMinWidth("calc(" + (2 * ViewConstants.MIN_WIDTH_FOR_FIELD) + "px + var(--lumo-space-m))");
        productBlock.add(new HorizontalLayout(pnField, completePnField));

        idxField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        idxField.setMaxLength(3);
        idxField.setValueChangeMode(ValueChangeMode.EAGER);
        // Set initial value in openEditor(), generate value on change
        idxField.addValueChangeListener(event -> completePnField.setValue(pnField.getValue() + idxField.getValue()));
        completeSourcePnField.setMinWidth("calc(" + (2 * ViewConstants.MIN_WIDTH_FOR_FIELD) + "px + var(--lumo-space-m))");
        completeSourcePnField.setValueChangeMode(ValueChangeMode.EAGER);
        completeSourcePnField.setMaxLength(13);
        productBlock.add(new HorizontalLayout(idxField, completeSourcePnField));

        productDescriptionComboBox.setClearButtonVisible(true);
        productDescriptionComboBox.setMinWidth(
                "calc(" + (2 * ViewConstants.MIN_WIDTH_FOR_FIELD) + "px + var(--lumo-space-m))");
        productDescriptionComboBox.addValueChangeListener(e -> {
            if(StringUtils.isNotBlank(e.getValue())) writeToHiddenProductDescriptionFields(e.getValue());
        });
        predecessorField.setMaxLength(13);
        predecessorField.setValueChangeMode(ValueChangeMode.EAGER);
        predecessorField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        productBlock.add(new HorizontalLayout(productDescriptionComboBox, predecessorField));

        VerticalLayout customerBlock = createFieldSetWithBorder("Customer");

        customerComboBox.setItemLabelGenerator(c -> c.getName() +
                (StringUtils.isNotBlank(c.getCurrency()) ? String.format(" (%s)", c.getCurrency()) : ""));
        customerComboBox.setClearButtonVisible(true);
        customerComboBox.setMinWidth("calc(" + (2 * ViewConstants.MIN_WIDTH_FOR_FIELD) + "px + var(--lumo-space-m))");
        customerComboBox.addValueChangeListener(event -> {
            if(event.getValue() != null) {
                customerField.setValue(event.getValue().getCode());
                deliveryField.setValue(event.getValue().getDeliveryLocation());
            }
            else {
                customerField.setValue("Filled based on selected Customer");
                deliveryField.setValue("Filled based on selected Customer");
            }
        });
        customerField.setReadOnly(true);
        customerField.setValue("Filled based on selected Customer");
        customerField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        customerPartNumberField.setMinWidth(
                "calc(" + (2 * ViewConstants.MIN_WIDTH_FOR_FIELD) + "px + var(--lumo-space-m))");
        customerPartNumberField.setMaxLength(20);
        deliveryField.setReadOnly(true);
        deliveryField.setValue("Filled based on selected Customer");
        deliveryField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        customerBlock.add(new HorizontalLayout(customerComboBox, customerField));
        customerBlock.add(new HorizontalLayout(customerPartNumberField, deliveryField));

        VerticalLayout buBlock = createFieldSetWithBorder("BU");

        cooComboBox.setClearButtonVisible(true);
        cooComboBox.addValueChangeListener(e -> {
            if(StringUtils.isNotBlank(e.getValue())) {
                String cooName = splitFieldValueParentheses(e.getValue())[1];
                sourceComboBox.setItems(cooPlantService.getPlantsByCoo(cooName).stream().map(Plant::getName));
                writeToHiddenCooFields(e.getValue());
            }
            else if(StringUtils.isNotBlank(e.getOldValue())) {
                sourceComboBox.clear();
                sourceComboBox.setItems(List.of());
                cooNameFieldHidden.clear();
                cooAbbreviationFieldHidden.clear();
            }
        });
        sourceComboBox.setClearButtonVisible(true);
        sourceComboBox.addValueChangeListener(e -> {
            if(StringUtils.isNotBlank(e.getValue()) && StringUtils.isBlank(sourceCodeField.getValue())) {
                sourceCodeField.setValue(cooPlantService.getPlantByNameAndCooName(e.getValue(),
                        cooNameFieldHidden.getValue()).getCode());
            }
            else if(StringUtils.isNotBlank(e.getOldValue())) {
                sourceCodeField.clear();
            }
        });
        sourceCodeField.setReadOnly(true);
        sourceCodeField.setPlaceholder("Filled automatically");
        buBlock.add(createRowToAdd(cooComboBox, sourceComboBox, sourceCodeField));

        VerticalLayout pricesBlock = createFieldSetWithBorder("Prices");

        cataloguePriceField.setPrefixComponent(new Span("¥"));
        cataloguePriceField.setStep(0.01);

        pricesBlock.add(createRowToAdd(cataloguePriceField, transferPriceCheckbox, ppcCheckbox));

        VerticalLayout releaseBlock = createFieldSetWithBorder("Release Flow");

        releaseFlowField.setWidth("calc(" + (3 * ViewConstants.MIN_WIDTH_FOR_FIELD) + "px + 2*var(--lumo-space-m))");
        releaseFlowField.setDescription(new StyledText("The release flow describes <strong>who should be " +
                "contacted next</strong>. When the PN Requests gets released or denied, the <strong>"
                + Role.REQUESTER.getName() + " would get notified automatically</strong>. Then, the customer " +
                "should get contacted. <br>&nbsp;<br> Each role represents the responsible person for this " +
                "PN Request. Select a role and add it via the button. Roles could be deleted by clicking on them. " +
                "Two roles could be swapped with the exchange icon between them."));
        releaseFlowField.setSelectLabel("Select Role");
        releaseFlowField.setSelectWidth(
                "calc(" + (2 * ViewConstants.MIN_WIDTH_FOR_FIELD) + "px + var(--lumo-space-m))");
        releaseFlowField.setButtonText("Add Role to Release Flow");
        releaseFlowField.setNumbersLeftOfText(true);
        releaseFlowField.setButtonWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        releaseFlowField.setItems(Arrays.stream(Role.values())
                .filter(role -> role.getPnVariable() != null).filter(role -> !role.equals(Role.REQUESTER)));
        releaseFlowField.setMaxNumberItems(5);
        // Todo: Manage horizontal scrolling for many entries
        releaseFlowField.setItemLabelGenerator(Role::getName);

        releaseBlock.add(releaseFlowField);

        VerticalLayout responsibilitiesBlock = createFieldSetWithBorder("Responsibilities");

        responsibilitiesBlock.add(createRowToAdd(businessUnitComboBox,
                createEmployeeField(Role.PRODUCT_MANAGER),
                createEmployeeField(Role.PRODUCT_SPECIALIST)));

        businessUnitComboBox.setItemLabelGenerator(BusinessUnit::getName);
        businessUnitComboBox.setClearButtonVisible(true);
        businessUnitComboBox.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        businessUnitComboBox.addValueChangeListener(event -> {
            if(event.getValue() != null && event.getValue().getProductManagers() != null) {
                employeeFields.get(Role.PRODUCT_MANAGER).setItems(event.getValue().getProductManagers());
            }
            else {
                employeeFields.get(Role.PRODUCT_MANAGER).setItems(employeeService.getAllProductManagers());
            }
        });
        employeeFields.get(Role.PRODUCT_MANAGER).setPlaceholder("Select based on Business Unit.");

        VerticalLayout commentsBlock = createFieldSetWithBorder("");
        commentsField.setPlaceholder("Write your comments here...");
        commentsField.setWidth("calc(" + (3 * ViewConstants.MIN_WIDTH_FOR_FIELD) + "px + 2*var(--lumo-space-m))");
        commentsField.setHeight("180px");
        commentsField.addValueChangeListener(event -> {
            if(!event.getValue().equals(event.getOldValue())) {
                partNumberObjectBinder.validate();
            }
        });
        commentsBlock.add(commentsField);

        return new VerticalLayout(contactCustomerButton, requestBlock, productBlock, customerBlock, buBlock, pricesBlock,
                releaseBlock, responsibilitiesBlock, commentsBlock);
    }

    private VerticalLayout createFieldSetWithBorder(String label) {
        Div divForLabel = new Div(new Label(label));
        divForLabel.getElement().getStyle().set("margin-top", "-13px");
        divForLabel.getElement().getStyle().set("background-color", "white");
        divForLabel.getElement().getStyle().set("color", "var(--lumo-secondary-text-color)");
        divForLabel.getElement().getStyle().set("font-size", "var(--lumo-font-size-s)");
        divForLabel.getElement().getStyle().set("padding-left", "5px");
        divForLabel.getElement().getStyle().set("padding-right", "5px");
        VerticalLayout fieldSet = new VerticalLayout(divForLabel);
        fieldSet.getElement().getStyle().set("border", "1px dashed var(--lumo-contrast-30pct)");
        fieldSet.getElement().getStyle().set("border-radius", "var(--lumo-border-radius)");
        fieldSet.setWidth(null);
        return fieldSet;
    }

    private HorizontalLayout createRowToAdd(Component... components) {
        HorizontalLayout row = new HorizontalLayout();
        for(Component c : components) {
            if(c instanceof HasSize) {
                ((HasSize) c).setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
            }
            row.add(c);
        }
        row.setAlignItems(FlexComponent.Alignment.BASELINE);
        return row;
    }

    private ComboBox<Employee> createEmployeeField(Role role) {
        ComboBox<Employee> comboBox = new ComboBox<>(role.getName());

        comboBox.setClearButtonVisible(true);
        comboBox.setItemLabelGenerator(Employee::getName);
        comboBox.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);

        employeeFields.put(role, comboBox);

        return comboBox;
    }

    private String getCompletePn(PartNumberObject partNumberObject) {
        if(StringUtils.isNotBlank(partNumberObject.getIdx())) {
            return partNumberObject.getPn() + partNumberObject.getIdx();
        }
        return "Automatically generated";
    }

    private void bindFields() {
        partNumberObjectBinder.forField(pnField)
                .asRequired("Please fill in correct PN (PN).")
                .withValidator(pn -> pn.length() == 10, "A PN should have 10 digits.")
                .bind(PartNumberObject_.PN);
        partNumberObjectBinder.forField(idxField)
                .withValidator(idx -> idx.length() == 3 || idx.length() == 0, "An index should have 3 digits.")
                .withValidator(idx -> idx.length() == 0 || completePnValidity(), "The resulting complete PN already exists.")
                .bind(PartNumberObject_.IDX);
        partNumberObjectBinder.forField(completeSourcePnField)
                .withValidator(p -> p.length() == 0 || p.length() == 13, "PNs have 13 digits.")
                .bind(PartNumberObject_.COMPLETE_SOURCE_PN);
        partNumberObjectBinder.forField(productDescriptionTextHidden).bind(PartNumberObject_.PRODUCT_DESCRIPTION);
        partNumberObjectBinder.forField(productDescriptionCodeHidden).bind(PartNumberObject_.PRODUCT_CODE);
        partNumberObjectBinder.forField(dueDateField).bind(PartNumberObject_.DUE_DATE);
        partNumberObjectBinder.forField(startOfProductionField).bind(PartNumberObject_.START_OF_PRODUCTION);
        partNumberObjectBinder.forField(endOfProductionField).bind(PartNumberObject_.END_OF_PRODUCTION);
        partNumberObjectBinder.forField(createdDateField).bind(PartNumberObject_.CREATED_DATE);
        partNumberObjectBinder.forField(createdByComboBox)
                .asRequired("Please select which Employee created this request.")
                .bind(PartNumberObject_.CREATED_BY_EMPLOYEE);
        partNumberObjectBinder.forField(createdByField).bind(PartNumberObject_.CREATED_BY_TEAM_DEPARTMENT);
        partNumberObjectBinder.forField(requestTypeMultiselect).bind(PartNumberObject_.REQUEST_TYPE);
        partNumberObjectBinder.forField(profileMultiselect).bind(PartNumberObject_.CHANNEL_PROFILES);
        partNumberObjectBinder.forField(customerComboBox).bind(PartNumberObject_.CUSTOMER);
        partNumberObjectBinder.forField(customerPartNumberField).bind(PartNumberObject_.CUSTOMER_PART_NUMBER);
        partNumberObjectBinder.forField(predecessorField)
                .withValidator(p -> p.length() == 0 || p.length() == 13,
                        "Predecessor PNs have 13 digits.")
                .bind(PartNumberObject_.PREDECESSOR);
        partNumberObjectBinder.forField(cooAbbreviationFieldHidden).bind(PartNumberObject_.COO_ABBREVIATION);
        partNumberObjectBinder.forField(cooNameFieldHidden).bind(PartNumberObject_.COO_NAME);
        partNumberObjectBinder.forField(sourceComboBox).bind(PartNumberObject_.PLANT);
        partNumberObjectBinder.forField(cataloguePriceField).bind(PartNumberObject_.CATALOGUE_PRICE);
        partNumberObjectBinder.forField(transferPriceCheckbox).bind(PartNumberObject_.TRANSFER_PRICE);
        partNumberObjectBinder.forField(ppcCheckbox).bind(PartNumberObject_.PPC);
        partNumberObjectBinder.forField(commentsField)
                .bind(PartNumberObject::getComments, PartNumberObject::setComments);

        partNumberObjectBinder.forField(businessUnitComboBox).bind(PartNumberObject_.BUSINESS_UNIT);
        employeeFields.forEach((role, field) -> partNumberObjectBinder.forField(field).bind(role.getPnVariable()));
        partNumberObjectBinder.forField(releaseFlowField).bind(pn -> pn.getTaskRelease().getRemainingSteps(),
                (pn, list) -> pn.getTaskRelease().setRemainingSteps(list));
    }

    private boolean completePnValidity() {
        if(!pnField.isInvalid()) {
            return !partNumberObjectService.existsPartNumberObjectByCompletePn(pnField.getValue() + idxField.getValue());
        }
        return true;
    }

    private void writeToHiddenCooFields(String value) {
        String[] split = splitFieldValueParentheses(value);
        cooAbbreviationFieldHidden.setValue(split[0]);
        cooNameFieldHidden.setValue(split[1]);
    }

    private void writeToHiddenProductDescriptionFields(String value) {
        String[] split = splitFieldValueParentheses(value);
        productDescriptionTextHidden.setValue(split[0]);
        productDescriptionCodeHidden.setValue(split[1]);
    }

    private String[] splitFieldValueParentheses(String value) {
        String[] split = value.split("\\s\\(");
        split[1] = split[1].substring(0, split[1].length() - 1);
        return split;
    }

    protected void openEditor(PartNumberObjectGridDTO partNumberObjectGridDTO) {

        if (partNumberObjectGridDTO == null) {
            this.close();
            return;
        }

        boolean persisted = partNumberObjectGridDTO.getId() != null;
        if(persisted) {
            openEditor(partNumberObjectService.getPartNumberObjectById(partNumberObjectGridDTO.getId()));
            return;
        }
        openEditor(new PartNumberObject());
    }

    private void openEditor(PartNumberObject partNumberObject) {

        boolean persisted = partNumberObject.getId() != null;

        if(persisted &&
                !partNumberObject.getTaskRelease().getCompletelyFinished() &&
                ((partNumberObject.getTaskRelease().getCurrentResponsibleEmployee() == null &&
                        partNumberObject.getTaskRelease().getCurrentEmployeesRole() != null) ||
                        partNumberObject.getCreatedByEmployee() == null)) {

            selectResponsibilityDialog.open(partNumberObject);

            return;
        }

        contactCustomerButton.setVisible(!Progress.IN_PROGRESS.equals(partNumberObject.getTaskRelease().getStatus()) &&
                CustomerNotification.NOT_NOTIFIED.equals(partNumberObject.getTaskRelease().getCustomerNotification()));

        denyButton.setVisible(persisted && Progress.IN_PROGRESS.equals(partNumberObject.getTaskRelease().getStatus())
                && !Role.REQUESTER.equals(partNumberObject.getTaskRelease().getCurrentEmployeesRole()));
        deleteButton.setVisible(persisted);
        pnField.setReadOnly(persisted);
        createdByComboBox.setReadOnly(persisted);

        reloadComboBoxes(partNumberObject);
        // Once a responsible Employee gets assigned and saved, its field becomes mandatory.
        mandatoryAttributesOfPartNumbers = new HashSet<>(finalMandatoryAttributesOfPartNumbers);
        employeeFields.entrySet().stream().filter(entry -> !entry.getValue().isEmpty())
                .forEach(entry -> mandatoryAttributesOfPartNumbers.add(entry.getKey().getPnVariable()));
        partNumberObjectBinder.setBean(partNumberObject);

        setHeadingText(partNumberObject);

        if(!persisted) {
            pnField.focus();
            LocalDate today = LocalDate.now();
            createdDateField.setValue(today);

            releaseFlowField.setValue(List.of(Role.PRODUCT_SPECIALIST, Role.PRODUCT_MANAGER,
                    Role.PRODUCT_SPECIALIST));
        }

        completePnField.setValue(getCompletePn(partNumberObject));

        saveButton.setEnabled(false);

        this.open();

        if(partNumberObject.getTaskRelease().getCompletelyFinished()) {
            submitButton.setEnabled(false);
            submitButton.setText("Release Completed");
            submitButton.getElement().getThemeList().add("success");

            saveButton.setText("No Changes Possible");

            return;
        }

        boolean emptyFields = mandatoryAttributesOfPartNumbers.stream()
                .flatMap(prop -> partNumberObjectBinder.getBinding(prop).stream())
                .anyMatch(binding -> binding.getField().isEmpty());

        submitButton.setEnabled(!emptyFields);
        submitButton.setText("Submit (next team)");
        submitButton.getElement().getThemeList().remove("success");

        saveButton.setText("Save (edit later)");
    }

    private void setHeadingText(PartNumberObject partNumberObject) {
        boolean persisted = partNumberObject.getId() != null;

        heading.setText(persisted ? "Edit Request" : "New Request");

        responsibleEmployeeDiv.removeAll();

        TaskRelease taskRelease = partNumberObject.getTaskRelease();

        if(persisted && taskRelease.getCurrentResponsibleEmployee() != null && !taskRelease.getCompletelyFinished()) {
            responsibleEmployeeDiv.add(new StyledText("Current Responsibility: <strong>" +
                    taskRelease.getCurrentResponsibleEmployee().getName() + "</strong> (" +
                    taskRelease.getCurrentEmployeesRole().getName() + ")"));
            return;
        }
        if(persisted && taskRelease.getCurrentResponsibleEmployee() == null && !taskRelease.getCompletelyFinished()) {
            responsibleEmployeeDiv.add(new StyledText("<strong>Employee got deleted. Select new " +
                    taskRelease.getCurrentEmployeesRole().getName() + ".</strong>"));
            return;
        }
        if(persisted && taskRelease.getCompletelyFinished() && taskRelease.getStatus().equals(Progress.RELEASED)) {
            StyledText styledText = new StyledText("<strong>RELEASED</strong>");
            styledText.getElement().getStyle().set("color", "var(--lumo-success-color)");
            responsibleEmployeeDiv.add(styledText);
            heading.setText("Read Released Request");
        }
        if(persisted && taskRelease.getCompletelyFinished() && taskRelease.getStatus().equals(Progress.RELEASED) &&
                !taskRelease.getCustomerNotification().equals(CustomerNotification.NOT_NOTIFIED)) {
            StyledText styledText = new StyledText("<br>Customer reaction: <strong>" +
                    taskRelease.getCustomerNotification().getName() + "</strong>" +
                    (taskRelease.getCustomerNotification().equals(CustomerNotification.REJECTED) ?
                            "<br>Reason: " + taskRelease.getRejectionReasonOrFollowUp() : ""));
            responsibleEmployeeDiv.add(styledText);
            return;
        }
        if(persisted && taskRelease.getCompletelyFinished() && taskRelease.getStatus().equals(Progress.DENIED)) {
            StyledText styledText = new StyledText("<strong>Request DENIED</strong>");
            styledText.getElement().getStyle().set("color", "var(--lumo-error-color)");
            responsibleEmployeeDiv.add(styledText);
            heading.setText("Read Denied Request");
        }
        if(persisted && taskRelease.getCompletelyFinished() && taskRelease.getStatus().equals(Progress.DENIED) &&
                !taskRelease.getCustomerNotification().equals(CustomerNotification.NOT_NOTIFIED)) {
            StyledText styledText = new StyledText("<br>Customer <strong>" +
                    taskRelease.getCustomerNotification().getName() + "</strong>" +
                    (StringUtils.isNotBlank(taskRelease.getRejectionReasonOrFollowUp()) ?
                            "<br>Follow-up comment: " + taskRelease.getRejectionReasonOrFollowUp() : ""));
            responsibleEmployeeDiv.add(styledText);
        }
    }

    private void reloadComboBoxes(PartNumberObject partNumberObject) {
        customerComboBox.setItems(customerService.getAllCustomers());
        createdByComboBox.setItems(employeeService.getAllEmployees());
        requestTypeMultiselect.setItems(selectableValueService.getSelectableValuesByValueGroup(ValueGroup.REQUEST_TYPE)
                .stream().map(SelectableValue::getLabel));
        profileMultiselect.setItems(selectableValueService.getSelectableValuesByValueGroup(ValueGroup.CHANNEL_PROFILE)
                .stream().map(SelectableValue::getLabel));
        cooComboBox.setItems(cooPlantService.getAllCoos()
                .stream().map(c -> String.format("%s (%s)", c.getAbbreviation(), c.getName())));
        cooComboBox.setValue(readCooComboBoxValue(partNumberObject));
        sourceComboBox.setItems(partNumberObject != null && StringUtils.isNotBlank(partNumberObject.getCooName()) ?
                cooPlantService.getPlantsByCoo(partNumberObject.getCooName()).stream().map(Plant::getName)
                        .collect(Collectors.toList()) : List.of());
        sourceCodeField.setValue(partNumberObject != null && partNumberObject.getPlantCode() != null
                ? partNumberObject.getPlantCode() : "");
        productDescriptionComboBox.setItems(productDescriptionService.getAllProductDescriptions()
                .stream().map(pd -> String.format("%s (%s)", pd.getDescription(), pd.getCode())));
        productDescriptionComboBox.setValue(readProductDescriptionComboBoxValue(partNumberObject));
        businessUnitComboBox.setItems(businessUnitService.getAllBusinessUnits());

        employeeFields.forEach((role, field) -> field.setItems(employeeService.getAllEmployeesByRole(role)));
    }

    private String readCooComboBoxValue(PartNumberObject partNumberObject) {
        if(partNumberObject != null && StringUtils.isNotBlank(partNumberObject.getCooAbbreviation()) &&
                StringUtils.isNotBlank(partNumberObject.getCooName())) {
            return String.format("%s (%s)", partNumberObject.getCooAbbreviation(), partNumberObject.getCooName());
        }
        return null;
    }

    private String readProductDescriptionComboBoxValue(PartNumberObject partNumberObject) {
        if(partNumberObject != null && StringUtils.isNotBlank(partNumberObject.getProductCode()) &&
                StringUtils.isNotBlank(partNumberObject.getProductDescription())) {
            return String.format("%s (%s)", partNumberObject.getProductDescription(),
                    partNumberObject.getProductCode());
        }
        return null;
    }

    private void saveButtonListener(PartNumberObject partNumberObject) {
        partNumberObject.setPlantCode(sourceCodeField.getValue());

        if(partNumberObject.getId() != null) {
            partNumberObjectService.putPartNumberObject(partNumberObject);
            Notification.show(String.format("PN %s (created at %s) updated",
                    partNumberObject.getPn(), partNumberObject.getCreatedDate()));
        }
        else {
            partNumberObjectService.postPartNumberObject(partNumberObject);
            Notification.show(String.format("PN %s created", partNumberObject.getPn()));
        }
        changeHandler.onChange();
    }

    private void submitButtonListener(PartNumberObject partNumberObject) {

        if(partNumberObject.getTaskRelease().getCompletedTaskSteps().isEmpty() &&
                partNumberObject.getTaskRelease().getRemainingSteps().isEmpty()) {
            submitConfirmationDialog.open(Submission.FIRST_STEP_EMPTY);
            return;
        }

        if(partNumberObject.getTaskRelease().getRemainingSteps().isEmpty()) {
            submitConfirmationDialog.open(Submission.FINISHED);
            return;
        }

        Role role = partNumberObject.getTaskRelease().getRemainingSteps().get(0);
        Employee employee = partNumberObjectService.getResponsibleEmployeeForRole(role, partNumberObject);

        if(employee != null) {
            submitConfirmationDialog.open(String.format("%s (%s)", employee.getName(), employee.getEmailAddress()));
            return;
        }

        submitConfirmationDialog.open(Submission.RESPONSIBILITY_MISSING);
    }

    private void denyButtonListener(PartNumberObject partNumberObject) {
        if(StringUtils.isBlank(commentsField.getValue())) {
            commentsField.setInvalid(true);
            commentsField.setErrorMessage("Please give a reason for the denial.");
            return;
        }
        denyConfirmationDialog.open(partNumberObject.getPn());
    }
}
