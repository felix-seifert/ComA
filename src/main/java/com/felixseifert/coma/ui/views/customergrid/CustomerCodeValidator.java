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

import com.felixseifert.coma.backend.service.CustomerService;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import lombok.AllArgsConstructor;

import java.util.regex.Pattern;

@AllArgsConstructor
public class CustomerCodeValidator implements Validator<String> {

    private static final long serialVersionUID = 1L;

    private CustomerService customerService;

    @Override
    public ValidationResult apply(String code, ValueContext valueContext) {
        if(!Pattern.matches("[0-9]*", code)) return ValidationResult.error("Customer Codes consist of numbers.");
        if(code.length() != 8 && code.length() != 10) return ValidationResult.error("Customer Codes have 8 or 10 digits");
        if(customerService.exists(code)) return ValidationResult.error("Customer Code already exists.");
        return ValidationResult.ok();
    }
}
