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

import static org.apache.commons.lang.BooleanUtils.isFalse;
import static org.openlmis.requisition.i18n.MessageKeys.ERROR_NULL_ID;
import static org.openlmis.requisition.i18n.MessageKeys.ERROR_ORDERABLE_NOT_IN_AVAILABLE_LIST;
import static org.openlmis.requisition.i18n.MessageKeys.ERROR_PROGRAM_NOT_FOUND;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.openlmis.requisition.domain.RequisitionTemplate;
import org.openlmis.requisition.dto.OrderableDto;
import org.openlmis.requisition.dto.ProgramOrderableDto;
import org.openlmis.requisition.exception.ValidationMessageException;
import org.openlmis.requisition.utils.Message;

public final class RequisitionBuilder {

  private RequisitionBuilder() {
  }

  /**
   * Create a new instance of Requisition with given facility, program IDs and emergency flag.
   *
   * @param facilityId UUID of facility
   * @param programId  UUID of program
   * @param emergency  flag
   * @return a new instance of Requisition
   * @throws ValidationMessageException if any of arguments is {@code null}
   */
  public static Requisition newRequisition(UUID facilityId, UUID programId, Boolean emergency) {
    if (facilityId == null || programId == null || emergency == null) {
      throw new ValidationMessageException(new Message(ERROR_NULL_ID));
    }
    return new Requisition(facilityId, programId, null, null, emergency);
  }

  /**
   * Creates new requisition object based on data from {@link Requisition.Importer} for update.
   *
   * @param importer instance of {@link Requisition.Importer}.
   * @param template the requisition template of updated requisition.
   * @param programId the program id of updated requisition.
   * @param requisitionStatus the requisition status of updated requisition.
   * @param orderables orderables for line items.
   * @return new instance of requisition.
   */
  public static Requisition newRequisition(
      Requisition.Importer importer, RequisitionTemplate template, UUID programId,
      RequisitionStatus requisitionStatus, Map<UUID, OrderableDto> orderables) {
    Requisition requisition = new Requisition();
    requisition.setRequisitionLineItems(new ArrayList<>());

    if (importer.getRequisitionLineItems() != null) {
      for (RequisitionLineItem.Importer requisitionLineItem : importer.getRequisitionLineItems()) {
        RequisitionLineItem item = RequisitionLineItem.newRequisitionLineItem(requisitionLineItem);
        OrderableDto orderable = orderables.get(item.getOrderableId());

        if (null == orderable) {
          throw new ValidationMessageException(
              new Message(ERROR_ORDERABLE_NOT_IN_AVAILABLE_LIST, item.getOrderableId()));
        }

        ProgramOrderableDto programOrderable = orderable.findProgramOrderableDto(programId);

        if (programOrderable != null) {
          item.setNonFullSupply(isFalse(programOrderable.getFullSupply()));
          item.setPricePerPack(programOrderable.getPricePerPack());
        } else {
          throw new ValidationMessageException(ERROR_PROGRAM_NOT_FOUND, programId);
        }
        if (isSkipped(requisitionLineItem) && requisitionStatus.isPreAuthorize()) {
          item.skipLineItem(template);
        }
        requisition.getRequisitionLineItems().add(item);
      }
    }
    requisition.setNumberOfMonthsInPeriod(importer.getProcessingPeriod().getDurationInMonths());
    requisition.setDraftStatusMessage(importer.getDraftStatusMessage());

    // required for conflict check
    requisition.setModifiedDate(importer.getModifiedDate());

    if (importer.getDatePhysicalStockCountCompleted() != null) {
      requisition.setDatePhysicalStockCountCompleted(
          new DatePhysicalStockCountCompleted(importer.getDatePhysicalStockCountCompleted()));
    }
    return requisition;
  }

  private static boolean isSkipped(RequisitionLineItem.Importer requisitionLineItem) {
    return requisitionLineItem.getSkipped() != null && requisitionLineItem.getSkipped();
  }
}
