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
import com.felixseifert.coma.backend.model.ProductDescription;
import com.felixseifert.coma.backend.repos.ProductDescriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProductDescriptionServiceImpl implements ProductDescriptionService {

    @Autowired
    private ProductDescriptionRepository productDescriptionRepository;

    @Override
    public List<ProductDescription> getAllProductDescriptions() {
        log.debug("Get all ProductDescriptions");
        return productDescriptionRepository.findAllByOrderByDescription();
    }

    @Override
    public ProductDescription postProductDescription(ProductDescription productDescription)
            throws BlankValueNotAllowedException, EntityAlreadyExistsException {

        if(StringUtils.isBlank(productDescription.getDescription())) {
            throw new BlankValueNotAllowedException(ErrorMessages.DESCRIPTION_NOT_SPECIFIED);
        }
        if(productDescription.getCode() == null) {
            throw new BlankValueNotAllowedException(ErrorMessages.DESCRIPTION_CODE_NOT_SPECIFIED);
        }

        boolean exists = productDescriptionRepository.existsByCode(productDescription.getCode());
        if(exists) {
            throw new EntityAlreadyExistsException(ErrorMessages.PRODUCT_DESCRIPTION_CODE_ALREADY_EXISTS);
        }
        exists = productDescriptionRepository.existsByDescription(productDescription.getDescription());
        if(exists) {
            throw new EntityAlreadyExistsException(ErrorMessages.PRODUCT_DESCRIPTION_TEXT_ALREADY_EXISTS);
        }

        log.info("Create new ProductDescription: {}", productDescription);
        return productDescriptionRepository.save(productDescription);
    }

    @Override
    public void deleteProductDescription(ProductDescription productDescription) throws EntityIDNotFoundException {
        if(!productDescriptionRepository.existsByCode(productDescription.getCode())) {
            throw new EntityIDNotFoundException(ErrorMessages.PRODUCT_DESCRIPTION_NOT_FOUND);
        }
        log.info("Delete ProductDescription {}", productDescription);
        productDescriptionRepository.delete(productDescription);
    }

    @Override
    public boolean existsByCode(String code) {
        boolean exists = productDescriptionRepository.existsByCode(code);
        log.debug("ProductDescription with code {} is persisted: {}", code, exists);
        return exists;
    }

    @Override
    public boolean existsByDescription(String description) {
        boolean exists = productDescriptionRepository.existsByDescription(description);
        log.debug("ProductDescription with description text {} is persisted: {}", description, exists);
        return exists;
    }
}
