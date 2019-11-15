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

package com.felixseifert.coma.backend.repos;

import com.felixseifert.coma.backend.model.PartNumberObject;
import com.felixseifert.coma.backend.model.PartNumberObject_;
import com.felixseifert.coma.backend.model.enums.Progress;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.model.dto.PartNumberObjectGridDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@DataJpaTest
@RunWith(SpringRunner.class)
public class PartNumberObjectRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private PartNumberObjectRepository partNumberObjectRepository;

    private String pnBeginning = "012";
    private PartNumberObject partNumberObjectExpected1;
    private PartNumberObject partNumberObjectExpected2;
    private PartNumberObject partNumberObjectExpected3;
    private static PartNumberObjectGridDTO partNumberObjectExpected1Dto;
    private static PartNumberObjectGridDTO partNumberObjectExpected2Dto;
    private static PartNumberObjectGridDTO partNumberObjectExpected3Dto;
    private List<PartNumberObjectGridDTO> partNumberObjectList;

    @Before
    public void setDatabase() {
        partNumberObjectExpected1 = new PartNumberObject();
        partNumberObjectExpected1.setPn(pnBeginning + "3456789");
        partNumberObjectExpected1.setIdx("abc");
        partNumberObjectExpected1.setStartOfProduction(LocalDate.parse("2020-01-01"));
        partNumberObjectExpected1.setEndOfProduction(LocalDate.parse("2021-10-31"));
        partNumberObjectExpected1.setCreatedByTeamDepartment("CRA");
        partNumberObjectExpected1.getTaskRelease().setStatus(Progress.IN_PROGRESS);
        partNumberObjectExpected2 = new PartNumberObject();
        partNumberObjectExpected2.setPn("9876543210");
        partNumberObjectExpected2.setStartOfProduction(LocalDate.parse("2019-11-01"));
        partNumberObjectExpected2.setEndOfProduction(LocalDate.parse("2022-08-31"));
        partNumberObjectExpected2.setCreatedByTeamDepartment("CRA");
        partNumberObjectExpected3 = new PartNumberObject();
        partNumberObjectExpected3.setPn(pnBeginning + "9876543");
        partNumberObjectExpected3.setStartOfProduction(LocalDate.parse("2019-12-15"));
        partNumberObjectExpected3.setEndOfProduction(LocalDate.parse("2025-03-10"));
        partNumberObjectExpected3.setCreatedByTeamDepartment("CRA");

        testEntityManager.persist(partNumberObjectExpected1);
        testEntityManager.persist(partNumberObjectExpected2);
        testEntityManager.persist(partNumberObjectExpected3);
        testEntityManager.flush();

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

        partNumberObjectList = List.of(partNumberObjectExpected1Dto, partNumberObjectExpected2Dto,
                partNumberObjectExpected3Dto);
    }

    @Test
    public void findAllLimitTest() {
        List<PartNumberObjectGridDTO> partNumberObjectListActual = partNumberObjectRepository.findAllPnGridDtos(
                2, 0, true, PartNumberObject_.LAST_MODIFIED_DATE_TIME);
        assertTrue(partNumberObjectListActual.containsAll(
                List.of(partNumberObjectExpected3Dto, partNumberObjectExpected2Dto)));
        assertEquals(2, partNumberObjectListActual.size());
    }

    @Test(expected = Exception.class)
    public void findAllLimitTest_limitZero() {
        partNumberObjectRepository.findAllPnGridDtos(0, 0, false, null);
    }

    @Test
    public void findAllLimitOffsetTest() {
        List<PartNumberObjectGridDTO> partNumberObjectListActual = partNumberObjectRepository.findAllPnGridDtos(
                1, 2, true, PartNumberObject_.LAST_MODIFIED_DATE_TIME);
        assertEquals(1, partNumberObjectListActual.size());
        assertTrue(partNumberObjectList.contains(partNumberObjectListActual.get(0)));
    }

    @Test
    public void findByPnStartsWithIgnoreCaseTest_pageable() {
        List<PartNumberObjectGridDTO> partNumberObjectListActual = partNumberObjectRepository
                .findByPnStartsWithIgnoreCase(pnBeginning, 2, 1, true, PartNumberObject_.LAST_MODIFIED_DATE_TIME);
        List<PartNumberObjectGridDTO> partNumberObjectPageActualEmpty = partNumberObjectRepository
                .findByPnStartsWithIgnoreCase("22", 2, 1, true, PartNumberObject_.LAST_MODIFIED_DATE_TIME);

        assertEquals(1, partNumberObjectListActual.size());
        assertTrue(partNumberObjectList.contains(partNumberObjectListActual.get(0)));
        assertTrue(partNumberObjectPageActualEmpty.isEmpty());
    }

    @Test
    public void findByPnStartsWithIgnoreCaseTest_pageable_sort() {
        List<PartNumberObjectGridDTO> partNumberObjectListActual =
                partNumberObjectRepository.findByPnStartsWithIgnoreCase("", 2, 0, true, PartNumberObject_.PN);

        assertEquals(2, partNumberObjectListActual.size());
        assertEquals(partNumberObjectExpected2Dto, partNumberObjectListActual.get(0));
        assertEquals(partNumberObjectExpected3Dto, partNumberObjectListActual.get(1));
    }

    @Test(expected = Exception.class)
    public void findByPnStartsWithIgnoreCaseTest_wrongOffset() {
        partNumberObjectRepository.findByPnStartsWithIgnoreCase("", 2, -1, true, PartNumberObject_.PN);

    }

    @Test(expected = Exception.class)
    public void findByPnStartsWithIgnoreCaseTest_wrongLimit() {
        partNumberObjectRepository.findByPnStartsWithIgnoreCase("", 0, 0, true, PartNumberObject_.PN);
    }

    @Test
    public void findByIdTest() {
        Optional<PartNumberObject> partNumberActual =
                partNumberObjectRepository.findById(partNumberObjectExpected1.getId());
        Optional<PartNumberObject> partNumberActualEmpty = partNumberObjectRepository.findById(999);

        assertTrue(partNumberActual.isPresent());
        assertEquals(partNumberObjectExpected1, partNumberActual.get());
        assertFalse(partNumberActualEmpty.isPresent());
    }

    @Test
    public void saveTest() {
        PartNumberObject partNumberObjectExpected4 = new PartNumberObject();
        partNumberObjectExpected4.setPn("7777777777");
        partNumberObjectExpected4.setStartOfProduction(LocalDate.parse("2019-12-01"));
        partNumberObjectExpected4.setCreatedByTeamDepartment("RBR");

        PartNumberObject partNumberObjectActual = partNumberObjectRepository.save(partNumberObjectExpected4);
        boolean partNumberExists = partNumberObjectRepository.existsById(partNumberObjectExpected4.getId());
        List<PartNumberObject> partNumberObjectListActual = partNumberObjectRepository.findAll();

        assertEquals(partNumberObjectExpected4, partNumberObjectActual);
        assertTrue(partNumberExists);
        assertEquals(4, partNumberObjectListActual.size());
        assertEquals(partNumberObjectExpected4, partNumberObjectListActual.get(3));
    }

    @Test
    public void deleteTest() {
        partNumberObjectRepository.delete(partNumberObjectExpected1);

        boolean partNumberExists = partNumberObjectRepository
                .existsById(partNumberObjectExpected1.getId());
        List<PartNumberObject> partNumberObjectListActual = partNumberObjectRepository.findAll();

        assertFalse(partNumberExists);
        assertEquals(2, partNumberObjectListActual.size());
    }

    @Test
    public void existsByIdTest() {
        assertTrue(partNumberObjectRepository.existsById(partNumberObjectExpected1.getId()));
        assertFalse(partNumberObjectRepository.existsById(999));
    }

    @Test
    public void existsByCompletePnTest() {
        assertTrue(partNumberObjectRepository.existsByCompletePn(
                partNumberObjectExpected1.getPn() + partNumberObjectExpected1.getIdx()));
        assertFalse(partNumberObjectRepository.existsByCompletePn("9898989898zzz"));
    }

    @Test
    public void countTest() {
        assertEquals(3, partNumberObjectRepository.count());
    }

    @Test
    public void countByPartNumberStartsWithIgnoreCaseTest() {
        assertEquals(2, partNumberObjectRepository.countByPnStartsWithIgnoreCase(pnBeginning));
    }
}
