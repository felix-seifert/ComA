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

import com.felixseifert.coma.backend.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Submission {
    NEXT_EMPLOYEE(Heading.NEXT_EMPLOYEE, Question.NEXT_EMPLOYEE, TextFieldLabelText.NEXT_EMPLOYEE, ButtonText.NEXT_EMPLOYEE, ButtonEnabled. NEXT_EMPLOYEE),
    FIRST_STEP_EMPTY(Heading.FIRST_STEP_EMPTY, Question.FIRST_STEP_EMPTY, TextFieldLabelText.FIRST_STEP_EMPTY, ButtonText.FIRST_STEP_EMPTY, ButtonEnabled.FIRST_STEP_EMPTY),
    FINISHED(Heading.FINISHED, Question.FINISHED, TextFieldLabelText.FINISHED, ButtonText.FINISHED, ButtonEnabled.FINISHED),
    RESPONSIBILITY_MISSING(Heading.RESPONSIBILITY_MISSING, Question.RESPONSIBILITY_MISSING, TextFieldLabelText.RESPONSIBILITY_MISSING, ButtonText.RESPONSIBILITY_MISSING, ButtonEnabled.RESPONSIBILITY_MISSING);

    @Getter
    private String headingText;

    @Getter
    private String question;

    @Getter
    private String textFieldLabelText;

    @Getter
    private String buttonText;

    @Getter
    private boolean buttonEnabled;

    private static class Heading {
        public static final String NEXT_EMPLOYEE = "Submit PN Entry to Next Employee";
        public static final String FIRST_STEP_EMPTY = "Release Flow Not Existent";
        public static final String FINISHED = "Mark Request as Finished";
        public static final String RESPONSIBILITY_MISSING = "Responsibility Missing";
    }

    private static class Question {
        public static final String NEXT_EMPLOYEE = "Do you want to submit the entry to the next employee?";
        public static final String FIRST_STEP_EMPTY = "You created the PN Request and no Release Flow is selected. " +
                "The PN Request cannot be forwarded to anyone. If you want to cancel this PN Request, delete it.";
        public static final String FINISHED = "The request would be marked as finished and the PN Request would " +
                "be automatically forwarded to the " + Role.REQUESTER.getName() +
                ". Then, the PN Request could not be edited anymore.";
        public static final String RESPONSIBILITY_MISSING = "No responsible employee is assigned for the next step. " +
                "Assign the remaining responsible employee(s) to submit the request.";
    }

    private static class TextFieldLabelText {
        public static final String NEXT_EMPLOYEE = "Next employee";
        public static final String FIRST_STEP_EMPTY = "";
        public static final String FINISHED = "";
        public static final String RESPONSIBILITY_MISSING = "";
    }

    private static class ButtonText {
        public static final String NEXT_EMPLOYEE = "Submit";
        public static final String FIRST_STEP_EMPTY = "No Submission Possible";
        public static final String FINISHED = "Mark as Finished";
        public static final String RESPONSIBILITY_MISSING = "Submission not Possible";
    }

    private static class ButtonEnabled {
        public static final boolean NEXT_EMPLOYEE = true;
        public static final boolean FIRST_STEP_EMPTY = false;
        public static final boolean FINISHED = true;
        public static final boolean RESPONSIBILITY_MISSING = false;
    }
}
