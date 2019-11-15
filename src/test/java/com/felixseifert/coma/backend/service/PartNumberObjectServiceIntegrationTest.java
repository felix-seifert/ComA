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

import com.felixseifert.coma.backend.model.PartNumberObject;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.model.dto.PartNumberObjectGridDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PartNumberObjectServiceIntegrationTest {

    @Autowired
    private PartNumberObjectService partNumberObjectService;

    private String pnBeginning;
    private PartNumberObject partNumberObjectExpected1;
    private PartNumberObject partNumberObjectExpected2;
    private PartNumberObject partNumberObjectExpected3;
    private PartNumberObjectGridDTO partNumberObjectExpected1Dto;
    private PartNumberObjectGridDTO partNumberObjectExpected2Dto;
    private PartNumberObjectGridDTO partNumberObjectExpected3Dto;

    @Before
    public void setup() {
        pnBeginning = "012";
        partNumberObjectExpected1 = new PartNumberObject();
        partNumberObjectExpected1.setPn(pnBeginning + "3456789");
        partNumberObjectExpected1.setStartOfProduction(LocalDate.parse("2020-01-01"));
        partNumberObjectExpected1.setCreatedByTeamDepartment("CRA");
        partNumberObjectExpected1.setComments("Old text");
        partNumberObjectService.postPartNumberObject(partNumberObjectExpected1);
        partNumberObjectExpected2 = new PartNumberObject();
        partNumberObjectExpected2.setPn("9876543210");
        partNumberObjectExpected2.setStartOfProduction(LocalDate.parse("2019-11-01"));
        partNumberObjectExpected2.setCreatedByTeamDepartment("CRA");
        partNumberObjectService.postPartNumberObject(partNumberObjectExpected2);
        partNumberObjectExpected3 = new PartNumberObject();
        partNumberObjectExpected3.setPn(pnBeginning + "9876543");
        partNumberObjectExpected3.setStartOfProduction(LocalDate.parse("2019-12-15"));
        partNumberObjectExpected3.setCreatedByTeamDepartment("CRA");
        partNumberObjectService.postPartNumberObject(partNumberObjectExpected3);

        String employeeName = "Heinrich Hanzen";

        partNumberObjectExpected1Dto = new PartNumberObjectGridDTO(partNumberObjectExpected1.getId(),
                partNumberObjectExpected1.getPn(), partNumberObjectExpected1.getDueDate(),
                partNumberObjectExpected1.getCreatedByTeamDepartment(), partNumberObjectExpected1.getCreatedDate(),
                employeeName, Role.REQUESTER, null, null, null);
        partNumberObjectExpected2Dto = new PartNumberObjectGridDTO(partNumberObjectExpected2.getId(),
                partNumberObjectExpected2.getPn(), partNumberObjectExpected2.getDueDate(),
                partNumberObjectExpected2.getCreatedByTeamDepartment(), partNumberObjectExpected2.getCreatedDate(),
                employeeName, Role.REQUESTER, null, null, null);
        partNumberObjectExpected3Dto = new PartNumberObjectGridDTO(partNumberObjectExpected3.getId(),
                partNumberObjectExpected3.getPn(), partNumberObjectExpected3.getDueDate(),
                partNumberObjectExpected3.getCreatedByTeamDepartment(), partNumberObjectExpected3.getCreatedDate(),
                employeeName, Role.REQUESTER, null, null, null);
    }

    @Test
    @Transactional
    public void getPartNumberObjectByPnStartsWithIgnoreCaseTest_pageable_sort() {
        List<PartNumberObjectGridDTO> actual = partNumberObjectService
                .getPartNumberObjectsByPnStartsWithIgnoreCase(pnBeginning, 2, 0, false, "pn");
        assertEquals(List.of(partNumberObjectExpected1Dto, partNumberObjectExpected3Dto), actual);
    }

    @Test
    @Transactional
    public void getPartNumberObjectByIdTest() {
        PartNumberObject partNumberObjectActual =
                partNumberObjectService.getPartNumberObjectById(partNumberObjectExpected1.getId());
        assertEquals(partNumberObjectExpected1.getComments(), partNumberObjectActual.getComments());
    }

    @Test
    @Transactional
    public void postPartNumberObjectTest() {
        PartNumberObject partNumberObjectToSave = new PartNumberObject();
        partNumberObjectToSave.setPn("abcdefghij");
        partNumberObjectToSave.setStartOfProduction(LocalDate.parse("2024-05-15"));
        partNumberObjectToSave.setCreatedByTeamDepartment("SLC");
        partNumberObjectToSave.setComments("Loooong Text...");

        PartNumberObject partNumberObjectActual = partNumberObjectService.postPartNumberObject(partNumberObjectToSave);
        List<PartNumberObjectGridDTO> partNumberObjectListActual = partNumberObjectService
                .getPartNumberObjectsByPnStartsWithIgnoreCase(null, 10, 0, false, null);

        assertEquals(partNumberObjectToSave.getComments(), partNumberObjectActual.getLobs().getComments());
        assertEquals(4, partNumberObjectListActual.size());
    }

    @Test
    @Transactional
    public void putPartNumberObjectTest_removeComments() {
        partNumberObjectExpected1.setComments(null);

        PartNumberObject partNumberObjectActual =
                partNumberObjectService.putPartNumberObject(partNumberObjectExpected1);
        List<PartNumberObjectGridDTO> partNumberObjectListActual = partNumberObjectService
                .getPartNumberObjectsByPnStartsWithIgnoreCase(null, 10, 0, false, null);

        assertEquals(partNumberObjectExpected1, partNumberObjectActual);
        assertNull(partNumberObjectActual.getLobs());
        assertEquals(3, partNumberObjectListActual.size());
    }

    @Test
    @Transactional
    public void putPartNumberObjectTest_newComments() {
        partNumberObjectExpected2.setComments("New Text");

        PartNumberObject partNumberObjectActual =
                partNumberObjectService.putPartNumberObject(partNumberObjectExpected2);
        List<PartNumberObjectGridDTO> partNumberObjectListActual = partNumberObjectService
                .getPartNumberObjectsByPnStartsWithIgnoreCase(null, 10, 0, false, null);

        assertEquals(partNumberObjectExpected2, partNumberObjectActual);
        assertEquals(partNumberObjectExpected2.getComments(), partNumberObjectActual.getLobs().getComments());
        assertEquals(3, partNumberObjectListActual.size());
    }

    @Test
    @Transactional
    public void putPartNumberObjectTest_updateComments() {
        partNumberObjectExpected1.setComments("Updated Text");

        PartNumberObject partNumberObjectActual =
                partNumberObjectService.putPartNumberObject(partNumberObjectExpected1);
        List<PartNumberObjectGridDTO> partNumberObjectListActual = partNumberObjectService
                .getPartNumberObjectsByPnStartsWithIgnoreCase(null, 10, 0, false, null);

        assertEquals(partNumberObjectExpected1, partNumberObjectActual);
        assertEquals(partNumberObjectExpected1.getComments(), partNumberObjectActual.getLobs().getComments());
        assertEquals(3, partNumberObjectListActual.size());
    }
}
