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
public class PlantRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private PlantRepository plantRepository;

    private Plant plantExpected1;
    private Plant plantExpected2;
    private Coo coo;

    @Before
    public void setDatabase() {
        coo = new Coo();
        coo.setName("Large Country");
        coo.setAbbreviation("LCY");

        testEntityManager.persist(coo);
        testEntityManager.flush();

        plantExpected1 = new Plant();
        plantExpected1.setName("Near Plant");
        plantExpected1.setCode("1234QW");
        plantExpected1.setCoo(coo);
        plantExpected2 = new Plant();
        plantExpected2.setName("Foreign Plant");
        plantExpected2.setCode("6543RF");
        plantExpected2.setCoo(coo);

        testEntityManager.persist(plantExpected1);
        testEntityManager.persist(plantExpected2);
        testEntityManager.flush();
    }

    @Test
    public void findByCooTest() {
        List<Plant> listActual = plantRepository.findByCooOrderByName(coo);
        assertEquals(List.of(plantExpected2, plantExpected1), listActual);
    }

    @Test
    public void findByCodeTest() {
        Optional<Plant> plantActual = plantRepository.findByCode(plantExpected1.getCode());
        Optional<Plant> plantActualEmpty = plantRepository.findByCode("9999ZZ");

        assertTrue(plantActual.isPresent());
        assertEquals(plantExpected1, plantActual.get());
        assertFalse(plantActualEmpty.isPresent());
    }

    @Test
    public void findByNameTestAndCooTest() {
        Optional<Plant> plantActual =
                plantRepository.findByNameAndCoo(plantExpected1.getName(), plantExpected1.getCoo());
        Optional<Plant> plantActualEmpty = plantRepository.findByNameAndCoo("Weird Name", coo);

        assertTrue(plantActual.isPresent());
        assertEquals(plantExpected1, plantActual.get());
        assertFalse(plantActualEmpty.isPresent());
    }

    @Test
    public void saveTest() {
        Plant plantToSave = new Plant();
        plantToSave.setName("New Plant");
        plantToSave.setCode("5678PK");
        plantToSave.setCoo(coo);

        plantRepository.save(plantToSave);
        boolean exists = plantRepository.existsByNameAndCoo(plantToSave.getName(), plantToSave.getCoo());
        List<Plant> listActual = plantRepository.findByCooOrderByName(coo);

        assertTrue(exists);
        assertEquals(3, listActual.size());
        assertTrue(listActual.contains(plantToSave));
    }

    @Test
    public void deleteTest() {
        plantRepository.delete(plantExpected1);

        boolean exists = plantRepository.existsByNameAndCoo(plantExpected1.getName(), plantExpected1.getCoo());
        List<Plant> listActual = plantRepository.findByCooOrderByName(coo);

        assertFalse(exists);
        assertEquals(1, listActual.size());
    }

    @Test
    public void existsByNameAndCooTest() {
        assertTrue(plantRepository.existsByNameAndCoo(plantExpected1.getName(), plantExpected1.getCoo()));
        assertFalse(plantRepository.existsByNameAndCoo("strange name", coo));
    }

    @Test
    public void existsByCodeTest() {
        assertTrue(plantRepository.existsByCode(plantExpected1.getCode()));
        assertFalse(plantRepository.existsByCode("9999ZZ"));
    }
}