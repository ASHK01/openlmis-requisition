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

package org.openlmis.requisition.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BasicOrderableDto {
  private UUID id;
  private String productCode;
  private String fullProductName;
  private long netContent;
  private long packRoundingThreshold;
  private boolean roundToZero;

  /**
   * Returns the number of packs to order. For this Orderable given a desired number of
   * dispensing units, will return the number of packs that should be ordered.
   *
   * @param dispensingUnits # of dispensing units we'd like to order for
   * @return the number of packs that should be ordered.
   */
  public long packsToOrder(long dispensingUnits) {
    if (dispensingUnits <= 0 || netContent == 0) {
      return 0;
    }

    long packsToOrder = dispensingUnits / netContent;
    long remainderQuantity = dispensingUnits % netContent;

    if (remainderQuantity > 0 && remainderQuantity > packRoundingThreshold) {
      packsToOrder += 1;
    }

    if (packsToOrder == 0 && !roundToZero) {
      packsToOrder = 1;
    }

    return packsToOrder;
  }
}
