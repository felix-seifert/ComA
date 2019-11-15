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

package com.felixseifert.coma.backend.model.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;

@Converter
public class ProgressConverter implements AttributeConverter<Progress, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Progress progress) {
        if(progress == null) return null;
        return progress.getDatabaseCode();
    }

    @Override
    public Progress convertToEntityAttribute(Integer databaseCode) {
        if(databaseCode == null) return null;
        return Arrays.stream(Progress.values()).filter(r -> r.getDatabaseCode().equals(databaseCode))
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
