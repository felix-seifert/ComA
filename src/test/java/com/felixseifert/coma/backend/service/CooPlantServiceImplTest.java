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
import com.felixseifert.coma.backend.model.Coo;
import com.felixseifert.coma.backend.model.Plant;
import com.felixseifert.coma.backend.repos.CooRepository;
import com.felixseifert.coma.backend.repos.PlantRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CooPlantServiceImplTest {

    @Autowired
    private CooPlantService cooPlantService;

    @MockBean
    private CooRepository cooRepository;

    @MockBean
    private PlantRepository plantRepository;

    private static Plant plantExpected1;
    private static Plant plantExpected2;
    private static Coo cooExpected1;
    private static Coo cooExpected2;

    @BeforeClass
    public static void setup() {
        plantExpected1 = new Plant();
        plantExpected1.setName("Big Plant");
        plantExpected1.setCode("1234QW");
        plantExpected2 = new Plant();
        plantExpected2.setName("Small Plant");
        plantExpected2.setCode("8790GH");
        cooExpected1 = new Coo();
        cooExpected1.setName("Large Country");
        cooExpected1.setAbbreviation("LCY");
        cooExpected1.addPlant(plantExpected1);
        cooExpected1.addPlant(plantExpected2);
        cooExpected2 = new Coo();
        cooExpected2.setName("Tiny Country");
        cooExpected2.setAbbreviation("TCY");
    }

    @Test
    public void getAllCoosTest() {
        when(cooRepository.findAllByOrderByAbbreviationAsc()).thenReturn(List.of(cooExpected1, cooExpected2));
        List<Coo> actual = cooPlantService.getAllCoos();
        assertEquals(List.of(cooExpected1, cooExpected2), actual);
    }

    @Test
    public void getAllCoosTest_nooCoosFound() {
        when(cooRepository.findAll()).thenReturn(List.of());
        List<Coo> actual = cooPlantService.getAllCoos();
        assertEquals(0, actual.size());
    }

    @Test
    public void postCooTest() {
        when(cooRepository.save(cooExpected1)).thenReturn(cooExpected1);
        assertEquals(cooExpected1, cooPlantService.postCoo(cooExpected1));
        verify(cooRepository).save(cooExpected1);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postCooTest_nameNull() {
        Coo cooEmptyName = new Coo();
        cooEmptyName.setName(null);
        cooEmptyName.setAbbreviation("ABC");
        cooPlantService.postCoo(cooEmptyName);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postCooTest_nameBlank() {
        Coo cooBlankName = new Coo();
        cooBlankName.setName(" ");
        cooBlankName.setAbbreviation("ABC");
        cooPlantService.postCoo(cooBlankName);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void postCooTest_nameExists() {
        when(cooRepository.existsByName(cooExpected1.getName())).thenReturn(true);
        cooPlantService.postCoo(cooExpected1);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postCooTest_abbrNull() {
        Coo cooEmptyAbbr = new Coo();
        cooEmptyAbbr.setName("Long Name");
        cooEmptyAbbr.setAbbreviation(null);
        cooPlantService.postCoo(cooEmptyAbbr);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postCooTest_abbrBlank() {
        Coo cooBlankAbbr = new Coo();
        cooBlankAbbr.setName("Long Name");
        cooBlankAbbr.setAbbreviation(" ");
        cooPlantService.postCoo(cooBlankAbbr);
    }

    @Test()
    public void deleteCooTest() {
        when(cooRepository.existsByName(cooExpected1.getName())).thenReturn(true);
        cooPlantService.deleteCoo(cooExpected1);
        verify(cooRepository).delete(cooExpected1);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void deleteCooTest_cooNotFound() {
        when(cooRepository.existsByName(cooExpected1.getName())).thenReturn(false);
        cooPlantService.deleteCoo(cooExpected1);
    }

    @Test
    public void existsByCooNameTest_true() {
        when(cooRepository.existsByName(cooExpected1.getName())).thenReturn(true);
        assertTrue(cooPlantService.existsByCooName(cooExpected1.getName()));
    }

    @Test
    public void existsByCooNameTest_false() {
        when(cooRepository.existsByName(cooExpected1.getName())).thenReturn(false);
        assertFalse(cooPlantService.existsByCooName(cooExpected1.getName()));
    }

    @Test
    public void existsByCooAbbreviationTest_true() {
        when(cooRepository.existsByAbbreviation(cooExpected1.getAbbreviation())).thenReturn(true);
        assertTrue(cooPlantService.existsByCooAbbreviation(cooExpected1.getAbbreviation()));
    }

    @Test
    public void existsByCooAbbreviationTest_false() {
        when(cooRepository.existsByAbbreviation(cooExpected1.getAbbreviation())).thenReturn(false);
        assertFalse(cooPlantService.existsByCooAbbreviation(cooExpected1.getAbbreviation()));
    }

    @Test
    public void getPlantsByCooTest() {
        when(plantRepository.findByCooOrderByName(cooExpected1)).thenReturn(List.of(plantExpected2, plantExpected1));
        List<Plant> actual = cooPlantService.getPlantsByCoo(cooExpected1);
        assertEquals(List.of(plantExpected2, plantExpected1), actual);
    }

    @Test
    public void getPlantsByCooTest_noPlantsFound() {
        when(plantRepository.findByCooOrderByName(cooExpected2)).thenReturn(List.of());
        List<Plant> actual = cooPlantService.getPlantsByCoo(cooExpected2);
        assertEquals(0, actual.size());
    }

    @Test
    public void getPlantsByCooTest_byName() {
        when(cooRepository.findByName(cooExpected1.getName())).thenReturn(Optional.of(cooExpected1));
        when(plantRepository.findByCooOrderByName(cooExpected1)).thenReturn(List.of(plantExpected2, plantExpected1));
        List<Plant> actual = cooPlantService.getPlantsByCoo(cooExpected1);
        assertEquals(List.of(plantExpected2, plantExpected1), actual);
    }

    @Test
    public void getPlantByCodeTest() {
        when(plantRepository.findByCode(plantExpected1.getCode()))
                .thenReturn(Optional.of(plantExpected1));
        Plant actual = cooPlantService.getPlantByCode(plantExpected1.getCode());
        assertEquals(plantExpected1, actual);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void getPlantByCodeTest_noCustomerFound() {
        when(plantRepository.findByCode(anyString())).thenReturn(Optional.empty());
        cooPlantService.getPlantByCode("9999ZZZ");
    }

    @Test
    public void getPlantByNameAndCooNameTest() {
        when(cooRepository.findByName(cooExpected1.getName())).thenReturn(Optional.of(cooExpected1));
        when(plantRepository.findByNameAndCoo(plantExpected1.getName(), cooExpected1))
                .thenReturn(Optional.of(plantExpected1));
        assertEquals(plantExpected1,
                cooPlantService.getPlantByNameAndCooName(plantExpected1.getName(), cooExpected1.getName()));
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void getPlantByNameAndCooNameTest_cooNotFound() {
        when(cooRepository.findByName(cooExpected1.getName())).thenReturn(Optional.empty());
        cooPlantService.getPlantByNameAndCooName(plantExpected1.getName(), cooExpected1.getName());
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void getPlantByNameAndCooNameTest_plantNotFound() {
        when(cooRepository.findByName(cooExpected1.getName())).thenReturn(Optional.of(cooExpected1));
        when(plantRepository.findByNameAndCoo(plantExpected1.getName(), cooExpected1))
                .thenReturn(Optional.empty());
        cooPlantService.getPlantByNameAndCooName(plantExpected1.getName(), cooExpected1.getName());
    }

    @Test
    public void postPlantTest() {
        when(plantRepository.save(plantExpected1)).thenReturn(plantExpected1);
        assertEquals(plantExpected1, cooPlantService.postPlant(plantExpected1));
        verify(plantRepository).save(plantExpected1);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postPlantTest_nameNull() {
        Plant plantNameNull = new Plant();
        plantNameNull.setName(null);
        plantNameNull.setCode("1234RE");
        plantNameNull.setCoo(cooExpected1);
        cooPlantService.postPlant(plantNameNull);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postPlantTest_nameBlank() {
        Plant plantNameBlank = new Plant();
        plantNameBlank.setName(" ");
        plantNameBlank.setCode("1234RE");
        plantNameBlank.setCoo(cooExpected1);
        cooPlantService.postPlant(plantNameBlank);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postPlantTest_codeNull() {
        Plant plantCodeNull = new Plant();
        plantCodeNull.setName("Prod Plant");
        plantCodeNull.setCode(null);
        plantCodeNull.setCoo(cooExpected1);
        cooPlantService.postPlant(plantCodeNull);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postPlantTest_codeBlank() {
        Plant plantCodeBlank = new Plant();
        plantCodeBlank.setName("Prod Plant");
        plantCodeBlank.setCode(" ");
        plantCodeBlank.setCoo(cooExpected1);
        cooPlantService.postPlant(plantCodeBlank);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postPlantTest_cooNull() {
        Plant plantCooNull = new Plant();
        plantCooNull.setName("Prod Plant");
        plantCooNull.setCode("1234RE");
        plantCooNull.setCoo(null);
        cooPlantService.postPlant(plantCooNull);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void postPlantTest_exists() {
        when(plantRepository.existsByNameAndCoo(plantExpected1.getName(), plantExpected1.getCoo())).thenReturn(true);
        cooPlantService.postPlant(plantExpected1);
    }

    @Test
    public void deletePlantTest() {
        when(plantRepository.existsByNameAndCoo(plantExpected1.getName(), plantExpected1.getCoo())).thenReturn(true);
        cooPlantService.deletePlant(plantExpected1);
        verify(plantRepository).delete(plantExpected1);
    }

    @Test(expected = EntityNotFoundException.class)
    public void deletePlantTest_plantNotFound() {
        when(plantRepository.existsByNameAndCoo(plantExpected1.getName(), plantExpected1.getCoo())).thenReturn(false);
        cooPlantService.deletePlant(plantExpected1);
    }

    @Test
    public void existsByPlantNameAndCooTest_true() {
        when(plantRepository.existsByNameAndCoo(plantExpected1.getName(), plantExpected1.getCoo())).thenReturn(true);
        assertTrue(cooPlantService.existsByPlantNameAndCoo(plantExpected1.getName(), plantExpected1.getCoo()));
    }

    @Test
    public void existsByPlantNameAndCooTest_false() {
        when(plantRepository.existsByNameAndCoo(plantExpected1.getName(), plantExpected1.getCoo())).thenReturn(false);
        assertFalse(cooPlantService.existsByPlantNameAndCoo(plantExpected1.getName(), plantExpected1.getCoo()));
    }
}