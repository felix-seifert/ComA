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

import com.felixseifert.coma.backend.model.BusinessUnit;
import com.felixseifert.coma.backend.model.Employee;
import com.felixseifert.coma.backend.model.PartNumberObject;
import com.felixseifert.coma.backend.model.enums.Role;
import com.felixseifert.coma.backend.model.dto.PartNumberObjectGridDTO;

import java.util.List;
import java.util.Optional;

public interface PartNumberObjectRepositoryCustom {

    Optional<PartNumberObject> findById(Integer id);

    List<PartNumberObjectGridDTO> findAllPnGridDtos(int limit, int offset, boolean descending, String sortProperty);

    List<PartNumberObjectGridDTO> findByPnStartsWithIgnoreCase(String pn, int limit, int offset,
                                                               boolean descending, String sortProperty);

    void removePNsForEmployeeByRole(Employee employee, Role role);

    void removePNsForBusinessUnit(BusinessUnit businessUnit);

    Integer countPartNumberObjects(Employee employee);

    Integer countPartNumberObjects(Employee employee, Role role);

    Integer countPartNumberObjects(BusinessUnit businessUnit);

    boolean existsByCompletePn(String completePn);
}
