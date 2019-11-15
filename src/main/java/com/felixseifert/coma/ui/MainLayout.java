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

package com.felixseifert.coma.ui;

import com.felixseifert.coma.ui.views.aboutview.AboutView;
import com.felixseifert.coma.ui.views.bugrid.BuGrid;
import com.felixseifert.coma.ui.views.customergrid.CustomerGrid;
import com.felixseifert.coma.ui.views.employeegrid.EmployeeGrid;
import com.felixseifert.coma.ui.views.pngrid.PnGrid;
import com.felixseifert.coma.ui.views.selectablevalueview.SelectableValueView;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import org.springframework.boot.info.BuildProperties;

// @PWA(name = "Part Number Communication Accelerator", shortName = "PNacc")
@Viewport("width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes, viewport-fit=cover")
public class MainLayout extends AppLayout implements RouterLayout {

    public MainLayout(BuildProperties buildProperties) {

        RouterLink pnGrid = new RouterLink(null, PnGrid.class);
        Icon pnMenuIcon = new Icon(VaadinIcon.LIST);
        HorizontalLayout pnMenuEntry = new HorizontalLayout(pnMenuIcon, new Span(new Text("PN List")));
        pnMenuEntry.setSpacing(true);
        pnGrid.add(pnMenuEntry);

        RouterLink customerGrid = new RouterLink(null, CustomerGrid.class);
        HorizontalLayout customerMenuEntry =
                new HorizontalLayout(new Icon(VaadinIcon.CASH), new Span(new Text("Customers")));
        customerMenuEntry.setSpacing(true);
        customerGrid.add(customerMenuEntry);

        RouterLink employeeGrid = new RouterLink(null, EmployeeGrid.class);
        HorizontalLayout employeeMenuEntry =
                new HorizontalLayout(new Icon(VaadinIcon.USERS), new Span(new Text("Employees")));
        employeeMenuEntry.setSpacing(true);
        employeeGrid.add(employeeMenuEntry);

        RouterLink buGrid = new RouterLink(null, BuGrid.class);
        HorizontalLayout buMenuEntry =
                new HorizontalLayout(new Icon(VaadinIcon.SITEMAP), new Span(new Text("Business Units")));
        buMenuEntry.setSpacing(true);
        buGrid.add(buMenuEntry);

        RouterLink selectableValueView = new RouterLink(null, SelectableValueView.class);
        HorizontalLayout selectableValueMenuEntry =
                new HorizontalLayout(new Icon(VaadinIcon.COMBOBOX), new Span(new Text("Dropdown Values")));
        selectableValueMenuEntry.setSpacing(true);
        selectableValueView.add(selectableValueMenuEntry);

        RouterLink aboutView = new RouterLink(null, AboutView.class);
        HorizontalLayout aboutMenuEntry =
                new HorizontalLayout(new Icon(VaadinIcon.INFO), new Span(new Text("About")));
        aboutMenuEntry.setSpacing(true);
        aboutView.add(aboutMenuEntry);

        Tabs tabs = new Tabs(new Tab(pnGrid),
                new Tab(customerGrid),
                new Tab(employeeGrid),
                new Tab(buGrid),
                new Tab(selectableValueView),
                new Tab(aboutView));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        this.addToDrawer(tabs);
        this.addToNavbar(new DrawerToggle(),
                new H4("ComA - Communication Accelerator"));
    }
}
