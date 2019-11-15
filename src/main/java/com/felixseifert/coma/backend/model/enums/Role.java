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

import com.felixseifert.coma.backend.model.PartNumberObject_;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum Role {
    SLC(Name.SLC, null, 1),
    SJP(Name.SJP, null, 2),
    PRODUCT_MANAGER(Name.PRODUCT_MANAGER, PartNumberObject_.PRODUCT_MANAGER, 10),
    PRODUCT_SPECIALIST(Name.PRODUCT_SPECIALIST, PartNumberObject_.PRODUCT_SPECIALIST, 20),
    REQUESTER(Name.REQUESTER, PartNumberObject_.CREATED_BY_EMPLOYEE, 80),
    UNKNOWN("UNKNOWN ROLE", null, 99);

    @Getter
    private final String name;

    @Getter
    private final String pnVariable;

    @Getter
    private final Integer databaseCode;

    private static final Map<String, Role> ROLES_BY_NAME = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(role -> ROLES_BY_NAME.put(role.name, role));
    }

    public static Role roleOfName(String name) {
        return ROLES_BY_NAME.get(name);
    }

    public static class Name {
        // Role name must not have any parentheses
        public static final String SLC = "SLC Employee";
        public static final String SJP = "SJP Employee";
        public static final String PRODUCT_MANAGER = "Product Manager";
        public static final String PRODUCT_SPECIALIST = "Product Specialist";
        public static final String REQUESTER = "Request Creator";
    }
}
