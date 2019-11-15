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

import com.felixseifert.coma.backend.model.Customer;
import com.felixseifert.coma.backend.repos.CustomerRepository;
import com.felixseifert.coma.backend.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public List<Customer> getAllCustomers() {
        log.debug("Get all Customers");
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomerByCode(String code) throws EntityIDNotFoundException {
        Optional<Customer> customer =
                isCodeNumberCorrect(code) ? customerRepository.findByCode(code) : Optional.empty();
        if(!customer.isPresent()) {
            throw new EntityIDNotFoundException(ErrorMessages.CUSTOMER_NOT_FOUND);
        }
        log.debug("Get Customer with code={}", code);
        return customer.get();
    }

    @Override
    public List<Customer> getCustomersByNameStartsWithIgnoreCase(String filterText) {
        log.debug("Get all Customers whos name start with {}", filterText);
        return customerRepository.findByNameStartsWithIgnoreCase(filterText);
    }

    @Override
    public Customer postCustomer(Customer customer) throws BlankValueNotAllowedException, EntityAlreadyExistsException {

        checkForBlankFields(customer);

        boolean exists = customerRepository.existsByCode(customer.getCode());
        if(exists) {
            throw new EntityAlreadyExistsException(ErrorMessages.CUSTOMER_ALREADY_EXISTS);
        }

        log.info("Create Customer: {}", customer);
        return customerRepository.save(customer);
    }

    @Override
    public Customer putCustomer(Customer customer) throws BlankValueNotAllowedException, EntityIDNotFoundException {

        checkForBlankFields(customer);

        boolean exists = customerRepository.existsByCode(customer.getCode());
        if(!exists) {
            throw new EntityIDNotFoundException(ErrorMessages.CUSTOMER_NOT_FOUND);
        }

        log.info("Update Customer: {}", customer);
        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Customer customer) throws EntityIDNotFoundException, DependencyException {

        if(!customerRepository.existsByCode(customer.getCode())) {
            throw new EntityIDNotFoundException(ErrorMessages.CUSTOMER_NOT_FOUND);
        }
        if(customer.getPartNumberObjects() != null && !customer.getPartNumberObjects().isEmpty()) {
            throw new DependencyException(ErrorMessages.CUSTOMER_HAS_RELATIONSHIPS);
        }

        log.info("Delete Customer {}", customer);
        customerRepository.delete(customer);
    }

    @Override
    public boolean exists(String code) {
        boolean exists = isCodeNumberCorrect(code) ? customerRepository.existsByCode(code) : false;
        log.debug("Customer with code {} is persisted: {}", code, exists);
        return exists;
    }

    public boolean isCodeNumberCorrect(String code) throws NumberFormatException {
        if(Pattern.matches("[0-9]{8,11}", code)) return true;
        else throw new NumberFormatException("The code does not consist of 8 to 10 digits.");
    }

    private void checkForBlankFields(Customer customer) throws BlankValueNotAllowedException {
        if(StringUtils.isBlank(customer.getName())) {
            throw new BlankValueNotAllowedException(ErrorMessages.NAME_NOT_SPECIFIED);
        }
        if(StringUtils.isBlank(customer.getDeliveryLocation())) {
            throw new BlankValueNotAllowedException(ErrorMessages.DELIVERY_LOCATION_NOT_SPECIFIED);
        }
        if(customer.getCode() == null) {
            throw new BlankValueNotAllowedException(ErrorMessages.CUSTOMER_CODE_NOT_SPECIFIED);
        }
    }
}
