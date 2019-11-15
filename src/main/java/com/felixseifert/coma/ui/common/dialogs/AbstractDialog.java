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

import com.felixseifert.coma.ui.common.ChangeHandler;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class AbstractDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    @Getter(AccessLevel.PACKAGE)
    @Setter
    private ChangeHandler changeHandler;

    private H3 heading;

    private VerticalLayout content;

    private Button okButton;
    private Button cancelButton;

    protected AbstractDialog() {
        this("");
    }

    protected AbstractDialog(String headingText) {

        VerticalLayout verticalLayout = new VerticalLayout();

        heading = new H3(headingText);
        verticalLayout.add(heading);

        content = new VerticalLayout();
        content.setPadding(false);
        verticalLayout.add(content);

        cancelButton = new Button("Cancel");
        okButton = new Button();
        HorizontalLayout actions = new HorizontalLayout(cancelButton, okButton);
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        verticalLayout.add(actions);

        this.add(verticalLayout);

        cancelButton.addClickListener(e -> this.close());
        okButton.addClickListener(e -> {
            this.close();
            changeHandler.onChange();
        });
    }
}
