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

package com.felixseifert.coma.backend.model;

import com.felixseifert.coma.backend.repos.PartNumberObjectRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PartNumberObjectAuditableTest {

    @Autowired
    private PartNumberObjectRepository partNumberObjectRepository;

    private PartNumberObject partNumberObject1;
    private PartNumberObject partNumberObject2;

    @Before
    public void databaseSetup() {
        partNumberObject1 = new PartNumberObject();
        partNumberObject1.setPn("0123456789");
        partNumberObject1.setStartOfProduction(LocalDate.parse("2020-01-01"));
        partNumberObject1.setEndOfProduction(LocalDate.parse("2021-10-31"));
        partNumberObject1.setCreatedByTeamDepartment("CRA");
        partNumberObject2 = new PartNumberObject();
        partNumberObject2.setPn("9876543210");
        partNumberObject2.setStartOfProduction(LocalDate.parse("2019-11-01"));
        partNumberObject2.setEndOfProduction(LocalDate.parse("2022-08-31"));
        partNumberObject2.setCreatedByTeamDepartment("CRA");

        partNumberObject1 = partNumberObjectRepository.save(partNumberObject1);
        partNumberObject2 = partNumberObjectRepository.save(partNumberObject2);
    }

    @Test
    public void hasAuditablePropertiesTest() {
        assertTrue(partNumberObject1.getCreatedBy() instanceof String);
        assertTrue(partNumberObject1.getCreatedDate() instanceof LocalDate);
        assertTrue(partNumberObject1.getLastModifiedBy() instanceof String);
        assertTrue(partNumberObject1.getLastModifiedDateTime() instanceof LocalDateTime);
    }
}
