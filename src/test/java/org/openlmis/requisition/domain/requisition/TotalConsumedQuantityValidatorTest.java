/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.requisition.domain.requisition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openlmis.requisition.domain.requisition.Requisition.REQUISITION_LINE_ITEMS;
import static org.openlmis.requisition.domain.requisition.RequisitionLineItem.TOTAL_CONSUMED_QUANTITY;
import static org.openlmis.requisition.i18n.MessageKeys.ERROR_IS_HIDDEN;
import static org.openlmis.requisition.i18n.MessageKeys.ERROR_MUST_BE_NON_NEGATIVE;
import static org.openlmis.requisition.i18n.MessageKeys.ERROR_VALUE_MUST_BE_ENTERED;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.openlmis.requisition.domain.RequisitionTemplateDataBuilder;
import org.openlmis.requisition.exception.ValidationMessageException;
import org.openlmis.requisition.utils.Message;

public class TotalConsumedQuantityValidatorTest {

  @Test
  public void shouldPassValidation() {
    Requisition requisition = new RequisitionDataBuilder()
        .addLineItem(new RequisitionLineItemDataBuilder().build())
        .build();

    TotalConsumedQuantityValidator validator =
        new TotalConsumedQuantityValidator(requisition, requisition.getTemplate());

    HashMap<String, Message> errors = new HashMap<>();
    validator.validateCanChangeStatus(errors);

    assertThat(errors).isEmpty();
  }

  @Test
  public void shouldRejectIfValueIsLessThanZeroDuringStatusChange() {
    TotalConsumedQuantityValidator validator = getTotalConsumedQuantityValidator(-10);

    HashMap<String, Message> errors = new HashMap<>();
    validator.validateCanChangeStatus(errors);

    assertThat(errors).hasSize(1);
    assertThat(errors).containsEntry(REQUISITION_LINE_ITEMS,
        new Message(ERROR_MUST_BE_NON_NEGATIVE, TOTAL_CONSUMED_QUANTITY));
  }

  @Test
  public void shouldRejectIfValueIsNullDuringStatusChange() {
    TotalConsumedQuantityValidator validator = getTotalConsumedQuantityValidator(null);

    HashMap<String, Message> errors = new HashMap<>();
    validator.validateCanChangeStatus(errors);

    assertThat(errors).hasSize(1);
    assertThat(errors).containsEntry(REQUISITION_LINE_ITEMS,
        new Message(ERROR_VALUE_MUST_BE_ENTERED, TOTAL_CONSUMED_QUANTITY));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenColumnDoesNotExist() {
    Requisition requisition = new RequisitionDataBuilder()
        .addLineItem(new RequisitionLineItemDataBuilder().build())
        .setTemplate(
            new RequisitionTemplateDataBuilder()
                .withAllColumnsExceptTotalConsumedQuantity()
                .build())
        .build();

    TotalConsumedQuantityValidator validator =
        new TotalConsumedQuantityValidator(requisition, requisition.getTemplate());

    validator.validateCanChangeStatus(new HashMap<>());
  }

  @Test
  public void shouldNotThrowExceptionWhenColumnDoesExist() {
    Requisition requisition = new RequisitionDataBuilder()
        .addLineItem(new RequisitionLineItemDataBuilder().build())
        .setTemplate(new RequisitionTemplateDataBuilder().withAllColumns().build())
        .build();

    TotalConsumedQuantityValidator validator =
        new TotalConsumedQuantityValidator(requisition, requisition.getTemplate());

    validator.validateCanChangeStatus(new HashMap<>());
  }

  @Test
  public void shouldRejectIfColumnIsHiddenAndValueNotEmpty() {
    Requisition requisition = new RequisitionDataBuilder()
        .addLineItem(new RequisitionLineItemDataBuilder().build())
        .setTemplate(
            new RequisitionTemplateDataBuilder().withTotalConsumedQuantityColumnHidden().build())
        .build();

    TotalConsumedQuantityValidator validator =
        new TotalConsumedQuantityValidator(requisition, requisition.getTemplate());

    Map<String, Message> errors = new HashMap<>();
    validator.validateCanChangeStatus(errors);

    assertThat(errors).hasSize(1);
    assertThat(errors).containsEntry(REQUISITION_LINE_ITEMS,
        new Message(ERROR_IS_HIDDEN, TOTAL_CONSUMED_QUANTITY));
  }

  private TotalConsumedQuantityValidator getTotalConsumedQuantityValidator(
      Integer totalConsumedQuantity) {
    Requisition requisition = new RequisitionDataBuilder()
        .addLineItem(new RequisitionLineItemDataBuilder()
            .setTotalConsumedQuantity(totalConsumedQuantity)
            .build())
        .build();

    return new TotalConsumedQuantityValidator(requisition, requisition.getTemplate());
  }

}
