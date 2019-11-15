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

import com.felixseifert.coma.backend.model.Coo;
import com.felixseifert.coma.backend.service.CooPlantService;
import com.felixseifert.coma.ui.common.dialogs.AbstractSimpleCreateDialog;
import com.felixseifert.coma.ui.common.ViewConstants;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;

public class CooCreateDialog extends AbstractSimpleCreateDialog<Coo> {

    private static final long serialVersionUID = 1L;

    private TextField nameField;
    private TextField abbrField;

    CooCreateDialog(CooPlantService cooPlantService) {
        super(cooPlantService::postCoo);

        nameField = new TextField("Name");
        nameField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        nameField.setWidthFull();
        nameField.setMaxLength(60);
        getContent().add(nameField);
        abbrField = new TextField("Abbreviation");
        abbrField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        abbrField.setWidthFull();
        abbrField.setMaxLength(3);
        getContent().add(abbrField);

        getBinder().forField(nameField)
                .asRequired("Please type in a name.")
                .withValidator(n -> !cooPlantService.existsByCooName(n), "This name already exists.")
                .bind("name");
        getBinder().forField(abbrField)
                .asRequired("Please type in an abbreviation.")
                .withValidator(a -> !cooPlantService.existsByCooAbbreviation(a), "This abbreviation already exists.")
                .bind("abbreviation");

        getCompulsoryFields().add("name");
        getCompulsoryFields().add("abbreviation");

        getCreateButton().addClickListener(e -> Notification.show(
                String.format("CoO \"%s (%s)\" created", abbrField.getValue(), nameField.getValue())));
    }

    @Override
    public void open(Coo coo) {
        getBinder().setBean(coo);
        nameField.clear();
        abbrField.clear();
        getCreateButton().setEnabled(false);
        this.open();
        getHeading().setText("Add Country");
        nameField.focus();
    }
}
