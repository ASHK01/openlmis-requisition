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

package org.openlmis.requisition.service.fulfillment;

import static org.openlmis.utils.RequestHelper.createEntity;
import static org.openlmis.utils.RequestHelper.createUri;

import org.openlmis.requisition.dto.OrderDto;
import org.openlmis.requisition.dto.ProofOfDeliveryDto;
import org.openlmis.requisition.exception.ValidationMessageException;
import org.openlmis.requisition.i18n.MessageKeys;
import org.openlmis.requisition.service.RequestParameters;
import org.openlmis.utils.Message;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Service
public class OrderFulfillmentService extends BaseFulfillmentService<OrderDto> {

  /**
   * Creates a new instance of an order.
   *
   * @param order instance that contain data required to save order
   */
  public void create(OrderDto order) {
    try {
      String url = getServiceUrl() + getUrl();
      HttpEntity<OrderDto> body = createEntity(authService.obtainAccessToken(),
              order);
      postNew(url, body);
    } catch (RestClientException ex) {
      throw new ValidationMessageException(
          new Message(
              MessageKeys.ERROR_CONVERTING_REQUISITION_TO_ORDER, order.getExternalId()),
          ex);
    }
  }

  /**
   * Creates a new instance of order multiple orders by posting to the
   * batch order creation endpoint.
   *
   * @param orders list of orders to create
   */
  public void create(List<OrderDto> orders) {
    try {
      String url = getServiceUrl() + getBatchUrl();
      HttpEntity<List<OrderDto>> body = createEntity(authService.obtainAccessToken(),
              orders);
      postNew(url, body);
    } catch (RestClientException ex) {
      throw new ValidationMessageException(
          new Message(MessageKeys.ERROR_CONVERTING_MULTIPLE_REQUISITIONS), ex);
    }
  }

  /**
   * Finds orders that matched the provided parameters.
   */
  public Page<OrderDto> search(UUID supplyingFacility, UUID requestingFacility,
                               UUID program, UUID processingPeriod, String status) {
    RequestParameters parameters = RequestParameters
        .init()
        .set("supplyingFacility", supplyingFacility)
        .set("requestingFacility", requestingFacility)
        .set("program", program)
        .set("processingPeriod", processingPeriod)
        .set("status", status);

    return getPage("search", parameters);
  }

  /**
   * Finds proof of delivery related with the given order.
   */
  public ProofOfDeliveryDto getProofOfDelivery(UUID orderId) {
    return findOne(
        orderId + "/proofOfDeliveries",
        RequestParameters.init(),
        ProofOfDeliveryDto.class
    );
  }

  private void postNew(String url, HttpEntity<?> body) {
    restTemplate.postForEntity(createUri(url), body, Object.class);
  }

  protected String getBatchUrl() {
    return getUrl() + "batch";
  }

  @Override
  protected String getUrl() {
    return "/api/orders/";
  }

  @Override
  protected Class<OrderDto> getResultClass() {
    return OrderDto.class;
  }

  @Override
  protected Class<OrderDto[]> getArrayResultClass() {
    return OrderDto[].class;
  }

}
