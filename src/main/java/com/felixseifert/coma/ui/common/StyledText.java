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

package com.felixseifert.coma.ui.common;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Span;

public class StyledText extends Composite<Span> implements HasText {

    private static final long serialVersionUID = 1L;

    private Span content = new Span();

    private String text = "";

    public StyledText() {
        this("");
    }

    public StyledText(String htmlText) {
        setText(htmlText);
    }

    @Override
    protected Span initContent() {
        return content;
    }

    @Override
    public void setText(String htmlText) {
        if(htmlText == null) {
            htmlText = "";
        }
        if(htmlText.equals(text)) {
            return;
        }
        text = htmlText;
        content.removeAll();
        content.add(new Html("<span>" + htmlText + "</span>"));
    }

    @Override
    public String getText() {
        return text;
    }
}
