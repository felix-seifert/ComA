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

package com.felixseifert.coma.ui.views.selectablevalueview;

import com.felixseifert.coma.backend.model.enums.ValueGroup;
import com.felixseifert.coma.backend.service.CooPlantService;
import com.felixseifert.coma.backend.service.ProductDescriptionService;
import com.felixseifert.coma.backend.service.SelectableValueService;
import com.felixseifert.coma.ui.MainLayout;
import com.felixseifert.coma.ui.common.dialogs.DeleteConfirmationDialog;
import com.felixseifert.coma.ui.common.ViewConstants;
import com.felixseifert.coma.backend.model.Coo;
import com.felixseifert.coma.backend.model.Plant;
import com.felixseifert.coma.backend.model.ProductDescription;
import com.felixseifert.coma.backend.model.SelectableValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Route(value = "values", layout = MainLayout.class)
@PageTitle("Dropdown Values")
public class SelectableValueView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private SelectableValueService selectableValueService;

    private CooPlantService cooPlantService;

    private ProductDescriptionService productDescriptionService;

    private ComboBox<Coo> cooComboBox = new ComboBox<>("Country of Origin");
    private Button addCooButton = new Button("New CoO", VaadinIcon.PLUS.create());
    private Button deleteCooButton = new Button("Delete CoO", VaadinIcon.TRASH.create());

    private ComboBox<Plant> sourceComboBox = new ComboBox<>("Product Source (e.g. plant, warehouse)");
    private Button addSourceButton = new Button("New Source", VaadinIcon.PLUS.create());
    private Button deleteSourceButton = new Button("Delete Source", VaadinIcon.TRASH.create());

    private HorizontalLayout cooActions;
    private HorizontalLayout sourceActions;

    public SelectableValueView(SelectableValueService selectableValueService, CooPlantService cooPlantService,
                               ProductDescriptionService productDescriptionService) {

        this.selectableValueService = selectableValueService;
        this.cooPlantService = cooPlantService;
        this.productDescriptionService = productDescriptionService;

        Arrays.stream(ValueGroup.values())
                .sorted(Comparator.comparingInt(ValueGroup::getDatabaseCode))
                .forEachOrdered(group -> this.add(viewForValueGroup(group)));

        this.add(viewForDescriptionWithCode());
        this.add(viewForCooAndSource());
    }

    private HorizontalLayout viewForValueGroup(ValueGroup valueGroup) {

        DropdownAddDeleteLayout horizontalLayout = new DropdownAddDeleteLayout<>(
                valueGroup.getFieldDescription(), "New Value",
                SelectableValue::getLabel, selectableValueService.getSelectableValuesByValueGroup(valueGroup));

        // Dialog for creation and handler what to do when create is pressed
        SimpleCreateDialog createDialog = new SimpleCreateDialog(selectableValueService);
        createDialog.setChangeHandler(() -> horizontalLayout.getComboBox()
                .setItems(selectableValueService.getSelectableValuesByValueGroup(valueGroup)));

        // Dialog for delete confirmation and handler what to do when delete confirmed
        DeleteConfirmationDialog deleteConfirmationDialog =
                new DeleteConfirmationDialog("Delete Value for " + valueGroup.getFieldDescription(),
                        "Value to delete");
        deleteConfirmationDialog.setChangeHandler(() -> {
            SelectableValue selectableValue = (SelectableValue) horizontalLayout.getComboBox().getValue();
            selectableValueService.deleteSelectableValue(selectableValue);
            horizontalLayout.getComboBox().setItems(selectableValueService.getSelectableValuesByValueGroup(valueGroup));
            Notification.show(String.format("Value \"%s\" deleted", selectableValue.getLabel()));
        });

        // Listeners for buttons of created layout
        horizontalLayout.getAddButton().addClickListener(e -> {
            SelectableValue selectableValue = new SelectableValue();
            selectableValue.setValueGroup(valueGroup);
            createDialog.open(selectableValue);
        });
        horizontalLayout.getDeleteButton().addClickListener(e -> deleteConfirmationDialog
                .open(((SelectableValue) horizontalLayout.getComboBox().getValue()).getLabel()));

        return horizontalLayout;
    }

    private HorizontalLayout viewForDescriptionWithCode() {

        DropdownAddDeleteLayout horizontalLayout = new DropdownAddDeleteLayout<ProductDescription>(
                "Product Description", "New Product Description",
                pd -> String.format("%s (%s)", pd.getDescription(), pd.getCode()),
                productDescriptionService.getAllProductDescriptions());

        ProductDescriptionCreateDialog productDescriptionCreateDialog =
                new ProductDescriptionCreateDialog(productDescriptionService);
        productDescriptionCreateDialog.setChangeHandler(() -> horizontalLayout.getComboBox()
                .setItems(productDescriptionService.getAllProductDescriptions()));

        DeleteConfirmationDialog deleteConfirmationDialog =
                new DeleteConfirmationDialog("Delete Product Description", "Product Description to delete");
        deleteConfirmationDialog.setChangeHandler(() -> {
            ProductDescription productDescription = (ProductDescription) horizontalLayout.getComboBox().getValue();
            productDescriptionService.deleteProductDescription(productDescription);
            horizontalLayout.getComboBox().setItems(productDescriptionService.getAllProductDescriptions());
            Notification.show(String.format("Product Description \"%s (%s)\" deleted",
                    productDescription.getDescription(), productDescription.getCode()));
        });

        horizontalLayout.getAddButton().addClickListener(e -> {
            ProductDescription productDescription = new ProductDescription();
            productDescriptionCreateDialog.open(productDescription);
        });
        horizontalLayout.getDeleteButton().addClickListener(e -> deleteConfirmationDialog
                .open(String.format("%s (%s)",
                        ((ProductDescription) horizontalLayout.getComboBox().getValue()).getDescription(),
                        ((ProductDescription) horizontalLayout.getComboBox().getValue()).getCode())));

        return horizontalLayout;
    }

    private HorizontalLayout viewForCooAndSource() {
        cooComboBox.setItemLabelGenerator(c -> String.format("%s (%s)", c.getAbbreviation(), c.getName()));
        cooComboBox.setItems(cooPlantService.getAllCoos());
        cooComboBox.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);

        addCooButton.getElement().getThemeList().add("primary");
        deleteCooButton.getElement().getThemeList().add("error");
        deleteCooButton.setEnabled(false);
        cooActions = new HorizontalLayout(deleteCooButton, addCooButton);

        VerticalLayout cooLayout = new VerticalLayout(cooComboBox, cooActions);
        cooLayout.setPadding(false);

        sourceComboBox.setItemLabelGenerator(p -> String.format("%s (%s)", p.getName(), p.getCode()));
        sourceComboBox.setItems(List.of());
        sourceComboBox.setPlaceholder("Select CoO first");
        sourceComboBox.setMinWidth(ViewConstants.MIN_WIDTH_FOR_FIELD_STRING);

        addSourceButton.getElement().getThemeList().add("primary");
        addSourceButton.setEnabled(false);
        deleteSourceButton.getElement().getThemeList().add("error");
        deleteSourceButton.setEnabled(false);
        sourceActions = new HorizontalLayout(deleteSourceButton, addSourceButton);

        VerticalLayout sourceLayout = new VerticalLayout(sourceComboBox, sourceActions);
        sourceLayout.setPadding(false);

        cooComboBox.addValueChangeListener(event -> cooComboBoxValueChange(event.getValue()));
        sourceComboBox.addValueChangeListener(event -> {
            if(event.getValue() != null) deleteSourceButton.setEnabled(true);
            else deleteSourceButton.setEnabled(false);
        });

        createDeleteCoo();
        createDeleteSource();

        return new HorizontalLayout(cooLayout, sourceLayout);
    }

    private void createDeleteCoo() {
        // Dialog for CoO delete confirmation and handler what to do when delete confirmed
        DeleteConfirmationDialog deleteConfirmationDialog =
                new DeleteConfirmationDialog("Delete Country", "Country to delete");
        deleteConfirmationDialog.setChangeHandler(() -> {
            Coo coo = cooComboBox.getValue();
            cooPlantService.deleteCoo(coo);
            cooComboBox.setItems(cooPlantService.getAllCoos());
            Notification.show(String.format("Country \"%s (%s)\" and its %d plants deleted",
                    coo.getAbbreviation(), coo.getName(), coo.getPlants().size()));
        });

        // Dialog for CoO creation and handler what to do when create is pressed
        CooCreateDialog cooCreateDialog = new CooCreateDialog(cooPlantService);
        cooCreateDialog.setChangeHandler(() ->
                cooComboBox.setItems(cooPlantService.getAllCoos()));

        // Listeners for CoO buttons on this page
        addCooButton.addClickListener(e -> cooCreateDialog.open(new Coo()));
        deleteCooButton.addClickListener(e -> deleteConfirmationDialog.open(String.format("%s (%s)",
                cooComboBox.getValue().getAbbreviation(), cooComboBox.getValue().getName())));
    }

    private void createDeleteSource() {
        // Dialog for Plant delete confirmation and handler what to do when delete confirmed
        DeleteConfirmationDialog deleteConfirmationDialog =
                new DeleteConfirmationDialog("Delete Product Source", "Product Source to delete");
        deleteConfirmationDialog.setChangeHandler(() -> {
            Plant source = sourceComboBox.getValue();
            cooComboBox.getValue().getPlants().remove(source);
            cooPlantService.deletePlant(source);
            sourceComboBox.setItems(cooPlantService.getPlantsByCoo(source.getCoo()));
            Notification.show(String.format("Product Source \"%s\" in %s deleted",
                    source.getName(), source.getCoo().getName()));
        });

        // Dialog for source (Plant) creation and handler what to do when create is pressed
        PlantCreateDialog plantCreateDialog = new PlantCreateDialog(cooPlantService);
        plantCreateDialog.setChangeHandler(() ->
                sourceComboBox.setItems(cooPlantService.getPlantsByCoo(cooComboBox.getValue())));

        // Listeners for source buttons on this page
        addSourceButton.addClickListener(e -> {
            Plant source = new Plant();
            cooComboBox.getValue().addPlant(source);
            plantCreateDialog.open(source);
        });
        deleteSourceButton.addClickListener(e -> deleteConfirmationDialog.open(String.format("%s in %s",
                sourceComboBox.getValue().getName(), cooComboBox.getValue().getName())));
    }

    private void cooComboBoxValueChange(Coo eventValue) {
        if(eventValue != null) {
            sourceComboBox.setItems(cooPlantService.getPlantsByCoo(eventValue));
            sourceComboBox.setPlaceholder(null);
            addSourceButton.setEnabled(true);
            deleteCooButton.setEnabled(true);
        }
        else {
            sourceComboBox.setItems(List.of());
            sourceComboBox.setPlaceholder("Select CoO first");
            sourceActions.getChildren().filter(a -> a instanceof Button)
                    .forEach(b -> ((Button) b).setEnabled(false));
            deleteCooButton.setEnabled(false);
        }
    }
}
