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

package org.openlmis.requisition.testutils;

import org.openlmis.requisition.domain.SourceType;
import org.openlmis.requisition.dto.AvailableRequisitionColumnOptionDto;
import org.openlmis.requisition.dto.RequisitionTemplateColumnDto;

public class RequisitionTemplateColumnDtoDataBuilder {

  private String name;
  private String label;
  private String indicator;
  private int displayOrder;
  private Boolean isDisplayed;
  private SourceType source;
  private AvailableRequisitionColumnOptionDto option;
  private String definition;
  private String tag;

  /**
   * Constructs builder for {@link RequisitionTemplateColumnDto}.
   */
  public RequisitionTemplateColumnDtoDataBuilder() {
    this.name = "totalConsumedQuantity";
    this.label = "Total Consumed Quantity";
    this.indicator = "C";
    this.displayOrder = 1;
    this.isDisplayed = true;
    this.source = SourceType.USER_INPUT;
    this.option = new AvailableRequisitionColumnOptionDto();
    this.definition = "Quantity consumed in the reporting period.";
    this.tag = "consumption";
  }

  /**
   * Builds {@link RequisitionTemplateColumnDto} test data instance.
   * @return RequisitionTemplateColumnDto
   */
  public RequisitionTemplateColumnDto build() {
    RequisitionTemplateColumnDto dto = new RequisitionTemplateColumnDto();
    dto.setName(name);
    dto.setLabel(label);
    dto.setIndicator(indicator);
    dto.setDisplayOrder(displayOrder);
    dto.setIsDisplayed(isDisplayed);
    dto.setSource(source);
    dto.setOption(option);
    dto.setDefinition(definition);
    dto.setTag(tag);

    return dto;
  }

}
