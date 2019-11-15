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

package com.felixseifert.coma.backend.service;

import com.felixseifert.coma.backend.model.SelectableValue;
import com.felixseifert.coma.backend.model.enums.ValueGroup;

import java.util.List;

public interface SelectableValueService {

    List<SelectableValue> getAllSelectableValues();

    List<SelectableValue> getSelectableValuesByValueGroup(ValueGroup valueGroup);

    SelectableValue postSelectableValue(SelectableValue selectableValue);

    SelectableValue postSelectableValue(String label, ValueGroup valueGroup);

    void deleteSelectableValue(SelectableValue selectableValue);

    boolean exists(String label, ValueGroup valueGroup);
}
