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

import com.felixseifert.coma.backend.model.SelectableValue;
import com.felixseifert.coma.backend.model.enums.ValueGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@DataJpaTest
@RunWith(SpringRunner.class)
public class SelectableValueRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private SelectableValueRepository selectableValueRepository;

    private SelectableValue selectableValueExpected1;
    private SelectableValue selectableValueExpected2;
    private SelectableValue selectableValueExpected3;

    @Before
    public void setDatabase() {
        selectableValueExpected1 = new SelectableValue();
        selectableValueExpected1.setLabel("Interesting Label");
        selectableValueExpected1.setValueGroup(ValueGroup.LOCATION);
        selectableValueExpected2 = new SelectableValue();
        selectableValueExpected2.setLabel("Boring Label");
        selectableValueExpected2.setValueGroup(ValueGroup.LOCATION);
        selectableValueExpected3 = new SelectableValue();
        selectableValueExpected3.setLabel("SMS");
        selectableValueExpected3.setValueGroup(ValueGroup.DEPARTMENT);

        testEntityManager.persist(selectableValueExpected1);
        testEntityManager.persist(selectableValueExpected2);
        testEntityManager.persist(selectableValueExpected3);
        testEntityManager.flush();
    }

    @Test
    public void findAllTest() {
        List<SelectableValue> listActual = selectableValueRepository.findAll();
        assertEquals(List.of(selectableValueExpected1, selectableValueExpected2, selectableValueExpected3), listActual);
    }

    @Test
    public void findAllByValueGroupTest() {
        List<SelectableValue> listActual =
                selectableValueRepository.findAllByValueGroupOrderByLabelAsc(ValueGroup.LOCATION);
        assertEquals(List.of(selectableValueExpected2, selectableValueExpected1), listActual);
    }

    @Test
    public void saveTest() {
        SelectableValue selectableValueToSave = new SelectableValue();
        selectableValueToSave.setLabel("RBR11");
        selectableValueToSave.setValueGroup(ValueGroup.DEPARTMENT);

        SelectableValue selectableValueActual = selectableValueRepository.save(selectableValueToSave);
        boolean exists = selectableValueRepository.existsById(selectableValueToSave.getId());
        List<SelectableValue> listActual =
                selectableValueRepository.findAllByValueGroupOrderByLabelAsc(ValueGroup.DEPARTMENT);

        assertEquals(selectableValueToSave, selectableValueActual);
        assertTrue(exists);
        assertTrue(listActual.containsAll(List.of(selectableValueExpected3, selectableValueToSave)));
    }

    @Test
    public void deleteTest() {
        selectableValueRepository.delete(selectableValueExpected1);

        boolean exists = selectableValueRepository.existsById(selectableValueExpected1.getId());
        List<SelectableValue> listActual = selectableValueRepository.findAll();

        assertFalse(exists);
        assertEquals(2, listActual.size());
    }

    @Test
    public void existsByLabelAndValueGroupTest() {
        assertTrue(selectableValueRepository.existsByLabelAndValueGroup(
                selectableValueExpected1.getLabel(), selectableValueExpected1.getValueGroup()));
        assertFalse(selectableValueRepository.existsByLabelAndValueGroup(
                selectableValueExpected1.getLabel(), selectableValueExpected3.getValueGroup()));
    }

    @Test
    public void existsByIdTest() {
        assertTrue(selectableValueRepository.existsById(selectableValueExpected1.getId()));
        assertFalse(selectableValueRepository.existsById(999));
    }
}
