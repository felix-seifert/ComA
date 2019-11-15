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

package com.felixseifert.coma.ui.common.dialogs;

import com.felixseifert.coma.ui.common.ViewConstants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;

@Getter
public abstract class AbstractConfirmationDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private Label questionLabel;

    private TextField textField;

    private VerticalLayout optionalArea;

    private Checkbox checkbox = new Checkbox();

    public AbstractConfirmationDialog(String headingText, String readOnlyLabel, String question) {

        super(headingText);

        getContent().setWidth(ViewConstants.SIMPLE_DIALOG_WIDTH_STRING);

        questionLabel = new Label(question);
        getContent().add(questionLabel);

        textField = new TextField(readOnlyLabel);
        textField.setReadOnly(true);
        textField.setWidthFull();
        getContent().add(textField);

        optionalArea = new VerticalLayout();
        optionalArea.setPadding(false);
        getContent().add(optionalArea);
    }

    public void open(String readOnlyValue) {
        textField.setValue(readOnlyValue);
        this.open();
    }

    public Button getConfirmationButton() {
        return getOkButton();
    }
}
