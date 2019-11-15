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

package com.felixseifert.coma.ui.views.customergrid;

import com.felixseifert.coma.backend.model.Customer;
import com.felixseifert.coma.backend.service.CustomerService;
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

@Route(value = "customers", layout = MainLayout.class)
@PageTitle("Customers")
public class CustomerGrid extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private CustomerService customerService;

    public CustomerGrid(CustomerService customerService, CustomerEditor customerEditor) {

        this.customerService = customerService;

        // Create grid structure with relevant columns
        Grid<Customer> grid = new Grid<>();
        grid.addColumn(Customer::getCode).setHeader("Customer Code").setSortable(true);
        grid.addColumn(Customer::getName).setHeader("Name").setSortable(true);
        grid.addColumn(Customer::getDeliveryLocation).setHeader("Delivery Location").setSortable(true);
        grid.addColumn(Customer::getCurrency).setHeader("Currency").setSortable(true);
        grid.getColumns().forEach(c -> c.setAutoWidth(true));

        // Set items of the grid
        grid.setItems(filterCustomers(null));

        // Create actions displayed above the grid
        TextField filter = new Filter("Search for Customer Name");
        Button createButton= new Button("New Customer", VaadinIcon.PLUS.create());
        HorizontalLayout actions = new HorizontalLayout(filter, createButton);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.CENTER);

        // Add previously created components to layout
        this.add(actions, grid);
        this.setSizeFull();

        // Add listeners
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> grid.setItems(filterCustomers(e.getValue())));

        grid.asSingleSelect().addValueChangeListener(e ->
                customerEditor.openEditor(grid.asSingleSelect().getValue()));

        createButton.addClickListener(e -> customerEditor.openEditor(new Customer()));

        customerEditor.setChangeHandler(() -> {
            customerEditor.close();
            grid.deselectAll();
            grid.setItems(filterCustomers(filter.getValue()));
        });
    }

    private List<Customer> filterCustomers(String filterText) {
        if(StringUtils.isNotBlank(filterText)) {
            return customerService.getCustomersByNameStartsWithIgnoreCase(filterText);
        }
        else {
            return customerService.getAllCustomers();
        }
    }
}
