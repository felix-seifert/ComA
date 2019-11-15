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

import com.felixseifert.coma.backend.exceptions.BlankValueNotAllowedException;
import com.felixseifert.coma.backend.exceptions.EntityAlreadyExistsException;
import com.felixseifert.coma.backend.exceptions.EntityIDNotFoundException;
import com.felixseifert.coma.backend.model.dto.PartNumberObjectGridDTO;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.repos.PartNumberLobRepository;
import com.felixseifert.coma.backend.repos.PartNumberObjectRepository;
import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.PartNumberLob;
import com.felixseifert.coma.backend.model.PartNumberObject;
import com.felixseifert.coma.backend.model.PartNumberObject_;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PartNumberObjectServiceImplTest {

    @Autowired
    private PartNumberObjectServiceImpl partNumberObjectServiceImpl;

    @MockBean
    private PartNumberObjectRepository partNumberObjectRepository;

    @MockBean
    private PartNumberLobRepository partNumberLobRepository;

    private static String pnBeginning = "012";
    private static PartNumberObject partNumberObjectExpected1;
    private static PartNumberObject partNumberObjectExpected2;
    private static PartNumberObject partNumberObjectExpected3;
    private static PartNumberObjectGridDTO partNumberObjectExpected1Dto;
    private static PartNumberObjectGridDTO partNumberObjectExpected2Dto;
    private static PartNumberObjectGridDTO partNumberObjectExpected3Dto;
    private static Employee employeeExpected1;
    private static Employee employeeExpected2;

    @BeforeClass
    public static void setup() {
        partNumberObjectExpected1 = new PartNumberObject();
        partNumberObjectExpected1.setPn(pnBeginning + "3456789");
        partNumberObjectExpected1.setIdx("abc");
        partNumberObjectExpected1.setId(1);
        partNumberObjectExpected1.setStartOfProduction(LocalDate.parse("2020-01-01"));
        partNumberObjectExpected1.setEndOfProduction(LocalDate.parse("2021-10-31"));
        partNumberObjectExpected2 = new PartNumberObject();
        partNumberObjectExpected2.setId(2);
        partNumberObjectExpected2.setPn("9876543210");
        partNumberObjectExpected2.setStartOfProduction(LocalDate.parse("2019-11-01"));
        partNumberObjectExpected2.setEndOfProduction(LocalDate.parse("2022-08-31"));
        partNumberObjectExpected3 = new PartNumberObject();
        partNumberObjectExpected3.setPn(pnBeginning + "9876543");
        partNumberObjectExpected3.setStartOfProduction(LocalDate.parse("2019-12-15"));
        partNumberObjectExpected3.setEndOfProduction(LocalDate.parse("2025-03-10"));

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

        employeeExpected1 = new Employee();
        employeeExpected1.setName("Berta Bold");
        employeeExpected1.setEmailAddress("berta.bold@jp.bosch.com");
        employeeExpected1.addRole(Role.PRODUCT_SPECIALIST);
        partNumberObjectExpected1.setProductSpecialist(employeeExpected1);
        partNumberObjectExpected2.setProductSpecialist(employeeExpected1);
        employeeExpected2 = new Employee();
        employeeExpected2.setName("Carlos Creeds");
        employeeExpected2.setEmailAddress("carlos.creeds3@jp.bosch.com");
        employeeExpected2.addRole(Role.PRODUCT_MANAGER);
        partNumberObjectExpected1.setProductManager(employeeExpected2);
    }

    @Test
    public void getPartNumberObjectByIdTest() {
        when(partNumberObjectRepository.findById(partNumberObjectExpected1.getId()))
                .thenReturn(Optional.of(partNumberObjectExpected1));
        PartNumberObject actual = partNumberObjectServiceImpl
                .getPartNumberObjectById(partNumberObjectExpected1.getId());
        assertEquals(partNumberObjectExpected1, actual);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void getPartNumberObjectByIdTest_noPartNumberObjectFound() {
        when(partNumberObjectRepository.findById(anyInt()))
                .thenReturn(Optional.empty());
        partNumberObjectServiceImpl.getPartNumberObjectById(anyInt());
    }

    @Test
    public void getPartNumberObjectByPnStartsWithIgnoreCaseTest_pageable_withoutSort() {
        when(partNumberObjectRepository.findByPnStartsWithIgnoreCase(pnBeginning, 2, 1, true,
                PartNumberObject_.LAST_MODIFIED_DATE_TIME)).thenReturn(List.of(partNumberObjectExpected3Dto));
        List<PartNumberObjectGridDTO> actual = partNumberObjectServiceImpl
                .getPartNumberObjectsByPnStartsWithIgnoreCase(pnBeginning, 2, 1, false, null);
        verify(partNumberObjectRepository).findByPnStartsWithIgnoreCase(pnBeginning, 2, 1,
                true, PartNumberObject_.LAST_MODIFIED_DATE_TIME);
        assertEquals(List.of(partNumberObjectExpected3Dto), actual);
    }

    @Test
    public void getPartNumberObjectByPnStartsWithIgnoreCaseTest_pageable_sort() {
        when(partNumberObjectRepository.findByPnStartsWithIgnoreCase(pnBeginning, 2, 0, false, "pn"))
                .thenReturn(List.of(partNumberObjectExpected1Dto, partNumberObjectExpected3Dto));
        List<PartNumberObjectGridDTO> actual = partNumberObjectServiceImpl
                .getPartNumberObjectsByPnStartsWithIgnoreCase(pnBeginning, 2, 0, false, "pn");
        verify(partNumberObjectRepository)
                .findByPnStartsWithIgnoreCase(pnBeginning, 2, 0, false, "pn");
        assertEquals(List.of(partNumberObjectExpected1Dto, partNumberObjectExpected3Dto), actual);
    }

    @Test
    public void postPartNumberObjectTest() {
        when(partNumberObjectRepository.save(partNumberObjectExpected3)).thenReturn(partNumberObjectExpected3);
        assertEquals(partNumberObjectExpected3,
                partNumberObjectServiceImpl.postPartNumberObject(partNumberObjectExpected3));
        verify(partNumberObjectRepository).save(partNumberObjectExpected3);
        verify(partNumberLobRepository, never()).save(any());
    }

    @Test
    public void postPartNumberObjectTest_withComments() {
        PartNumberObject partNumberObjectWithComments = new PartNumberObject();
        partNumberObjectWithComments.setPn("abcdefghij");
        partNumberObjectWithComments.setStartOfProduction(LocalDate.parse("2019-10-10"));
        partNumberObjectWithComments.setComments("Interesting comments");

        partNumberObjectServiceImpl.postPartNumberObject(partNumberObjectWithComments);
        verify(partNumberObjectRepository).save(any());
        verify(partNumberLobRepository, never()).save(any());
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postPartNumberObjectTest_pnNull() {
        PartNumberObject partNumberObjectEmptyPn = new PartNumberObject();
        partNumberObjectEmptyPn.setPn(null);
        partNumberObjectEmptyPn.setStartOfProduction(LocalDate.parse("2019-10-10"));
        partNumberObjectServiceImpl.postPartNumberObject(partNumberObjectEmptyPn);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postPartNumberObjectTest_pnBlank() {
        PartNumberObject partNumberObjectBlankPn = new PartNumberObject();
        partNumberObjectBlankPn.setPn(" ");
        partNumberObjectBlankPn.setStartOfProduction(LocalDate.parse("2019-10-10"));
        partNumberObjectServiceImpl.postPartNumberObject(partNumberObjectBlankPn);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void postPartNumberObjectTest_idExists() {
        when(partNumberObjectRepository.existsById(partNumberObjectExpected1.getId())).thenReturn(true);
        partNumberObjectServiceImpl.postPartNumberObject(partNumberObjectExpected1);
    }

    @Test
    public void putPartNumberObjectTest() {
        when(partNumberObjectRepository.save(partNumberObjectExpected1)).thenReturn(partNumberObjectExpected1);
        assertEquals(partNumberObjectExpected1,
                partNumberObjectServiceImpl.putPartNumberObject(partNumberObjectExpected1));
        verify(partNumberObjectRepository).save(partNumberObjectExpected1);
    }

    @Test
    public void putPartNumberObjectTest_newComments() {
        PartNumberObject partNumberObjectWithComments = new PartNumberObject();
        partNumberObjectWithComments.setId(20);
        partNumberObjectWithComments.setPn("abcdefghij");
        partNumberObjectWithComments.setStartOfProduction(LocalDate.parse("2019-10-10"));
        partNumberObjectWithComments.setComments("Interesting comments");
        partNumberObjectServiceImpl.putPartNumberObject(partNumberObjectWithComments);
        verify(partNumberObjectRepository).save(any());
        verify(partNumberLobRepository).save(any());
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putPartNumberObjectTest_pnNull() {
        PartNumberObject partNumberObjectEmptyPn = new PartNumberObject();
        partNumberObjectEmptyPn.setPn(null);
        partNumberObjectEmptyPn.setStartOfProduction(LocalDate.parse("2019-10-10"));
        partNumberObjectServiceImpl.putPartNumberObject(partNumberObjectEmptyPn);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putPartNumberObjectTest_pnBlank() {
        PartNumberObject partNumberObjectBlankPn = new PartNumberObject();
        partNumberObjectBlankPn.setPn(" ");
        partNumberObjectBlankPn.setStartOfProduction(LocalDate.parse("2019-10-10"));
        partNumberObjectServiceImpl.putPartNumberObject(partNumberObjectBlankPn);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void putPartNumberObjectTest_idDoesNotExist() {
        partNumberObjectServiceImpl.putPartNumberObject(partNumberObjectExpected3);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void putPartNumberObjectTest_lobIdDoesNotExist() {
        PartNumberObject partNumberObjectWithoutLobId = new PartNumberObject();
        partNumberObjectWithoutLobId.setId(20);
        partNumberObjectWithoutLobId.setPn("abcdefghij");
        partNumberObjectWithoutLobId.setLobs(new PartNumberLob());
        partNumberObjectServiceImpl.putPartNumberObject(partNumberObjectExpected3);
    }

    @Test
    public void deletePartNumberObjectTest() {
        when(partNumberObjectRepository.existsById(partNumberObjectExpected1.getId())).thenReturn(true);
        partNumberObjectServiceImpl.deletePartNumberObject(partNumberObjectExpected1);
        verify(partNumberObjectRepository).delete(partNumberObjectExpected1);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void deletePartNumberObjectTest_partNumberDoesNotExists() {
        when(partNumberObjectRepository.existsById(partNumberObjectExpected1.getId())).thenReturn(false);
        partNumberObjectServiceImpl.deletePartNumberObject(partNumberObjectExpected1);
    }

    @Test
    public void existsPartNumberObjectByIdTest_true() {
        when(partNumberObjectRepository.existsById(partNumberObjectExpected1.getId())).thenReturn(true);
        assertTrue(partNumberObjectServiceImpl.existsPartNumberObjectById(partNumberObjectExpected1.getId()));
    }

    @Test
    public void existsPartNumberObjectByIdTest_false() {
        when(partNumberObjectRepository.existsById(partNumberObjectExpected1.getId())).thenReturn(false);
        assertFalse(partNumberObjectServiceImpl
                .existsPartNumberObjectById(partNumberObjectExpected1.getId()));
    }

    @Test
    public void existsPartNumberObjectByCompletePnTest_true() {
        when(partNumberObjectRepository.existsByCompletePn(
                partNumberObjectExpected1.getPn() + partNumberObjectExpected1.getIdx())).thenReturn(true);
        assertTrue(partNumberObjectServiceImpl.existsPartNumberObjectByCompletePn(
                partNumberObjectExpected1.getPn() + partNumberObjectExpected1.getIdx()));
    }

    @Test
    public void existsPartNumberObjectByCompletePnTest_false() {
        when(partNumberObjectRepository.existsByCompletePn(
                partNumberObjectExpected1.getPn() + partNumberObjectExpected1.getIdx())).thenReturn(false);
        assertFalse(partNumberObjectServiceImpl.existsPartNumberObjectByCompletePn(
                partNumberObjectExpected1.getPn() + partNumberObjectExpected1.getIdx()));
    }

    @Test
    public void countPartNumberObjectsTest() {
        when(partNumberObjectRepository.count()).thenReturn(20L);
        assertEquals(20, partNumberObjectRepository.count());
    }

    @Test
    public void countPartNumberObjectsByPnStartsWithIgnoreCaseTest() {
        when(partNumberObjectRepository.countByPnStartsWithIgnoreCase(pnBeginning)).thenReturn(20);
        assertEquals(20, partNumberObjectRepository.countByPnStartsWithIgnoreCase(pnBeginning));
    }
}
