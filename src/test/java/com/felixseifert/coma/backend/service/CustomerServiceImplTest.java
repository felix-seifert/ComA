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
import com.felixseifert.coma.backend.exceptions.DependencyException;
import com.felixseifert.coma.backend.exceptions.EntityAlreadyExistsException;
import com.felixseifert.coma.backend.exceptions.EntityIDNotFoundException;
import com.felixseifert.coma.backend.model.enums.Currency;
import com.felixseifert.coma.backend.model.Customer;
import com.felixseifert.coma.backend.model.PartNumberObject;
import com.felixseifert.coma.backend.repos.CustomerRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CustomerServiceImplTest {

    @Autowired
    private CustomerServiceImpl customerServiceImpl;

    @MockBean
    private CustomerRepository customerRepository;

    private static Customer customerExpected1;
    private static Customer customerExpected2;
    private static Customer customerExpected3;
    private static List<Customer> listExpected;

    @BeforeClass
    public static void setup() {
        customerExpected1 = new Customer();
        customerExpected1.setCode("12345678");
        customerExpected1.setName("Important Customer");
        customerExpected1.setDeliveryLocation("Kyoto, Japan");
        customerExpected1.setCurrency(Currency.JPY.toString());
        customerExpected2 = new Customer();
        customerExpected2.setCode("67890123");
        customerExpected2.setName("Relevant Customer");
        customerExpected2.setDeliveryLocation("Nagano, Japan");
        customerExpected2.setCurrency(Currency.EUR.toString());
        customerExpected3 = new Customer();
        customerExpected3.setCode("22222123");
        customerExpected3.setName("Reimbursement Customer");
        customerExpected3.setDeliveryLocation("Tokyo, Japan");
        customerExpected3.setCurrency(Currency.EUR.toString());
        listExpected = List.of(customerExpected1, customerExpected2, customerExpected3);
    }

    @Test
    public void getAllCustomersTest() {
        when(customerRepository.findAll()).thenReturn(listExpected);
        List<Customer> actual = customerServiceImpl.getAllCustomers();
        assertEquals(listExpected, actual);
    }

    @Test
    public void getAllCustomersTest_noCustomersFound() {
        when(customerRepository.findAll()).thenReturn(List.of());
        List<Customer> actual = customerServiceImpl.getAllCustomers();
        assertEquals(List.of(), actual);
    }

    @Test
    public void getCustomerByCodeTest() {
        when(customerRepository.findByCode(customerExpected1.getCode()))
                .thenReturn(Optional.of(customerExpected1));
        Customer actual = customerServiceImpl.getCustomerByCode(customerExpected1.getCode());
        assertEquals(customerExpected1, actual);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void getCustomerByCode_noCustomerFound() {
        when(customerRepository.findByCode(anyString())).thenReturn(Optional.empty());
        customerServiceImpl.getCustomerByCode("12345678");
    }

    @Test
    public void getCustomersByNameStartsWithIgnoreCaseTest() {
        when(customerRepository.findByNameStartsWithIgnoreCase("re"))
                .thenReturn(List.of(customerExpected2, customerExpected3));
        List<Customer> actual =
                customerServiceImpl.getCustomersByNameStartsWithIgnoreCase("re");
        assertEquals(List.of(customerExpected2, customerExpected3), actual);
    }

    @Test
    public void getCustomersByNameStartsWithIgnoreCaseTest_noEmployeeFound() {
        when(customerRepository.findByNameStartsWithIgnoreCase(anyString())).thenReturn(List.of());
        List<Customer> actual = customerServiceImpl.getCustomersByNameStartsWithIgnoreCase("zz");
        assertEquals(List.of(), actual);
    }

    @Test
    public void postCustomerTest() {
        when(customerRepository.save(customerExpected1)).thenReturn(customerExpected1);
        assertEquals(customerExpected1, customerServiceImpl.postCustomer(customerExpected1));
        verify(customerRepository).save(customerExpected1);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postCustomerTest_nameNull() {
        Customer customerEmptyName = new Customer();
        customerEmptyName.setCode("23465");
        customerEmptyName.setName(null);
        customerEmptyName.setDeliveryLocation("Nagoya, Japan");
        customerEmptyName.setCurrency(Currency.USD.toString());
        customerServiceImpl.postCustomer(customerEmptyName);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postCustomerTest_nameBlank() {
        Customer customerBlankName = new Customer();
        customerBlankName.setCode("23465");
        customerBlankName.setName(" ");
        customerBlankName.setDeliveryLocation("Nagoya, Japan");
        customerBlankName.setCurrency(Currency.USD.toString());
        customerServiceImpl.postCustomer(customerBlankName);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postCustomerTest_locationNull() {
        Customer customerEmptyLocation = new Customer();
        customerEmptyLocation.setCode("23465123");
        customerEmptyLocation.setName("Customer to Save");
        customerEmptyLocation.setDeliveryLocation(null);
        customerEmptyLocation.setCurrency(Currency.USD.toString());
        customerServiceImpl.postCustomer(customerEmptyLocation);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postCustomerTest_locationBlank() {
        Customer customerBlankLocation = new Customer();
        customerBlankLocation.setCode("23465123");
        customerBlankLocation.setName("Customer to Save");
        customerBlankLocation.setDeliveryLocation(" ");
        customerBlankLocation.setCurrency(Currency.USD.toString());
        customerServiceImpl.postCustomer(customerBlankLocation);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void postCustomerTest_codeNull() {
        Customer customerEmptyCode = new Customer();
        customerEmptyCode.setCode(null);
        customerEmptyCode.setName("Customer to Save");
        customerEmptyCode.setDeliveryLocation("Nagoya, Japan");
        customerEmptyCode.setCurrency(Currency.USD.toString());
        customerServiceImpl.postCustomer(customerEmptyCode);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void postCustomerTest_codeExists() {
        when(customerRepository.existsByCode(customerExpected1.getCode())).thenReturn(true);
        customerServiceImpl.postCustomer(customerExpected1);
    }

    @Test
    public void putCustomerTest() {
        when(customerRepository.existsByCode(customerExpected1.getCode())).thenReturn(true);
        when(customerRepository.save(customerExpected1)).thenReturn(customerExpected1);
        assertEquals(customerExpected1, customerServiceImpl.putCustomer(customerExpected1));
        verify(customerRepository).save(customerExpected1);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putCustomerTest_nameNull() {
        Customer customerEmptyName = new Customer();
        customerEmptyName.setCode("23465123");
        customerEmptyName.setName(null);
        customerEmptyName.setDeliveryLocation("Nagoya, Japan");
        customerEmptyName.setCurrency(Currency.USD.toString());
        customerServiceImpl.putCustomer(customerEmptyName);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putCustomerTest_nameBlank() {
        Customer customerBlankName = new Customer();
        customerBlankName.setCode("23465123");
        customerBlankName.setName(" ");
        customerBlankName.setDeliveryLocation("Nagoya, Japan");
        customerBlankName.setCurrency(Currency.USD.toString());
        customerServiceImpl.putCustomer(customerBlankName);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putCustomerTest_locationNull() {
        Customer customerEmptyLocation = new Customer();
        customerEmptyLocation.setCode("23465123");
        customerEmptyLocation.setName("Customer to Save");
        customerEmptyLocation.setDeliveryLocation(null);
        customerEmptyLocation.setCurrency(Currency.USD.toString());
        customerServiceImpl.putCustomer(customerEmptyLocation);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putCustomerTest_locationBlank() {
        Customer customerBlankLocation = new Customer();
        customerBlankLocation.setCode("23465123");
        customerBlankLocation.setName("Customer to Save");
        customerBlankLocation.setDeliveryLocation(" ");
        customerBlankLocation.setCurrency(Currency.USD.toString());
        customerServiceImpl.putCustomer(customerBlankLocation);
    }

    @Test(expected = BlankValueNotAllowedException.class)
    public void putCustomerTest_codeNull() {
        Customer customerEmptyCode = new Customer();
        customerEmptyCode.setCode(null);
        customerEmptyCode.setName("Customer to Save");
        customerEmptyCode.setDeliveryLocation("Nagoya, Japan");
        customerEmptyCode.setCurrency(Currency.USD.toString());
        customerServiceImpl.putCustomer(customerEmptyCode);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void putCustomerTest_codeDoesNotExist() {
        when(customerRepository.existsByCode(customerExpected1.getCode())).thenReturn(false);
        customerServiceImpl.putCustomer(customerExpected1);
    }

    @Test
    public void deleteCustomerTest() throws DependencyException {
        when(customerRepository.existsByCode(customerExpected1.getCode())).thenReturn(true);
        customerServiceImpl.deleteCustomer(customerExpected1);
        verify(customerRepository).delete(customerExpected1);
    }

    @Test(expected = EntityIDNotFoundException.class)
    public void deleteCustomerTest_customerDoesNotExists() throws DependencyException {
        when(customerRepository.existsByCode(customerExpected1.getCode())).thenReturn(false);
        customerServiceImpl.deleteCustomer(customerExpected1);
    }

    @Test(expected = DependencyException.class)
    public void deleteCustomerTest_hasDependencies() throws DependencyException {
        PartNumberObject partNumberObject = new PartNumberObject();
        customerExpected1.setPartNumberObjects(Set.of(partNumberObject));
        when(customerRepository.existsByCode(customerExpected1.getCode())).thenReturn(true);
        customerServiceImpl.deleteCustomer(customerExpected1);
    }

    @Test
    public void existsTest_true() {
        when(customerRepository.existsByCode(customerExpected1.getCode())).thenReturn(true);
        assertTrue(customerServiceImpl.exists(customerExpected1.getCode()));
    }

    @Test
    public void existsTest_false() {
        when(customerRepository.existsByCode(customerExpected1.getCode())).thenReturn(false);
        assertFalse(customerServiceImpl.exists(customerExpected1.getCode()));
    }

    @Test(expected = NumberFormatException.class)
    public void isCodeNumberCorrectTest() {
        assertTrue(customerServiceImpl.isCodeNumberCorrect("12345678"));
        assertTrue(customerServiceImpl.isCodeNumberCorrect("1234567890"));
        assertFalse(customerServiceImpl.isCodeNumberCorrect("123456789a"));
    }
}
