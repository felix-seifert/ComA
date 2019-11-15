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
import com.felixseifert.coma.backend.exceptions.ErrorMessages;
import com.felixseifert.coma.backend.model.Coo;
import com.felixseifert.coma.backend.model.Plant;
import com.felixseifert.coma.backend.repos.CooRepository;
import com.felixseifert.coma.backend.repos.PlantRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CooPlantServiceImpl implements CooPlantService {

    @Autowired
    private CooRepository cooRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Override
    public List<Coo> getAllCoos() {
        log.debug("Get all CoOs");
        return cooRepository.findAllByOrderByAbbreviationAsc();
    }

    @Override
    public Coo postCoo(Coo coo) throws BlankValueNotAllowedException, EntityAlreadyExistsException {
        if(StringUtils.isBlank(coo.getName())) {
            throw new BlankValueNotAllowedException(ErrorMessages.NAME_NOT_SPECIFIED);
        }
        if(StringUtils.isBlank(coo.getAbbreviation())) {
            throw new BlankValueNotAllowedException(ErrorMessages.ABBREVIATION_NOT_SPECIFIED);
        }

        boolean exists = cooRepository.existsByName(coo.getName());
        if(exists) {
            throw new EntityAlreadyExistsException(ErrorMessages.COO_ALREADY_EXISTS);
        }

        log.info("Create new CoO: {}", coo);
        return cooRepository.save(coo);
    }

    @Override
    public void deleteCoo(Coo coo) throws EntityIDNotFoundException {
        if(!cooRepository.existsByName(coo.getName())) {
            throw new EntityIDNotFoundException(ErrorMessages.COO_NOT_FOUND);
        }
        log.info("Delete CoO {}", coo);
        cooRepository.delete(coo);
    }

    @Override
    public boolean existsByCooName(String name) {
        boolean exists = cooRepository.existsByName(name);
        log.debug("CoO with name {} is persisted: {}", name, exists);
        return exists;
    }

    @Override
    public boolean existsByCooAbbreviation(String abbreviation) {
        boolean exists = cooRepository.existsByAbbreviation(abbreviation);
        log.debug("CoO with abbreviation {} is persisted: {}", abbreviation, exists);
        return exists;
    }

    @Override
    public List<Plant> getPlantsByCoo(Coo coo) {
        log.debug("Get all Plants with CoO {}", coo);
        return plantRepository.findByCooOrderByName(coo);
    }

    @Override
    public List<Plant> getPlantsByCoo(String cooName) {
        log.debug("Get all Plants with CoO name {}", cooName);
        Optional<Coo> cooOptional = cooRepository.findByName(cooName);
        return cooOptional.isPresent() ? plantRepository.findByCooOrderByName(cooOptional.get()) : List.of();
    }

    @Override
    public Plant getPlantByCode(String code) throws EntityIDNotFoundException {
        log.debug("Get Plant with code={}", code);
        Optional<Plant> plant = plantRepository.findByCode(code);
        if(!plant.isPresent()) {
            throw new EntityIDNotFoundException(ErrorMessages.PLANT_CODE_NOT_FOUND);
        }
        return plant.get();
    }

    @Override
    public Plant getPlantByNameAndCooName(String name, String cooName) throws EntityIDNotFoundException {
        log.debug("Get Plant with name={}", name);
        Optional<Coo> cooOptional = cooRepository.findByName(cooName);
        if(!cooOptional.isPresent()) {
            throw new EntityIDNotFoundException(ErrorMessages.COO_NOT_FOUND);
        }
        Optional<Plant> plant = plantRepository.findByNameAndCoo(name, cooOptional.get());
        if(!plant.isPresent()) {
            throw new EntityIDNotFoundException(ErrorMessages.PLANT_NOT_FOUND);
        }
        return plant.get();
    }

    @Override
    public Plant postPlant(Plant plant) throws BlankValueNotAllowedException, EntityAlreadyExistsException {
        if(StringUtils.isBlank(plant.getName())) {
            throw new BlankValueNotAllowedException(ErrorMessages.NAME_NOT_SPECIFIED);
        }
        if(StringUtils.isBlank(plant.getCode())) {
            throw new BlankValueNotAllowedException(ErrorMessages.CODE_NOT_SPECIFIED);
        }
        if(plant.getCoo() == null) {
            throw new BlankValueNotAllowedException(ErrorMessages.COO_NOT_SPECIFIED);
        }

        boolean exists = plantRepository.existsByNameAndCoo(plant.getName(), plant.getCoo());
        if(exists) {
            throw new EntityAlreadyExistsException(ErrorMessages.PLANT_ALREADY_EXISTS);
        }

        log.info("Create new Plant: {}", plant);
        return plantRepository.save(plant);
    }

    @Override
    public void deletePlant(Plant plant) throws EntityIDNotFoundException {
        if(!plantRepository.existsByNameAndCoo(plant.getName(), plant.getCoo())) {
            throw new EntityIDNotFoundException(ErrorMessages.PLANT_NOT_FOUND);
        }

        log.info("Delete Plant {}", plant);
        plantRepository.delete(plant);
    }

    @Override
    public boolean existsByPlantNameAndCoo(String name, Coo coo) {
        boolean exists = plantRepository.existsByNameAndCoo(name, coo);
        log.debug("Plant with name {} and CoO {} is persisted: {}", name, coo.getName(), exists);
        return exists;
    }

    @Override
    public boolean existsByPlantCode(String code) {
        boolean exists = plantRepository.existsByCode(code);
        log.debug("Plant with code {} is persisted: {}", code, exists);
        return exists;
    }
}
