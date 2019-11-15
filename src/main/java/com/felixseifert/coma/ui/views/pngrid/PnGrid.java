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

import com.felixseifert.coma.backend.model.dto.PartNumberObjectGridDTO;
import com.felixseifert.coma.backend.service.PartNumberObjectService;
import com.felixseifert.coma.ui.MainLayout;
import com.felixseifert.coma.ui.common.Filter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Function;

@Route(value = "partnumbers", layout = MainLayout.class)
@PageTitle("PN List")
public class PnGrid extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private PartNumberObjectService partNumberObjectService;

    @Getter
    private Grid<PartNumberObjectGridDTO> grid;

    @Getter
    private TextField filter;

    @Getter
    private Button createButton;

    public PnGrid(PartNumberObjectEditor partNumberObjectEditor,
                  PartNumberObjectService partNumberObjectService) {

        this.partNumberObjectService = partNumberObjectService;

        Function<PartNumberObjectGridDTO, String> currentEmployeeHeader = pnDto -> {
            if(pnDto.getCurrentEmployeeName() != null && pnDto.getCurrentEmployeeRole() != null) {
                return String.format("%s (%s)", pnDto.getCurrentEmployeeName(),
                        pnDto.getCurrentEmployeeRole().getName());
            }
            if(pnDto.getReleaseCompletedAt() == null && pnDto.getCurrentEmployeeRole() != null) {
                return "Set new " + pnDto.getCurrentEmployeeRole().getName();
            }
            return "";
        };

        // Create grid structure with relevant columns
        grid = new Grid<>();
        grid.addColumn(PartNumberObjectGridDTO::getId).setHeader("ID").setSortProperty("id");
        grid.addColumn(PartNumberObjectGridDTO::getPn).setHeader("PN").setSortProperty("pn");
        grid.addColumn(PartNumberObjectGridDTO::getDueDate).setHeader("Due Date").setSortProperty("dueDate");
        grid.addColumn(PartNumberObjectGridDTO::getCreatedByTeamDepartment).setHeader("Created by")
                .setSortProperty("createdByTeamDepartment");
        grid.addColumn(PartNumberObjectGridDTO::getCreatedDate).setHeader("Created at")
                .setSortProperty("createdDate");
        grid.addColumn(currentEmployeeHeader::apply)
                .setHeader("Responsible Employee").setSortProperty("currentEmployeeName");
        grid.addColumn(pnDTO -> pnDTO.getStatus() != null ? pnDTO.getStatus().getName() : null)
                .setHeader("Progress").setSortProperty("status");
        grid.addColumn(pnDTO -> pnDTO.getCustomerNotification() != null ?
                pnDTO.getCustomerNotification().getName() : null)
                .setHeader("Customer Notification").setSortProperty("customerNotification");
        grid.addColumn(pnDTO -> pnDTO.getReleaseCompletedAt() != null ?
                pnDTO.getReleaseCompletedAt().toLocalDate() : null)
                .setHeader("Release Date").setSortProperty("releaseCompletedAt");
        grid.getColumns().forEach(c -> c.setAutoWidth(true));

        // Set up dataProvider for grid for lazy loading
        DataProvider<PartNumberObjectGridDTO, String> dataProvider = createPartNumberObjectDataProvider();
        ConfigurableFilterDataProvider<PartNumberObjectGridDTO, Void, String> dataProviderWrapper =
                dataProvider.withConfigurableFilter();
        grid.setDataProvider(dataProviderWrapper);

        // Create actions displayed above the grid
        filter = new Filter("Search for PN");
        createButton = new Button("New Request", VaadinIcon.PLUS.create());
        HorizontalLayout actions = new HorizontalLayout(filter, createButton);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.CENTER);

        // Add previously created components to layout
        this.add(actions, grid);
        this.setSizeFull();

        // Add listeners
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> {
            String filterText = e.getValue();
            if(StringUtils.isBlank(filterText)) {
                filterText = null;
            }
            dataProviderWrapper.setFilter(filterText);
        });

        grid.asSingleSelect().addValueChangeListener(e ->
                partNumberObjectEditor.openEditor(grid.asSingleSelect().getValue()));

        createButton.addClickListener(e -> partNumberObjectEditor.openEditor(new PartNumberObjectGridDTO()));

        partNumberObjectEditor.setChangeHandler(() -> {
            partNumberObjectEditor.close();
            grid.deselectAll();
            grid.getDataProvider().refreshAll();
        });
    }

    private DataProvider<PartNumberObjectGridDTO, String> createPartNumberObjectDataProvider() {
        return DataProvider.fromFilteringCallbacks(
                query -> getPartNumberObjectListProvided(query).stream(),
                query -> partNumberObjectService
                        .countPartNumberObjectsByPnStartsWithIgnoreCase(query.getFilter().orElse(null)));
    }

    private List<PartNumberObjectGridDTO> getPartNumberObjectListProvided(
            Query<PartNumberObjectGridDTO, String> query) {
        String sortProperty = null;
        boolean descending = false;
        SortOrder<String> firstSortOrder = query.getSortOrders().stream().findFirst().orElse(null);
        if(firstSortOrder != null) {
            sortProperty = firstSortOrder.getSorted();
            descending = firstSortOrder.getDirection() == SortDirection.DESCENDING;
        }

        String filter = query.getFilter().orElse(null);
        int limit = query.getLimit();
        int offset = query.getOffset();
        return partNumberObjectService
                .getPartNumberObjectsByPnStartsWithIgnoreCase(filter, limit, offset, descending, sortProperty);
    }
}
