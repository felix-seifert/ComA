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

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Getter;

import java.util.Collection;

public class DropdownAddDeleteLayout<T> extends HorizontalLayout {

    private static final long serialVersionUID = 1L;

    @Getter
    private ComboBox<T> comboBox;

    @Getter
    private Button addButton;

    @Getter
    private Button deleteButton;

    public DropdownAddDeleteLayout(String label, String createLabel, ItemLabelGenerator<T> itemLabelGenerator,
                                   Collection<T> items) {
        comboBox = new ComboBox<>();
        comboBox.setLabel(label);
        comboBox.setMinWidth("320px");
        comboBox.setItemLabelGenerator(itemLabelGenerator);
        comboBox.setItems(items);

        addButton = new Button(createLabel, VaadinIcon.PLUS.create());
        addButton.getElement().getThemeList().add("primary");
        deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
        deleteButton.getElement().getThemeList().add("error");
        deleteButton.setEnabled(false);

        this.add(comboBox, deleteButton, addButton);
        this.setAlignItems(Alignment.BASELINE);

        comboBox.addValueChangeListener(event -> {
            if(event.getValue() != null) deleteButton.setEnabled(true);
            else deleteButton.setEnabled(false);
        });
    }
}
