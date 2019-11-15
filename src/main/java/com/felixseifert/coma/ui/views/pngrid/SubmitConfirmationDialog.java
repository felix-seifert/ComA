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

import com.felixseifert.coma.ui.common.dialogs.AbstractConfirmationDialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.apache.commons.lang3.StringUtils;

public class SubmitConfirmationDialog extends AbstractConfirmationDialog {

    private static final long serialVersionUID = 1L;

    private SubmitConfirmationDialog(String headingText, String readOnlyLabel, String question) {
        super(headingText, readOnlyLabel, question);
    }

    public SubmitConfirmationDialog() {
        this("", "", "");

        getConfirmationButton().setIconAfterText(false);
        getConfirmationButton().setIcon(VaadinIcon.CHECK.create());
        getConfirmationButton().getElement().getThemeList().add("primary");
    }

    @Override
    public void open(String readOnlyValue) {
        this.open(Submission.NEXT_EMPLOYEE, readOnlyValue);
    }

    public void open(Submission submission) {
        this.open(submission, "");
    }

    public void open(Submission submission, String readOnlyValue) {
        getHeading().setText(submission.getHeadingText());
        getQuestionLabel().setText(submission.getQuestion());
        getTextField().setLabel(submission.getTextFieldLabelText());
        getTextField().setVisible(StringUtils.isNotBlank(readOnlyValue));
        getConfirmationButton().setText(submission.getButtonText());
        getConfirmationButton().setEnabled(submission.isButtonEnabled());
        super.open(readOnlyValue);
    }
}
