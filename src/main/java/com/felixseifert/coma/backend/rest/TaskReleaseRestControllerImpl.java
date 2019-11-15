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

package com.felixseifert.coma.backend.rest;

import com.felixseifert.coma.backend.model.dto.TaskReleaseDTO;
import com.felixseifert.coma.backend.repos.TaskReleaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("rest/v1/release-tasks")
@Slf4j
public class TaskReleaseRestControllerImpl implements TaskReleaseRestController {

    @Autowired
    private TaskReleaseRepository taskReleaseRepository;

    @Override
    @GetMapping("/unfinished-with-responsible-employee")
    public ResponseEntity<List<TaskReleaseDTO>> getUnfinishedWithResponsibleEmployee() {

        List<TaskReleaseDTO> result = taskReleaseRepository.findAllUnfinishedWithResponsibleEmployee();

        if(result.isEmpty()) {
            log.info("No relevant TaskReleases found");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        log.info("Return List<TaskReleaseDTO> with {} entries", result.size());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    @GetMapping("/unfinished-by-email-address")
    public ResponseEntity<List<TaskReleaseDTO>> getUnfinishedByEmployeesEmailAddress(
            @RequestParam(name = "email-address", defaultValue = "") String emailAddress) {

        List<TaskReleaseDTO> result = taskReleaseRepository.findAllUnfinishedByEmployeeEmailAddress(emailAddress);

        if(result.isEmpty()) {
            log.info("No relevant TaskReleases for {} found", emailAddress);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        log.info("Return List<TaskReleaseDTO> with {} entries for \"{}\"", result.size(),
                StringUtils.isNotBlank(emailAddress) ? emailAddress : "null");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    @GetMapping("/responsible-employee-email-addresses")
    public ResponseEntity<List<String>> getResponsibleEmployeeEmailAddresses() {

        List<String> result = taskReleaseRepository.findAllResponsibleEmployeeEmailAddresses();

        if(result.isEmpty()) {
            log.info("No relevant email addresses found");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        log.info("Return List<String> with {} email addresses", result.size());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
