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

package com.felixseifert.coma.ui.views.selectablevalueview;

import com.felixseifert.coma.backend.model.SelectableValue;
import com.felixseifert.coma.backend.service.SelectableValueService;
import com.felixseifert.coma.ui.common.dialogs.AbstractSimpleCreateDialog;
import com.felixseifert.coma.ui.common.ViewConstants;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;

public class SimpleCreateDialog extends AbstractSimpleCreateDialog<SelectableValue> {

    private static final long serialVersionUID = 1L;

    private TextField textField;

    SimpleCreateDialog(SelectableValueService selectableValueService) {
        super(selectableValueService::postSelectableValue);
        textField = new TextField("Value");
        textField.setWidthFull();
        textField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        textField.setMaxLength(100);
        getContent().add(textField);

        getBinder().forField(textField)
                .asRequired("Please type in a value.")
                .withValidator(v -> !selectableValueService.exists(textField.getValue(),
                        getBinder().getBean().getValueGroup()),
                        "This value already exists.")
                .bind("label");

        getCreateButton().addClickListener(e ->
                Notification.show(String.format("Value \"%s\" created", textField.getValue())));
    }

    @Override
    public void open(SelectableValue selectableValue) {
        getBinder().setBean(selectableValue);
        textField.clear();
        getCreateButton().setEnabled(false);
        this.open();
        getHeading().setText("Add " + selectableValue.getValueGroup().getFieldDescription());
        textField.focus();
    }
}
