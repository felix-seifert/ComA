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

import com.felixseifert.coma.backend.model.Coo;
import com.felixseifert.coma.backend.model.Plant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@DataJpaTest
@RunWith(SpringRunner.class)
public class CooRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private CooRepository cooRepository;

    @Autowired
    private PlantRepository plantRepository;

    private Coo cooExpected1;
    private Coo cooExpected2;
    private Plant plant;

    @Before
    public void setDatabase() {
        cooExpected1 = new Coo();
        cooExpected1.setName("Large Country");
        cooExpected1.setAbbreviation("LCY");
        cooExpected2 = new Coo();
        cooExpected2.setName("Tiny Country");
        cooExpected2.setAbbreviation("TCY");
        plant = new Plant();
        plant.setName("New Plant");
        plant.setCode("4560BM");
        cooExpected1.addPlant(plant);

        testEntityManager.persist(cooExpected1);
        testEntityManager.persist(cooExpected2);
        testEntityManager.flush();
    }

    @Test
    public void findAllTest() {
        List<Coo> listActual = cooRepository.findAllByOrderByAbbreviationAsc();
        assertEquals(List.of(cooExpected1, cooExpected2), listActual);
    }

    @Test
    public void findByNameTest() {
        Optional<Coo> cooActual = cooRepository.findByName(cooExpected1.getName());
        Optional<Coo> cooActualEmpty = cooRepository.findByName("strange name");

        assertTrue(cooActual.isPresent());
        assertEquals(cooExpected1, cooActual.get());
        assertFalse(cooActualEmpty.isPresent());
    }

    @Test
    public void saveTest() {
        Coo cooToSave = new Coo();
        cooToSave.setName("Country to Save");
        cooToSave.setAbbreviation("CTS");

        cooRepository.save(cooToSave);
        boolean exists = cooRepository.existsByName(cooToSave.getName());
        List<Coo> listActual = cooRepository.findAll();

        assertTrue(exists);
        assertEquals(3, listActual.size());
    }

    @Test
    public void deleteTest() {
        cooRepository.delete(cooExpected1);

        boolean exists = cooRepository.existsByName(cooExpected1.getName());
        List<Coo> listActual = cooRepository.findAll();

        assertFalse(exists);
        assertEquals(1, listActual.size());
        assertTrue(plantRepository.findAll().isEmpty());
    }

    @Test
    public void existsByNameTest() {
        assertTrue(cooRepository.existsByName(cooExpected1.getName()));
        assertFalse(cooRepository.existsByName("strange name"));
    }

    @Test
    public void existsByAbbreviationTest() {
        assertTrue(cooRepository.existsByAbbreviation(cooExpected1.getAbbreviation()));
        assertFalse(cooRepository.existsByAbbreviation("ZZ"));
    }
}