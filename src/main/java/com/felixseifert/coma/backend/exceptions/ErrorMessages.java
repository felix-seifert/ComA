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

package com.felixseifert.coma.backend.exceptions;

public class ErrorMessages {

    public static final String PART_NUMBER_ALREADY_EXISTS = "Could not create new PN object. ID already exists.";
    public static final String PART_NUMBER_ID_NOT_FOUND = "Given Part Number ID could not be found.";
    public static final String PART_NUMBER_NOT_SPECIFIED = "Given Part Number is blank.";
    public static final String PART_NUMBER_LOB_ID_NOT_FOUND = "Given Part Number Lob ID could not be found.";

    public static final String EMPLOYEE_NOT_FOUND = "Employee with given ID could not be found.";
    public static final String EMPLOYEE_ALREADY_EXISTS = "Could not create new Employee. Given ID already exists.";

    public static final String EMPLOYEE_HAS_RELATIONSHIPS = "Employee still has relationships to other objects. Remove relationships before deleting Employee.";
    public static final String CUSTOMER_HAS_RELATIONSHIPS = "Customer still has relationships to other objects. Remove relationships before deleting Customer.";

    public static final String BUSINESS_UNIT_NOT_FOUND = "Business Unit could not be found.";
    public static final String BUSINESS_UNIT_ALREADY_EXISTS = "Could not create new Business Unit. Given name already exists.";
    public static final String BUSINESS_UNIT_HAS_DEPENDENCIES = "Business Unit still has dependencies. Remove dependencies before deleting Business Unit.";

    public static final String CUSTOMER_NOT_FOUND = "Customer with given code could not be found.";
    public static final String CUSTOMER_ALREADY_EXISTS = "Could not create new Customer. Given code already exists.";
    public static final String DELIVERY_LOCATION_NOT_SPECIFIED = "Given Delivery Location is blank.";
    public static final String CUSTOMER_CODE_NOT_SPECIFIED = "Customer Code serves as primary key and must be specified.";

    public static final String NAME_NOT_SPECIFIED = "Given Name is blank.";
    public static final String EMAIL_ADDRESS_NOT_SPECIFIED = "Given Email Address is blank.";

    public static final String LABEL_NOT_SPECIFIED = "Given Label is blank.";
    public static final String VALUE_GROUP_NOT_SPECIFIED = "Given ValueGroup is blank.";
    public static final String LABEL_VALUE_GROUP_ALREADY_EXISTS = "Could not create new SelectableValue. Label ValueGroup combination already exists.";
    public static final String SELECTABLE_VALUE_NOT_FOUND = "Given SelectableValue could not be found.";
    public static final String ABBREVIATION_NOT_SPECIFIED = "Given Abbreviation is blank.";
    public static final String COO_ALREADY_EXISTS = "Could not create new CoO. Name already exists.";
    public static final String COO_NOT_FOUND = "CoO with given name could not be found.";
    public static final String COO_NOT_SPECIFIED = "Given CoO is blank.";
    public static final String CODE_NOT_SPECIFIED = "Given code is blank.";
    public static final String PLANT_NOT_FOUND = "Plant with given name and CoO could not be found.";
    public static final String PLANT_CODE_NOT_FOUND = "Plant with given code could not be found.";
    public static final String PLANT_ALREADY_EXISTS = "Could not create new Plant. Name CoO combination already exists.";
    public static final String DESCRIPTION_NOT_SPECIFIED = "Given description text is blank.";
    public static final String DESCRIPTION_CODE_NOT_SPECIFIED = "Given code is blank.";
    public static final String PRODUCT_DESCRIPTION_CODE_ALREADY_EXISTS = "Given product description code already exists.";
    public static final String PRODUCT_DESCRIPTION_TEXT_ALREADY_EXISTS = "Given product description text already exists.";
    public static final String PRODUCT_DESCRIPTION_NOT_FOUND = "ProductDescription could not be found.";

    public static final String EMAIL_ADDRESS_ALREADY_EXISTS_NO_NEW_ENTITY = "Could not create new Employee. Given email address already exists.";
    public static final String EMAIL_ADDRESS_ALREADY_EXISTS_FOR_OTHER_ENTITY = "Given email address already exists for another Employee.";
    public static final String EMAIL_ADDRESS_NOT_FOUND = "Given Email Address could not be found.";

    public static final String EMPLOYEE_IS_NOT_SET = "Next step of task cannot be assigned to any Employee.";
    public static final String NO_EMPLOYEE_RESPONSIBLE = "Finished tasks need an Employee assigned (responsible).";

    public static final String REQUEST_DENIAL_NEEDS_COMMENTS = "Request is denied but no comments specified.";
}
