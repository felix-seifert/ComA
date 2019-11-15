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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.binder.Binder;
import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractSimpleCreateDialog<CreatedClass> extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    @Getter
    private Binder<CreatedClass> binder;

    @Getter
    private Set<String> compulsoryFields = new HashSet<>();

    protected AbstractSimpleCreateDialog(Consumer<CreatedClass> saveOperation) {

        Class<CreatedClass> gType =
                (Class<CreatedClass>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        binder = new Binder<>(gType);

        getContent().setMargin(false);
        getContent().setWidth(ViewConstants.SIMPLE_DIALOG_WIDTH_STRING);

        getOkButton().setText("Create");
        getOkButton().setIcon(VaadinIcon.CHECK.create());
        getOkButton().getElement().getThemeList().add("primary");

        binder.addStatusChangeListener(status -> {
            boolean emptyFields = compulsoryFields.stream()
                    .flatMap(prop -> getBinder().getBinding(prop).stream())
                    .anyMatch(binding -> binding.getField().isEmpty());
            getOkButton().setEnabled(!status.hasValidationErrors() && !emptyFields);
        });

        getOkButton().addClickListener(event -> {
            saveOperation.accept(binder.getBean());
            getChangeHandler().onChange();
        });
    }

    public abstract void open(CreatedClass object);

    public Button getCreateButton() {
        return getOkButton();
    }
}
