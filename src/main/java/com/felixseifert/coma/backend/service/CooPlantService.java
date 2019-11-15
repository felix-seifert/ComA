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

import com.felixseifert.coma.backend.model.Coo;
import com.felixseifert.coma.backend.model.Plant;

import java.util.List;

public interface CooPlantService {

    List<Coo> getAllCoos();

    Coo postCoo(Coo coo);

    void deleteCoo(Coo coo);

    boolean existsByCooName(String name);

    boolean existsByCooAbbreviation(String abbreviation);

    List<Plant> getPlantsByCoo(Coo coo);

    List<Plant> getPlantsByCoo(String cooName);

    Plant getPlantByCode(String code);

    Plant getPlantByNameAndCooName(String name, String cooName);

    Plant postPlant(Plant plant);

    void deletePlant(Plant plant);

    boolean existsByPlantNameAndCoo(String name, Coo coo);

    boolean existsByPlantCode(String code);
}
