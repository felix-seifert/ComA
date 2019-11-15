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

package com.felixseifert.coma.ui.views.bugrid;

import com.felixseifert.coma.backend.model.BusinessUnit;
import com.felixseifert.coma.backend.service.BusinessUnitService;
import com.felixseifert.coma.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.stream.Collectors;

@Route(value = "units", layout = MainLayout.class)
@PageTitle("Business Units")
public class BuGrid extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    public BuGrid(BusinessUnitService businessUnitService, BuEditor buEditor) {

        // Create grid structure with relevant columns
        Grid<BusinessUnit> grid = new Grid<>();
        grid.addColumn(BusinessUnit::getName).setHeader("Business Unit").setSortable(true);
        grid.addColumn(BuGrid::ProductManagerLabel).setHeader("Product Manager").setSortable(true);
        grid.getColumns().forEach(c -> c.setAutoWidth(true));

        // Set items of the grid
        grid.setItems(businessUnitService.getAllBusinessUnits());

        // Create actions displayed above the grid
        Button createButton= new Button("New Business Unit", VaadinIcon.PLUS.create());
        HorizontalLayout actions = new HorizontalLayout(createButton);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.CENTER);

        // Add previously created components to layout
        this.add(actions, grid);
        this.setSizeFull();

        // Add listeners
        grid.asSingleSelect().addValueChangeListener(e -> buEditor.openEditor(grid.asSingleSelect().getValue()));

        createButton.addClickListener(e -> buEditor.openEditor(new BusinessUnit()));

        buEditor.setChangeHandler(() -> {
            buEditor.close();
            grid.deselectAll();
            grid.setItems(businessUnitService.getAllBusinessUnits());
        });
    }

    private static String ProductManagerLabel(BusinessUnit businessUnit) {
        if(businessUnit.getProductManagers() != null) {
            return businessUnit.getProductManagers().stream()
                    .map(pm -> String.format("%s (%s)", pm.getName(), pm.getEmailAddress()))
                    .collect(Collectors.joining(", "));
        }
        return "";
    }
}
