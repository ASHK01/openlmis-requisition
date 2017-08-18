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

package org.openlmis.requisition.service.stockmanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.requisition.dto.ReasonCategory;
import org.openlmis.requisition.dto.ReasonDto;
import org.openlmis.requisition.dto.ReasonType;
import org.openlmis.requisition.dto.ValidReasonDto;
import org.openlmis.requisition.service.BaseCommunicationService;
import org.openlmis.requisition.service.RequestParameters;
import org.openlmis.utils.RequestHelper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ValidReasonStockManagementServiceTest
    extends BaseStockmanagementServiceTest<ValidReasonDto> {

  private ValidReasonStockmanagementService service;

  @Override
  protected BaseCommunicationService<ValidReasonDto> getService() {
    return new ValidReasonStockmanagementService();
  }

  @Override
  protected ValidReasonDto generateInstance() {
    ValidReasonDto validReason = new ValidReasonDto();
    validReason.setProgramId(UUID.randomUUID());
    validReason.setFacilityTypeId(UUID.randomUUID());

    ReasonDto reason = new ReasonDto();
    reason.setReasonCategory(ReasonCategory.ADJUSTMENT);
    reason.setReasonType(ReasonType.BALANCE_ADJUSTMENT);
    validReason.setReason(reason);

    return validReason;
  }

  @Before
  public void setUp() {
    super.setUp();
    service = (ValidReasonStockmanagementService) prepareService();
  }

  @Test
  public void shouldGetValidReasons() throws Exception {
    // given
    ValidReasonDto validReason = generateInstance();

    ResponseEntity response = ResponseEntity
        .ok(Collections.singletonList(validReason).toArray());

    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(ValidReasonDto[].class)))
        .thenReturn(response);

    // when
    List<ValidReasonDto> actual =
        service.search(validReason.getProgramId(), validReason.getFacilityTypeId());

    // then
    verify(restTemplate).exchange(
          uriCaptor.capture(), eq(HttpMethod.GET),
          entityCaptor.capture(), eq(ValidReasonDto[].class)
    );

    assertEquals(1, actual.size());
    assertEquals(validReason, actual.get(0));
    assertEquals(prepareUrl(validReason), uriCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
    assertAuthHeader(entityCaptor.getValue());
  }

  private URI prepareUrl(ValidReasonDto validReason) {
    RequestParameters parameters = RequestParameters
        .init()
        .set("program", validReason.getProgramId())
        .set("facilityType", validReason.getFacilityTypeId());

    return RequestHelper.createUri(service.getServiceUrl() + service.getUrl(), parameters);
  }
}
