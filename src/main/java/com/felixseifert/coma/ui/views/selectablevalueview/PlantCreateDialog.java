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

import com.felixseifert.coma.backend.model.Plant;
import com.felixseifert.coma.backend.service.CooPlantService;
import com.felixseifert.coma.ui.common.dialogs.AbstractSimpleCreateDialog;
import com.felixseifert.coma.ui.common.ViewConstants;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;

public class PlantCreateDialog extends AbstractSimpleCreateDialog<Plant> {

    private static final long serialVersionUID = 1L;

    private TextField nameField;

    private TextField codeField;

    PlantCreateDialog(CooPlantService cooPlantService) {
        super(cooPlantService::postPlant);

        nameField = new TextField("Name");
        nameField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        nameField.setWidthFull();
        nameField.setMaxLength(60);
        getContent().add(nameField);

        codeField = new TextField("Product Source Code");
        codeField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        codeField.setWidthFull();
        codeField.setMaxLength(20);
        getContent().add(codeField);

        getBinder().forField(nameField)
                .asRequired("Please type in a name.")
                .withValidator(n -> !cooPlantService.existsByPlantNameAndCoo(n, getBinder().getBean().getCoo()),
                        "This name already exists.")
                .bind("name");
        getBinder().forField(codeField)
                .asRequired("Please type in the product source code.")
                .withValidator(c -> !cooPlantService.existsByPlantCode(c), "This code already exists.")
                .bind("code");

        getCompulsoryFields().add("name");
        getCompulsoryFields().add("code");

        getCreateButton().addClickListener(e ->
                Notification.show(String.format("Product Source \"%s\" created", nameField.getValue())));
    }

    @Override
    public void open(Plant source) {
        getBinder().setBean(source);
        getCreateButton().setEnabled(false);
        this.open();
        getHeading().setText("Add Product Source in " + source.getCoo().getName());
        nameField.focus();
    }
}
