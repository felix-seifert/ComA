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
import com.felixseifert.coma.backend.model.SelectableValue;
import com.felixseifert.coma.backend.model.enums.ValueGroup;
import com.felixseifert.coma.backend.repos.SelectableValueRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SelectableValueServiceImpl implements SelectableValueService {

    @Autowired
    private SelectableValueRepository selectableValueRepository;

    @Override
    public List<SelectableValue> getAllSelectableValues() {
        log.debug("Get all SelectableValues");
        return selectableValueRepository.findAll();
    }

    @Override
    public List<SelectableValue> getSelectableValuesByValueGroup(ValueGroup valueGroup) {
        log.debug("Get list of SelectableValues of the group {}", valueGroup);
        return selectableValueRepository.findAllByValueGroupOrderByLabelAsc(valueGroup);
    }

    @Override
    public SelectableValue postSelectableValue(SelectableValue selectableValue) {
        log.info("Create SelectableValue: {}", selectableValue);

        if(StringUtils.isBlank(selectableValue.getLabel())) {
            throw new BlankValueNotAllowedException(ErrorMessages.LABEL_NOT_SPECIFIED);
        }
        if(selectableValue.getValueGroup() == null) {
            throw new BlankValueNotAllowedException(ErrorMessages.VALUE_GROUP_NOT_SPECIFIED);
        }
        if (selectableValueRepository.existsByLabelAndValueGroup(selectableValue.getLabel(),
                selectableValue.getValueGroup())) {
            throw new EntityAlreadyExistsException(ErrorMessages.LABEL_VALUE_GROUP_ALREADY_EXISTS);
        }

        return selectableValueRepository.save(selectableValue);
    }

    @Override
    public SelectableValue postSelectableValue(String label, ValueGroup valueGroup) {
        SelectableValue selectableValue = new SelectableValue();
        selectableValue.setLabel(label);
        selectableValue.setValueGroup(valueGroup);
        return postSelectableValue(selectableValue);
    }

    @Override
    public void deleteSelectableValue(SelectableValue selectableValue) {
        log.info("Delete SelectableValue {}", selectableValue);

        if(!selectableValueRepository.existsById(selectableValue.getId())) {
            throw new EntityIDNotFoundException(ErrorMessages.SELECTABLE_VALUE_NOT_FOUND);
        }

        selectableValueRepository.delete(selectableValue);
    }

    @Override
    public boolean exists(String label, ValueGroup valueGroup) {
        boolean exists = selectableValueRepository.existsByLabelAndValueGroup(label, valueGroup);
        log.debug("Combination {} and {} exists: {}", label, valueGroup.getFieldDescription(), exists);
        return exists;
    }


}
