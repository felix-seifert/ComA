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
import com.felixseifert.coma.backend.model.PartNumberObject;
import com.felixseifert.coma.backend.model.SelectableValue;
import com.felixseifert.coma.backend.model.enums.CustomerNotification;
import com.felixseifert.coma.backend.model.enums.Progress;
import com.felixseifert.coma.backend.model.enums.ValueGroup;
import com.felixseifert.coma.backend.service.PartNumberObjectService;
import com.felixseifert.coma.backend.service.SelectableValueService;
import com.felixseifert.coma.ui.common.ChangeHandler;
import com.felixseifert.coma.ui.common.ViewConstants;
import com.felixseifert.coma.ui.common.dialogs.AbstractDialog;
import com.felixseifert.coma.ui.common.dialogs.SaveConfirmationDialog;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public class ContactCustomerDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    @Setter
    private ChangeHandler closeAction;

    private PartNumberObjectService partNumberObjectService;

    private SelectableValueService selectableValueService;

    private RadioButtonGroup<CustomerNotification> customerReactionRadioButtonGroup = new RadioButtonGroup<>();

    private ComboBox<String> rejectionReasonComboBox = new ComboBox<>("Rejection Reason");

    private TextField followUpField = new TextField("Short follow-up comment");

    private SaveConfirmationDialog saveReasonDialog;

    public ContactCustomerDialog(ChangeHandler closeAction) {

        super("Contact Customer");

        this.closeAction = closeAction;

        partNumberObjectService = BeanGetter.getBean(PartNumberObjectService.class);
        selectableValueService = BeanGetter.getBean(SelectableValueService.class);

        saveReasonDialog = new SaveConfirmationDialog(
                "Save " + ValueGroup.REASON_FOR_REACTION.getFieldDescription(),
                "Reason to save", "Do you want to create this reason for rejection?");
        saveReasonDialog.setChangeHandler(() -> selectableValueService.postSelectableValue(
                rejectionReasonComboBox.getValue(), ValueGroup.REASON_FOR_REACTION));

        this.setWidth(ViewConstants.SIMPLE_DIALOG_WIDTH_STRING);

        getContent().add(new Label("How did the customer react concerning the released PN?"));

        customerReactionRadioButtonGroup.setLabel("Customer Reaction");
        customerReactionRadioButtonGroup.setItems(CustomerNotification.ACCEPTED, CustomerNotification.REJECTED);
        customerReactionRadioButtonGroup.setRenderer(new TextRenderer<>(CustomerNotification::getName));
        customerReactionRadioButtonGroup.addValueChangeListener(e -> {
            if(CustomerNotification.ACCEPTED.equals(e.getValue())) {
                getOkButton().setEnabled(okButtonEnabled());
                rejectionReasonComboBox.setVisible(false);
            }
            if(CustomerNotification.REJECTED.equals(e.getValue())) {
                getOkButton().setEnabled(okButtonEnabled());
                rejectionReasonComboBox.setVisible(true);
            }
        });
        getContent().add(customerReactionRadioButtonGroup);

        rejectionReasonComboBox.setWidthFull();
        rejectionReasonComboBox.addValueChangeListener(e -> getOkButton().setEnabled(okButtonEnabled()));
        rejectionReasonComboBox.addCustomValueSetListener(e -> {
            rejectionReasonComboBox.setValue(e.getDetail());
            if(!selectableValueService.exists(e.getDetail(), ValueGroup.REASON_FOR_REACTION)) {
                saveReasonDialog.open(e.getDetail());     // Todo: Add restriction of max length (see model)
            }
        });
        getContent().add(rejectionReasonComboBox);

        followUpField.setWidthFull();
        followUpField.setMaxLength(255);
        getContent().add(followUpField);

        getOkButton().setText("Confirm");
        getOkButton().setIcon(VaadinIcon.CHECK.create());
        getOkButton().getElement().getThemeList().add("primary");
    }

    private boolean okButtonEnabled() {
        return CustomerNotification.ACCEPTED.equals(customerReactionRadioButtonGroup.getValue()) ||
                (CustomerNotification.REJECTED.equals(customerReactionRadioButtonGroup.getValue()) &&
                        StringUtils.isNotBlank(rejectionReasonComboBox.getValue()));
    }

    public void open(int partNumberObjectId) {

        PartNumberObject partNumberObject = partNumberObjectService.getPartNumberObjectById(partNumberObjectId);

        customerReactionRadioButtonGroup.clear();
        rejectionReasonComboBox.clear();
        followUpField.clear();

        if(Progress.RELEASED.equals(partNumberObject.getTaskRelease().getStatus())) {
            setChangeHandler(() -> changeHandlerReleased(partNumberObject));
            rejectionReasonComboBox.setItems(selectableValueService.getSelectableValuesByValueGroup(
                    ValueGroup.REASON_FOR_REACTION).stream().map(SelectableValue::getLabel));
            rejectionReasonComboBox.setVisible(false);
            customerReactionRadioButtonGroup.setVisible(true);
            followUpField.setVisible(false);
            getOkButton().setEnabled(false);
            this.open();
            return;
        }

        setChangeHandler(() -> changeHandlerDenied(partNumberObject));
        followUpField.setVisible(true);
        customerReactionRadioButtonGroup.setVisible(false);
        rejectionReasonComboBox.setVisible(false);
        getOkButton().setEnabled(true);
        this.open();
    }

    private void changeHandlerReleased(PartNumberObject partNumberObject) {
        partNumberObject.getTaskRelease().setStepFinished(true);
        partNumberObject.getTaskRelease().setCustomerNotification(customerReactionRadioButtonGroup.getValue());

        if(customerReactionRadioButtonGroup.getValue().equals(CustomerNotification.REJECTED)) {
            partNumberObject.getTaskRelease().setRejectionReasonOrFollowUp(rejectionReasonComboBox.getValue());
        }

        partNumberObjectService.putPartNumberObject(partNumberObject);

        closeAction.onChange();
    }

    private void changeHandlerDenied(PartNumberObject partNumberObject) {
        partNumberObject.getTaskRelease().setStepFinished(true);
        partNumberObject.getTaskRelease().setCustomerNotification(CustomerNotification.NOTIFIED);
        partNumberObject.getTaskRelease().setRejectionReasonOrFollowUp(followUpField.getValue());
        partNumberObjectService.putPartNumberObject(partNumberObject);
        closeAction.onChange();
    }
}
