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
import com.felixseifert.coma.backend.model.ProductDescription;
import com.felixseifert.coma.backend.repos.ProductDescriptionRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ProductDescriptionServiceImplTest {

    @Autowired
    private ProductDescriptionServiceImpl productDescriptionService;

    @MockBean
    private ProductDescriptionRepository productDescriptionRepository;

    private static ProductDescription productDescriptionExpected1;
    private static ProductDescription productDescriptionExpected2;

    @BeforeClass
    public static void setup() {
        productDescriptionExpected1 = new ProductDescription();
        productDescriptionExpected1.setCode("123456");
        productDescriptionExpected1.setDescription("Funny Product");
        productDescriptionExpected2 = new ProductDescription();
        productDescriptionExpected2.setCode("654321");
        productDescriptionExpected2.setDescription("New Product");
    }

    @Test
    public void getAllProductDescriptionsTest() {
        when(productDescriptionRepository.findAllByOrderByDescription())
                .thenReturn(List.of(productDescriptionExpected1, productDescriptionExpected2));
        List<ProductDescription> actual = productDescriptionService.getAllProductDescriptions();
        assertEquals(List.of(productDescriptionExpected1, productDescriptionExpected2), actual);
    }

    @Test
    public void getAllProductDescriptionsTest_noDescriptionsFound() {
        when(productDescriptionRepository.findAllByOrderByDescription()).thenReturn(List.of());
        List<ProductDescription> actual = productDescriptionService.getAllProductDescriptions();
        assertEquals(0, actual.size());
    }

    @Test
    public void postProductDescriptionTest() {
        when(productDescriptionRepository.save(productDescriptionExpected1)).thenReturn(productDescriptionExpected1);
        assertEquals(productDescriptionExpected1,
                productDescriptionService.postProductDescription(productDescriptionExpected1));
        verify(productDescriptionRepository).save(productDescriptionExpected1);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void postProductDescriptionTest_codeExists() {
        when(productDescriptionRepository.existsByCode(productDescriptionExpected1.getCode())).thenReturn(true);
        productDescriptionService.postProductDescription(productDescriptionExpected1);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void postProductDescriptionTest_descriptionTextExists() {
        when(productDescriptionRepository.existsByDescription(productDescriptionExpected1.getDescription()))
                .thenReturn(true);
        productDescriptionService.postProductDescription(productDescriptionExpected1);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postProductDescriptionTest_codeNull() {
        ProductDescription productDescriptionEmptyCode = new ProductDescription();
        productDescriptionEmptyCode.setCode(null);
        productDescriptionEmptyCode.setDescription("Description to Save");
        productDescriptionService.postProductDescription(productDescriptionEmptyCode);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postProductDescriptionTest_descriptionNull() {
        ProductDescription productDescriptionEmptyDescription = new ProductDescription();
        productDescriptionEmptyDescription.setCode("767676");
        productDescriptionEmptyDescription.setDescription(null);
        productDescriptionService.postProductDescription(productDescriptionEmptyDescription);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postProductDescriptionTest_descriptionBlank() {
        ProductDescription productDescriptionBlankDescription = new ProductDescription();
        productDescriptionBlankDescription.setCode("767676");
        productDescriptionBlankDescription.setDescription(" ");
        productDescriptionService.postProductDescription(productDescriptionBlankDescription);
    }

    @Test()
    public void deleteProductDescriptionTest() {
        when(productDescriptionRepository.existsByCode(productDescriptionExpected1.getCode())).thenReturn(true);
        productDescriptionService.deleteProductDescription(productDescriptionExpected1);
        verify(productDescriptionRepository).delete(productDescriptionExpected1);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void deleteProductDescriptionTest_notFound() {
        when(productDescriptionRepository.existsByCode(productDescriptionExpected1.getCode())).thenReturn(false);
        productDescriptionService.deleteProductDescription(productDescriptionExpected1);
    }

    @Test
    public void existsByCodeTest_true() {
        when(productDescriptionRepository.existsByCode(productDescriptionExpected1.getCode())).thenReturn(true);
        assertTrue(productDescriptionService.existsByCode(productDescriptionExpected1.getCode()));
    }

    @Test
    public void existsByCodeTest_false() {
        when(productDescriptionRepository.existsByCode(productDescriptionExpected1.getCode())).thenReturn(false);
        assertFalse(productDescriptionService.existsByCode(productDescriptionExpected1.getCode()));
    }

    @Test
    public void existsByDescriptionTest_true() {
        when(productDescriptionRepository.existsByDescription(productDescriptionExpected1.getDescription()))
                .thenReturn(true);
        assertTrue(productDescriptionService.existsByDescription(productDescriptionExpected1.getDescription()));
    }

    @Test
    public void existsByDescriptionTest_false() {
        when(productDescriptionRepository.existsByDescription(productDescriptionExpected1.getDescription()))
                .thenReturn(false);
        assertFalse(productDescriptionService.existsByDescription(productDescriptionExpected1.getDescription()));
    }
}
