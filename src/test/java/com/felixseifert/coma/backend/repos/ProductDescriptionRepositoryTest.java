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

import com.felixseifert.coma.backend.model.ProductDescription;
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
public class ProductDescriptionRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private ProductDescriptionRepository productDescriptionRepository;

    private ProductDescription productDescriptionExpected1;
    private ProductDescription productDescriptionExpected2;

    @Before
    public void setDatabase() {
        productDescriptionExpected1 = new ProductDescription();
        productDescriptionExpected1.setCode("123456");
        productDescriptionExpected1.setDescription("Funny Product");
        productDescriptionExpected2 = new ProductDescription();
        productDescriptionExpected2.setCode("654321");
        productDescriptionExpected2.setDescription("New Product");

        testEntityManager.persist(productDescriptionExpected1);
        testEntityManager.persist(productDescriptionExpected2);
        testEntityManager.flush();
    }

    @Test
    public void findAllTest() {
        List<ProductDescription> actualList = productDescriptionRepository.findAllByOrderByDescription();
        assertEquals(List.of(productDescriptionExpected1, productDescriptionExpected2), actualList);
    }

    @Test
    public void saveTest() {
        ProductDescription productDescriptionToSave = new ProductDescription();
        productDescriptionToSave.setCode("767676");
        productDescriptionToSave.setDescription("Description to Save");

        productDescriptionRepository.save(productDescriptionToSave);
        boolean exists = productDescriptionRepository.existsByCode(productDescriptionToSave.getCode());
        List<ProductDescription>actualList = productDescriptionRepository.findAllByOrderByDescription();

        assertTrue(exists);
        assertEquals(3, actualList.size());
    }

    @Test
    public void deleteTest() {
        productDescriptionRepository.delete(productDescriptionExpected1);

        boolean exists = productDescriptionRepository.existsByCode(productDescriptionExpected1.getCode());
        List<ProductDescription>actualList = productDescriptionRepository.findAllByOrderByDescription();

        assertFalse(exists);
        assertEquals(1, actualList.size());
    }

    @Test
    public void existsByCodeTest() {
        assertTrue(productDescriptionRepository.existsByCode(productDescriptionExpected1.getCode()));
        assertFalse(productDescriptionRepository.existsByCode("999999"));
    }

    @Test
    public void existsByDescription() {
        assertTrue(productDescriptionRepository.existsByDescription(productDescriptionExpected1.getDescription()));
        assertFalse(productDescriptionRepository.existsByDescription("No Description"));
    }
}
