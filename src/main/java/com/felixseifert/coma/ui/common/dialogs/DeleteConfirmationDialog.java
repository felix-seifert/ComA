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

import com.vaadin.flow.component.icon.VaadinIcon;

public class DeleteConfirmationDialog extends AbstractConfirmationDialog {

    private static final long serialVersionUID = 1L;

    public DeleteConfirmationDialog(String headingText, String readOnlyLabel) {
        super(headingText, readOnlyLabel, "Are you sure you want to delete this item? This action cannot be undone.");

        getConfirmationButton().setText("Yes, delete");
        getConfirmationButton().setIconAfterText(true);
        getConfirmationButton().setIcon(VaadinIcon.TRASH.create());
        getConfirmationButton().getElement().getThemeList().add("error");
    }
}
