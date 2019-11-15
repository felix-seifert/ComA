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

import com.felixseifert.coma.backend.model.ProductDescription;
import com.felixseifert.coma.backend.service.ProductDescriptionService;
import com.felixseifert.coma.ui.common.dialogs.AbstractSimpleCreateDialog;
import com.felixseifert.coma.ui.common.ViewConstants;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;

public class ProductDescriptionCreateDialog extends AbstractSimpleCreateDialog<ProductDescription> {

    private static final long serialVersionUID = 1L;

    private TextField codeField;

    private TextField descriptionField;

    ProductDescriptionCreateDialog(ProductDescriptionService productDescriptionService) {
        super(productDescriptionService::postProductDescription);

        codeField = new TextField("Product code (code for description)");
        codeField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        codeField.setWidthFull();
        codeField.setMaxLength(6);
        getContent().add(codeField);

        descriptionField = new TextField("Product description");
        descriptionField.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);
        descriptionField.setWidthFull();
        getContent().add(descriptionField);

        getBinder().forField(codeField)
                .asRequired("Please type in a product code.")
                .withValidator(new ProductCodeValidator(productDescriptionService))
                .bind("code");
        getBinder().forField(descriptionField)
                .asRequired("Please type in a product description.")
                .withValidator(d -> !productDescriptionService.existsByDescription(d),
                        "Product description already exists")
                .bind("description");

        getCompulsoryFields().add("code");
        getCompulsoryFields().add("description");

        getCreateButton().addClickListener(e ->
                Notification.show(String.format("Product Description \"%s (%s)\" created",
                        codeField.getValue(), descriptionField.getValue())));
    }

    @Override
    public void open(ProductDescription productDescription) {
        getBinder().setBean(productDescription);
        getCreateButton().setEnabled(false);
        this.open();
        getHeading().setText("Add Product Description");
        codeField.focus();
    }
}
