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
import com.felixseifert.coma.backend.model.SelectableValue;
import com.felixseifert.coma.backend.model.enums.ValueGroup;
import com.felixseifert.coma.backend.repos.SelectableValueRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SelectableValueServiceImplTest {

    @Autowired
    private SelectableValueServiceImpl selectableValueService;

    @MockBean
    private SelectableValueRepository selectableValueRepository;

    private static SelectableValue selectableValueExpected1;
    private static SelectableValue selectableValueExpected2;
    private static SelectableValue selectableValueExpected3;
    private static List<SelectableValue> listExpected;

    @BeforeClass
    public static void setup() {
        selectableValueExpected1 = new SelectableValue();
        selectableValueExpected1.setLabel("Interesting Label");
        selectableValueExpected1.setValueGroup(ValueGroup.LOCATION);
        selectableValueExpected2 = new SelectableValue();
        selectableValueExpected2.setLabel("Boring Label");
        selectableValueExpected2.setValueGroup(ValueGroup.LOCATION);
        selectableValueExpected3 = new SelectableValue();
        selectableValueExpected3.setLabel("SMS");
        selectableValueExpected3.setValueGroup(ValueGroup.DEPARTMENT);

        listExpected =
                List.of(selectableValueExpected1, selectableValueExpected2, selectableValueExpected3);
    }

    @Test
    public void getAllSelectableValuesTest() {
        when(selectableValueRepository.findAll()).thenReturn(listExpected);
        List<SelectableValue> listActual = selectableValueService.getAllSelectableValues();
        assertEquals(listExpected, listActual);
    }

    @Test
    public void getSelectableValuesByValueGroupTest() {
        when(selectableValueRepository.findAllByValueGroupOrderByLabelAsc(ValueGroup.LOCATION))
                .thenReturn(List.of(selectableValueExpected2, selectableValueExpected1));
        List<SelectableValue> listActual =
                selectableValueService.getSelectableValuesByValueGroup(ValueGroup.LOCATION);
        assertEquals(List.of(selectableValueExpected2, selectableValueExpected1), listActual);
    }

    @Test
    public void postSelectableValueTest() {
        when(selectableValueRepository.save(selectableValueExpected1)).thenReturn(selectableValueExpected1);
        assertEquals(selectableValueExpected1, selectableValueService.postSelectableValue(selectableValueExpected1));
        verify(selectableValueRepository).save(selectableValueExpected1);
    }

    @Test
    public void postSelectableValueTest_string() {
        String labelToSave = "New Value";
        ValueGroup valueGroupToSave = ValueGroup.LOCATION;
        SelectableValue selectableValueToSave = new SelectableValue();
        selectableValueToSave.setId(10);
        selectableValueToSave.setLabel(labelToSave);
        selectableValueToSave.setValueGroup(valueGroupToSave);
        when(selectableValueRepository.save(any(SelectableValue.class))).thenReturn(selectableValueToSave);
        SelectableValue actual = selectableValueService.postSelectableValue(labelToSave, valueGroupToSave);
        verify(selectableValueRepository).save(any(SelectableValue.class));
        assertEquals(selectableValueToSave, actual);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postSelectableValueTest_labelNull() {
        SelectableValue selectableValueEmptyLabel = new SelectableValue();
        selectableValueEmptyLabel.setLabel(null);
        selectableValueEmptyLabel.setValueGroup(ValueGroup.DEPARTMENT);
        selectableValueService.postSelectableValue(selectableValueEmptyLabel);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postSelectableValueTest_labelBlank() {
        SelectableValue selectableValueBlankLabel = new SelectableValue();
        selectableValueBlankLabel.setLabel(" ");
        selectableValueBlankLabel.setValueGroup(ValueGroup.DEPARTMENT);
        selectableValueService.postSelectableValue(selectableValueBlankLabel);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postSelectableValueTest_groupNull() {
        SelectableValue selectableValueEmptyGroup = new SelectableValue();
        selectableValueEmptyGroup.setLabel("Test");
        selectableValueEmptyGroup.setValueGroup(null);
        selectableValueService.postSelectableValue(selectableValueEmptyGroup);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void postSelectableValueTest_labelGroupCombinationExists() {
        when(selectableValueRepository.existsByLabelAndValueGroup(anyString(), any(ValueGroup.class)))
                .thenReturn(true);
        selectableValueService.postSelectableValue(selectableValueExpected1);
    }

    @Test
    public void deleteSelectableValueTest() {
        when(selectableValueRepository.existsById(any())).thenReturn(true);
        selectableValueService.deleteSelectableValue(selectableValueExpected1);
        verify(selectableValueRepository).delete(selectableValueExpected1);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void deleteSelectableValueTest_idDoesNotExists() {
        when(selectableValueRepository.existsById(any())).thenReturn(false);
        selectableValueService.deleteSelectableValue(selectableValueExpected1);
    }

    @Test
    public void existsTest_true() {
        when(selectableValueRepository.existsByLabelAndValueGroup(anyString(), any(ValueGroup.class))).thenReturn(true);
        assertTrue(selectableValueService.exists(selectableValueExpected1.getLabel(),
                selectableValueExpected1.getValueGroup()));
    }

    @Test
    public void existsTest_false() {
        when(selectableValueRepository.existsByLabelAndValueGroup(anyString(), any(ValueGroup.class))).thenReturn(false);
        assertFalse(selectableValueService.exists(selectableValueExpected1.getLabel(),
                selectableValueExpected1.getValueGroup()));
    }
}
