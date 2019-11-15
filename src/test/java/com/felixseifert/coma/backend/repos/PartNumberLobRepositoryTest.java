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

import com.felixseifert.coma.backend.model.PartNumberLob;
import com.felixseifert.coma.backend.model.PartNumberObject;
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
public class PartNumberLobRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private PartNumberLobRepository partNumberLobRepository;

    private PartNumberLob lobExpected;

    private PartNumberObject partNumberObject1;
    private PartNumberObject partNumberObject2;

    @Before
    public void setDatabase() {
        lobExpected = new PartNumberLob();
        lobExpected.setComments("Blub...");

        partNumberObject1 = new PartNumberObject();
        partNumberObject1.setPn("123456789");
        partNumberObject1.setStartOfProduction(LocalDate.parse("2020-01-01"));
        partNumberObject1.setCreatedByTeamDepartment("CRA");
        partNumberObject1.setLobs(lobExpected);
        partNumberObject2 = new PartNumberObject();
        partNumberObject2.setPn("9876543210");
        partNumberObject2.setStartOfProduction(LocalDate.parse("2019-11-01"));
        partNumberObject2.setCreatedByTeamDepartment("CRA");

        testEntityManager.persist(partNumberObject1);
        testEntityManager.persist(partNumberObject2);
        testEntityManager.flush();
    }

    @Test
    public void saveTest() {
        PartNumberLob partNumberLobToSave = new PartNumberLob();
        partNumberLobToSave.setComments("Bla...");
        partNumberObject2.setLobs(partNumberLobToSave);

        PartNumberLob lobActual = partNumberLobRepository.save(partNumberLobToSave);
        List<PartNumberLob> listActual = partNumberLobRepository.findAll();
        boolean lobExists = listActual.contains(partNumberLobToSave);

        assertEquals(partNumberLobToSave, lobActual);
        assertEquals(partNumberObject2, partNumberLobToSave.getPartNumberObject());
        assertTrue(lobExists);
        assertEquals(2, listActual.size());
        assertEquals(partNumberLobToSave, listActual.get(1));
    }

    @Test
    public void deleteTest() {
        partNumberLobRepository.delete(lobExpected);
        Optional<PartNumberLob> lobActual = partNumberLobRepository.findById(lobExpected.getId());
        assertFalse(lobActual.isPresent());
    }
}
