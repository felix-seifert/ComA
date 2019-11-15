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

package com.felixseifert.coma.ui.views.employeegrid;

import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.service.EmployeeService;
import com.felixseifert.coma.ui.MainLayout;
import com.felixseifert.coma.ui.common.Filter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "employees", layout = MainLayout.class)
@PageTitle("Employees")
public class EmployeeGrid extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private EmployeeService employeeService;

    public EmployeeGrid(EmployeeService employeeService, EmployeeEditor employeeEditor) {

        this.employeeService = employeeService;

        // Create grid structure with relevant columns
        Grid<Employee> grid = new Grid<>();
        grid.addColumn(Employee::getName).setHeader("Name").setSortable(true);
        grid.addColumn(Employee::getEmailAddress).setHeader("Email Address").setSortable(true);
        grid.addColumn(Employee::getTeam).setHeader("Team").setSortable(true);
        grid.addColumn(Employee::getLocation).setHeader("Location").setSortable(true);
        grid.addColumn(e -> e.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")))
                .setHeader("Roles").setSortable(true);
        grid.getColumns().forEach(c -> c.setAutoWidth(true));

        // Set items of the grid
        grid.setItems(filterEmployees(null));

        // Create actions displayed above the grid
        TextField filter = new Filter("Search for Employee");
        Button createButton= new Button("New Employee", VaadinIcon.PLUS.create());
        HorizontalLayout actions = new HorizontalLayout(filter, createButton);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.CENTER);

        // Add previously created components to layout
        this.add(actions, grid);
        this.setSizeFull();

        // Add listeners
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> grid.setItems(filterEmployees(e.getValue())));

        grid.asSingleSelect().addValueChangeListener(e ->
                employeeEditor.openEditor(grid.asSingleSelect().getValue()));

        createButton.addClickListener(e -> employeeEditor.openEditor(new Employee()));

        employeeEditor.setChangeHandler(() -> {
            employeeEditor.close();
            grid.deselectAll();
            grid.setItems(filterEmployees(filter.getValue()));
        });
    }

    private List<Employee> filterEmployees(String filterText) {
        if(StringUtils.isNotBlank(filterText)) {
            return employeeService.getEmployeesByNameStartsWithIgnoreCase(filterText);
        }
        return employeeService.getAllEmployees();
    }
}
