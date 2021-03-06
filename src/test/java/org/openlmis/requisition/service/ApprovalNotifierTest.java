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

package org.openlmis.requisition.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.requisition.i18n.MessageKeys.REQUISITION_EMAIL_ACTION_REQUIRED_CONTENT;
import static org.openlmis.requisition.i18n.MessageKeys.REQUISITION_EMAIL_ACTION_REQUIRED_SUBJECT;
import static org.openlmis.requisition.i18n.MessageKeys.REQUISITION_TYPE_EMERGENCY;
import static org.openlmis.requisition.service.PermissionService.REQUISITION_APPROVE;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.requisition.domain.requisition.Requisition;
import org.openlmis.requisition.domain.requisition.RequisitionStatus;
import org.openlmis.requisition.domain.requisition.StatusChange;
import org.openlmis.requisition.dto.FacilityDto;
import org.openlmis.requisition.dto.ProcessingPeriodDto;
import org.openlmis.requisition.dto.ProgramDto;
import org.openlmis.requisition.dto.RightDto;
import org.openlmis.requisition.dto.UserDto;
import org.openlmis.requisition.i18n.MessageService;
import org.openlmis.requisition.service.notification.NotificationService;
import org.openlmis.requisition.service.referencedata.FacilityReferenceDataService;
import org.openlmis.requisition.service.referencedata.PeriodReferenceDataService;
import org.openlmis.requisition.service.referencedata.ProgramReferenceDataService;
import org.openlmis.requisition.service.referencedata.RightReferenceDataService;
import org.openlmis.requisition.service.referencedata.SupervisingUsersReferenceDataService;
import org.openlmis.requisition.testutils.DtoGenerator;
import org.openlmis.requisition.testutils.UserDtoDataBuilder;
import org.openlmis.requisition.utils.Message;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class ApprovalNotifierTest {
  public static final String TEST_KEY = "testKey";
  private static final String APPROVER_1 = "approver1";

  @Mock
  private ProgramReferenceDataService programReferenceDataService;

  @Mock
  private PeriodReferenceDataService periodReferenceDataService;

  @Mock
  private NotificationService notificationService;

  @Mock
  private SupervisingUsersReferenceDataService supervisingUsersReferenceDataService;

  @Mock
  private RightReferenceDataService rightReferenceDataService;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private MessageService messageService;

  @InjectMocks
  private ApprovalNotifier approvalNotifier;

  private RightDto right = DtoGenerator.of(RightDto.class);
  private UUID supervisoryNodeId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private Requisition requisition = mock(Requisition.class);

  private static final String SUBJECT = "subject";
  private static final String EMERGENCY = "emergency";
  private static final String CONTENT = "Dear ${approver}: This email is informing you that the "
      + "${requisitionType} requisition submitted on ${submittedDate} for the Period ${periodName} "
      + "and ${programName} at ${facilityName} is ready for review. Please login to review "
      + "the requisition.${requisitionUrl}Thank you.";

  @Before
  public void setUp() throws Exception {
    mockServices();

    Field field = ApprovalNotifier.class.getDeclaredField("requisitionUri");
    field.setAccessible(true);
    field.set(approvalNotifier, "/requisition/");
  }

  @Test
  public void shouldCallNotificationService() throws Exception {
    UserDto approver = new UserDtoDataBuilder().withUsername("approver").build();

    when(supervisingUsersReferenceDataService.findAll(supervisoryNodeId, right.getId(), programId))
        .thenReturn(Collections.singletonList(approver));
    mockRequisition();
    mockMessages();

    mockChangeDate();

    approvalNotifier.notifyApprovers(requisition);

    verify(notificationService).notify(refEq(approver), eq(SUBJECT), contains("Dear approver: "
        + "This email is informing you that the test requisition"));
  }

  @Test
  public void shouldCallNotificationServiceForEmergencyRequisition() throws Exception {
    UserDto approver = new UserDtoDataBuilder().withUsername("approver").build();

    when(supervisingUsersReferenceDataService.findAll(supervisoryNodeId, right.getId(), programId))
        .thenReturn(Collections.singletonList(approver));
    mockRequisition(true);
    mockMessages();

    mockChangeDate();

    approvalNotifier.notifyApprovers(requisition);

    verify(notificationService).notify(refEq(approver), eq(SUBJECT), contains("Dear approver: "
        + "This email is informing you that the emergency requisition"));
  }

  @Test
  public void shouldCallNotificationServiceTwoTimesForTwoApproversWithProperUsernameInContent()
      throws Exception {
    UserDto approver = new UserDtoDataBuilder().withUsername(APPROVER_1).build();
    UserDto approver2 = new UserDtoDataBuilder().withUsername("approver2").build();

    when(supervisingUsersReferenceDataService.findAll(supervisoryNodeId, right.getId(), programId))
        .thenReturn(Arrays.asList(approver, approver2));
    mockRequisition();
    mockMessages();

    mockChangeDate();

    approvalNotifier.notifyApprovers(requisition);
    ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

    verify(notificationService, times(2))
        .notify(any(UserDto.class), eq(SUBJECT), argument.capture());

    List<String> values = argument.getAllValues();

    assertTrue(values.get(0).contains(APPROVER_1));
    assertTrue(values.get(1).contains("approver2"));
  }

  @Test
  public void shouldNotCallNotificationServiceIfStatusChangesAreNull() {
    UserDto approver = new UserDtoDataBuilder().withUsername(APPROVER_1).build();

    when(supervisingUsersReferenceDataService.findAll(supervisoryNodeId, right.getId(), programId))
        .thenReturn(Arrays.asList(approver));
    mockRequisition();
    mockMessages();

    approvalNotifier.notifyApprovers(requisition);

    verify(notificationService, times(0))
        .notify(any(UserDto.class), any(), any());
  }

  @Test
  public void shouldNotCallNotificationServiceIfStatusChangeForSubmitIsMissing() {
    UserDto approver = new UserDtoDataBuilder().withUsername(APPROVER_1).build();

    when(supervisingUsersReferenceDataService.findAll(supervisoryNodeId, right.getId(), programId))
        .thenReturn(Arrays.asList(approver));
    mockRequisition();
    mockMessages();
    mockChangeDate(RequisitionStatus.REJECTED);

    approvalNotifier.notifyApprovers(requisition);

    verify(notificationService, times(0))
        .notify(any(UserDto.class), any(), any());
  }

  private void mockChangeDate() {
    mockChangeDate(RequisitionStatus.SUBMITTED);
  }

  private void mockChangeDate(RequisitionStatus status) {
    StatusChange submitAuditEntry = mock(StatusChange.class);
    when(requisition.getStatusChanges()).thenReturn(Collections.singletonList(submitAuditEntry));
    when(submitAuditEntry.getStatus()).thenReturn(status);
    when(submitAuditEntry.getCreatedDate()).thenReturn(ZonedDateTime.now());
  }

  private void mockRequisition() {
    mockRequisition(false);
  }

  private void mockRequisition(Boolean emergency) {
    when(requisition.getSupervisoryNodeId()).thenReturn(supervisoryNodeId);
    when(requisition.getProgramId()).thenReturn(programId);
    when(requisition.getEmergency()).thenReturn(emergency);
  }

  private void mockServices() {
    when(rightReferenceDataService.findRight(REQUISITION_APPROVE)).thenReturn(right);
    when(periodReferenceDataService.findOne(any())).thenReturn(new ProcessingPeriodDto());
    when(programReferenceDataService.findOne(any())).thenReturn(new ProgramDto());
    when(facilityReferenceDataService.findOne(any())).thenReturn(new FacilityDto());
  }

  private void mockMessages() {
    Message.LocalizedMessage localizedMessage = new Message(TEST_KEY).new LocalizedMessage("test");
    when(messageService.localize(any())).thenReturn(localizedMessage);

    localizedMessage = new Message(TEST_KEY).new LocalizedMessage(EMERGENCY);
    when(messageService.localize(new Message(REQUISITION_TYPE_EMERGENCY)))
        .thenReturn(localizedMessage);
    localizedMessage = new Message(TEST_KEY).new LocalizedMessage(SUBJECT);
    when(messageService.localize(new Message(REQUISITION_EMAIL_ACTION_REQUIRED_SUBJECT)))
        .thenReturn(localizedMessage);
    localizedMessage = new Message(TEST_KEY).new LocalizedMessage(CONTENT);
    when(messageService.localize(new Message(REQUISITION_EMAIL_ACTION_REQUIRED_CONTENT)))
        .thenReturn(localizedMessage);
  }
}
