package org.openlmis.requisition.service.fulfillment;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.requisition.dto.OrderStatus.RECEIVED;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockitoAnnotations;
import org.openlmis.requisition.dto.OrderDto;
import org.openlmis.requisition.service.BaseCommunicationService;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RunWith(Parameterized.class)
public class OrderFulfillmentServiceParameterizedTest
    extends BaseFulfillmentServiceTest<OrderDto> {

  private static final String URI_QUERY_NAME = "name";
  private static final String URI_QUERY_VALUE = "value";

  @Override
  protected BaseCommunicationService<OrderDto> getService() {
    return new OrderFulfillmentService();
  }

  @Override
  protected OrderDto generateInstance() {
    OrderDto order = new OrderDto();
    order.setId(UUID.randomUUID());

    return order;
  }

  @Override
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    super.setUp();
  }

  private UUID supplyingFacility;
  private UUID requestingFacility;
  private UUID program;
  private UUID processingPeriod;
  private String status;

  /**
   * Creates new instance of Parameterized Test.
   */
  public OrderFulfillmentServiceParameterizedTest(UUID supplyingFacility, UUID requestingFacility,
                                                  UUID program, UUID processingPeriod,
                                                  String status) {
    this.supplyingFacility = supplyingFacility;
    this.requestingFacility = requestingFacility;
    this.program = program;
    this.processingPeriod = processingPeriod;
    this.status = status;
  }

  /**
   * Get test data.
   *
   * @return collection of objects that will be passed to test constructor.
   */
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {null, null, null, null, null},
        {UUID.randomUUID(), null, null, null, null},
        {null, UUID.randomUUID(), null, null, null},
        {null, null, UUID.randomUUID(), null, null},
        {null, null, null, UUID.randomUUID(), null},
        {null, null, null, null, RECEIVED.toString()},
        {UUID.randomUUID(), UUID.randomUUID(), null, null, null},
        {null, UUID.randomUUID(), UUID.randomUUID(), null, null},
        {null, null, UUID.randomUUID(), UUID.randomUUID(), null},
        {null, null, null, UUID.randomUUID(), RECEIVED.toString()},
        {UUID.randomUUID(), null, UUID.randomUUID(), null, RECEIVED.toString()},
        {null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null},
        {null, null, null, UUID.randomUUID(), null},
        {UUID.randomUUID(), null, null, null, null},
        {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, RECEIVED.toString()},
    });
  }

  @Test
  public void shouldCheckUserRight() {
    // given
    OrderFulfillmentService service = (OrderFulfillmentService) prepareService();
    OrderDto order = generateInstance();
    ResponseEntity<OrderDto[]> response = mock(ResponseEntity.class);

    // when
    when(restTemplate.getForEntity(any(URI.class), eq(service.getArrayResultClass())))
        .thenReturn(response);
    when(response.getBody()).thenReturn(new OrderDto[]{order});

    List<OrderDto> result = service.search(
        supplyingFacility, requestingFacility, program, processingPeriod, status
    );

    // then
    assertThat(result, hasSize(1));
    assertThat(result.get(0).getId(), is(equalTo(order.getId())));

    verify(restTemplate, atLeastOnce()).getForEntity(
        uriCaptor.capture(), eq(service.getArrayResultClass())
    );

    URI uri = uriCaptor.getValue();
    List<NameValuePair> parse = URLEncodedUtils.parse(uri, "UTF-8");

    assertQueryParameter(parse, "supplyingFacility", supplyingFacility);
    assertQueryParameter(parse, "requestingFacility", requestingFacility);
    assertQueryParameter(parse, "program", program);
    assertQueryParameter(parse, "processingPeriod", processingPeriod);
    assertQueryParameter(parse, "status", status);
  }

  private void assertQueryParameter(List<NameValuePair> parse, String field, Object value) {
    if (null != value) {
      assertThat(parse, hasItem(allOf(
          hasProperty(URI_QUERY_NAME, is(field)),
          hasProperty(URI_QUERY_VALUE, is(value.toString())))
      ));
    } else {
      assertThat(parse, not(hasItem(hasProperty(URI_QUERY_NAME, is(field)))));
    }
  }

}
