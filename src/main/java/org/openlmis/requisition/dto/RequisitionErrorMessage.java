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

import org.openlmis.requisition.utils.Message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * General and field error messages linked with a specific requisition.
 */
@Getter
@AllArgsConstructor
public class RequisitionErrorMessage {

  private UUID requisitionId;
  private Message.LocalizedMessage errorMessage;
  private Map<String, String> fieldErrors;

  public RequisitionErrorMessage(UUID requisitionId, Message.LocalizedMessage message) {
    this.requisitionId = requisitionId;
    this.errorMessage = message;
  }

  public RequisitionErrorMessage(UUID requisitionId, Map<String, String> fieldErrors) {
    this.requisitionId = requisitionId;
    this.fieldErrors = fieldErrors;
  }
}
