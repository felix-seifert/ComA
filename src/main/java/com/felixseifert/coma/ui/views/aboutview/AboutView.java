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

package com.felixseifert.coma.ui.views.aboutview;

import com.felixseifert.coma.ui.MainLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.boot.info.BuildProperties;

@Route(value = "about", layout = MainLayout.class)
@PageTitle("About ComA")
public class AboutView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    public AboutView(BuildProperties buildProperties) {

        HorizontalLayout basedOn = new HorizontalLayout(new Label("Based on: "),
                new Label(buildProperties.getName()));

        HorizontalLayout version = new HorizontalLayout(new Label("Version: "),
                new Label(buildProperties.getVersion()));

        Anchor link = new Anchor("https://felix-seifert.com", "Felix Seifert");
        link.setTarget("_blank");
        HorizontalLayout owner = new HorizontalLayout(new Label("Product Owner: "), link);

        HorizontalLayout contact = new HorizontalLayout(new Label("In case of any questions or bugs, contact " +
                "Felix under "), new Anchor("mailto:mail@felix-seifert.com", "mail@felix-seifert.com"));

        add(basedOn, version, owner, contact);
    }
}
