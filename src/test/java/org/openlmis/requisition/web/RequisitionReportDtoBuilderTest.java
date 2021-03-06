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

package org.openlmis.requisition.web;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.requisition.domain.requisition.Requisition;
import org.openlmis.requisition.domain.requisition.RequisitionLineItem;
import org.openlmis.requisition.domain.requisition.RequisitionStatus;
import org.openlmis.requisition.domain.requisition.StatusChange;
import org.openlmis.requisition.dto.ProgramOrderableDto;
import org.openlmis.requisition.dto.RequisitionDto;
import org.openlmis.requisition.dto.RequisitionLineItemDto;
import org.openlmis.requisition.dto.RequisitionReportDto;
import org.openlmis.requisition.dto.UserDto;
import org.openlmis.requisition.i18n.MessageKeys;
import org.openlmis.requisition.i18n.MessageService;
import org.openlmis.requisition.service.referencedata.UserReferenceDataService;
import org.openlmis.requisition.testutils.DtoGenerator;
import org.openlmis.requisition.utils.Message;
import org.openlmis.requisition.utils.RequisitionExportHelper;

@RunWith(MockitoJUnitRunner.class)
public class RequisitionReportDtoBuilderTest {

  private static final String SYSTEM = "SYSTEM";
  private static final Money TOTAL_COST = Money.of(CurrencyUnit.EUR, 15.6);
  private static final Money FS_TOTAL_COST = Money.of(CurrencyUnit.EUR, 3);
  private static final Money NFS_TOTAL_COST = Money.of(CurrencyUnit.EUR, 22.8);

  @Mock
  private RequisitionExportHelper exportHelper;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private RequisitionDtoBuilder requisitionDtoBuilder;

  @Mock
  private MessageService messageService;

  @Mock
  private Requisition requisition;

  @Mock
  private RequisitionDto requisitionDto;

  private UserDto user1 = DtoGenerator.of(UserDto.class, 2).get(0);
  private UserDto user2 = DtoGenerator.of(UserDto.class, 2).get(1);

  @Mock
  private List<RequisitionLineItem> fullSupply;

  @Mock
  private List<RequisitionLineItem> nonFullSupply;

  @InjectMocks
  private RequisitionReportDtoBuilder requisitionReportDtoBuilder =
      new RequisitionReportDtoBuilder();

  private List<RequisitionLineItemDto> fullSupplyDtos = new ArrayList<>();
  private List<RequisitionLineItemDto> nonFullSupplyLineDtos = new ArrayList<>();
  private UUID programId = UUID.randomUUID();

  @Before
  public void setUp() {
    when(userReferenceDataService.findOne(user1.getId())).thenReturn(user1);
    when(userReferenceDataService.findOne(user2.getId())).thenReturn(user2);
    when(requisitionDtoBuilder.build(requisition)).thenReturn(requisitionDto);

    when(requisition.getNonSkippedFullSupplyRequisitionLineItems())
        .thenReturn(fullSupply);
    when(requisition.getNonSkippedNonFullSupplyRequisitionLineItems())
        .thenReturn(nonFullSupply);
    when(exportHelper.exportToDtos(fullSupply))
        .thenReturn(fullSupplyDtos);
    when(exportHelper.exportToDtos(nonFullSupply))
        .thenReturn(nonFullSupplyLineDtos);

    Message msg = new Message(MessageKeys.STATUS_CHANGE_USER_SYSTEM);
    when(messageService.localize(msg))
      .thenReturn(msg. new LocalizedMessage(SYSTEM));

    when(requisition.getTotalCost()).thenReturn(TOTAL_COST);
    when(requisition.getFullSupplyTotalCost()).thenReturn(FS_TOTAL_COST);
    when(requisition.getNonFullSupplyTotalCost()).thenReturn(NFS_TOTAL_COST);
  }

  @Test
  public void shouldBuildDtoWithoutStatusChanges() {
    RequisitionReportDto dto = requisitionReportDtoBuilder.build(requisition);

    commonReportDtoAsserts(dto);
    assertNull(dto.getInitiatedBy());
    assertNull(dto.getInitiatedDate());
    assertNull(dto.getSubmittedBy());
    assertNull(dto.getSubmittedDate());
    assertNull(dto.getAuthorizedBy());
    assertNull(dto.getAuthorizedDate());
  }

  @Test
  public void shouldBuildDtoWithStatusChanges() {
    ZonedDateTime initDt = ZonedDateTime.now().minusDays(11);
    ZonedDateTime submitDt = ZonedDateTime.now().minusDays(6);
    ZonedDateTime authorizeDt = ZonedDateTime.now().minusDays(2);
    StatusChange initStatusChange = mock(StatusChange.class);
    StatusChange submitStatusChange = mock(StatusChange.class);
    StatusChange authorizeStatusChange = mock(StatusChange.class);
    when(initStatusChange.getStatus()).thenReturn(RequisitionStatus.INITIATED);
    when(initStatusChange.getCreatedDate()).thenReturn(initDt);
    when(initStatusChange.getAuthorId()).thenReturn(user1.getId());
    when(submitStatusChange.getStatus()).thenReturn(RequisitionStatus.SUBMITTED);
    when(submitStatusChange.getCreatedDate()).thenReturn(submitDt);
    when(submitStatusChange.getAuthorId()).thenReturn(user2.getId());
    when(authorizeStatusChange.getStatus()).thenReturn(RequisitionStatus.AUTHORIZED);
    when(authorizeStatusChange.getCreatedDate()).thenReturn(authorizeDt);
    when(authorizeStatusChange.getAuthorId()).thenReturn(user1.getId());
    List<StatusChange> statusChanges = new ArrayList<>();
    statusChanges.add(initStatusChange);
    statusChanges.add(submitStatusChange);
    statusChanges.add(authorizeStatusChange);
    when(requisition.getStatusChanges()).thenReturn(statusChanges);

    RequisitionReportDto dto = requisitionReportDtoBuilder.build(requisition);

    commonReportDtoAsserts(dto);
    assertEquals(user1, dto.getInitiatedBy());
    assertEquals(initDt, dto.getInitiatedDate());
    assertEquals(user2, dto.getSubmittedBy());
    assertEquals(submitDt, dto.getSubmittedDate());
    assertEquals(user1, dto.getAuthorizedBy());
    assertEquals(authorizeDt, dto.getAuthorizedDate());
  }

  @Test
  public void shouldBuildDtoWithSystemStatusChange() {
    ZonedDateTime now = ZonedDateTime.now();
    StatusChange initStatusChange = mock(StatusChange.class);
    when(initStatusChange.getStatus()).thenReturn(RequisitionStatus.INITIATED);
    when(initStatusChange.getCreatedDate()).thenReturn(now);
    List<StatusChange> statusChanges = Collections.singletonList(initStatusChange);
    when(requisition.getStatusChanges()).thenReturn(statusChanges);

    RequisitionReportDto dto = requisitionReportDtoBuilder.build(requisition);

    commonReportDtoAsserts(dto);
    assertNull(dto.getSubmittedBy());
    assertNull(dto.getSubmittedDate());
    assertNull(dto.getAuthorizedBy());
    assertNull(dto.getAuthorizedDate());

    assertEquals(now, dto.getInitiatedDate());
    UserDto fakeUser = dto.getInitiatedBy();
    assertNotNull(fakeUser);
    assertEquals(SYSTEM, fakeUser.getFirstName());
    assertNull(fakeUser.getLastName());
    assertEquals(SYSTEM, fakeUser.getUsername());
  }

  @Test
  public void shouldExportLineItemsToDtoAndSortByDisplayOrder() {
    when(exportHelper.exportToDtos(fullSupply))
        .thenReturn(prepareLineItemDtos());

    List<RequisitionLineItemDto> lineItemDtos = requisitionReportDtoBuilder
        .exportLinesToDtos(fullSupply, programId);

    assertTrue(getOrderableCategoryDisplayOrder(lineItemDtos.get(0))
        <= getOrderableCategoryDisplayOrder(lineItemDtos.get(1)));
    assertTrue(getOrderableCategoryDisplayOrder(lineItemDtos.get(1))
        <= getOrderableCategoryDisplayOrder(lineItemDtos.get(2)));
  }

  private List<RequisitionLineItemDto> prepareLineItemDtos() {
    List<RequisitionLineItemDto> dtos = DtoGenerator.of(RequisitionLineItemDto.class, 3, true);
    dtos.forEach(dto -> {
      HashSet<ProgramOrderableDto> programs = newHashSet(
          DtoGenerator.of(ProgramOrderableDto.class, 2, true));
      programs.iterator().next().setProgramId(programId);
      dto.getOrderable().setPrograms(programs);
    });
    return dtos;
  }

  private Integer getOrderableCategoryDisplayOrder(RequisitionLineItemDto lineItemDto) {
    return lineItemDto.getOrderable()
        .findProgramOrderableDto(programId)
        .getOrderableCategoryDisplayOrder();
  }

  private void commonReportDtoAsserts(RequisitionReportDto dto) {
    assertEquals(requisitionDto, dto.getRequisition());
    assertEquals(fullSupplyDtos, dto.getFullSupply());
    assertEquals(nonFullSupplyLineDtos, dto.getNonFullSupply());
    assertEquals(TOTAL_COST, dto.getTotalCost());
    assertEquals(FS_TOTAL_COST, dto.getFullSupplyTotalCost());
    assertEquals(NFS_TOTAL_COST, dto.getNonFullSupplyTotalCost());
  }
}
