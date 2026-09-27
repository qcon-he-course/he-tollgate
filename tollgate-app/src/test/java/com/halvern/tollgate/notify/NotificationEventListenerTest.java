package com.halvern.tollgate.notify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.ErrorCodes;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.platform.events.CaseDecided;
import com.halvern.tollgate.platform.events.PermitStatusChanged;
import com.halvern.tollgate.reference.Haulier;
import com.halvern.tollgate.reference.HaulierDao;

class NotificationEventListenerTest {

    private static final Instant NOW = Instant.parse("2023-03-14T12:00:00Z");

    private final MailNotifier mailNotifier = mock(MailNotifier.class);
    private final HaulierDao haulierDao = mock(HaulierDao.class);
    private final NotificationEventListener listener = new NotificationEventListener(mailNotifier, haulierDao);

    @BeforeEach
    void haulier() {
        Haulier haulier = new Haulier();
        haulier.setContactEmail("permits@marram.example");
        when(haulierDao.findById(7L)).thenReturn(haulier);
    }

    @Test
    void anIssuedCaseSendsTheApprovalWithThePermitDetails() throws Exception {
        listener.on(new CaseDecided("HX-A-2023-000101", 1L, 7L, "Marram Haulage", "R2", "ISSUED", null,
                "HX-P-2023-00042", LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 5), "officer1", NOW));

        Map<String, String> values = sentValues("application-approved", "HX-A-2023-000101");
        assertThat(values).containsEntry("permitReference", "HX-P-2023-00042")
                .containsEntry("firstMoveDate", "2023-04-03")
                .containsEntry("lastMoveDate", "2023-04-05")
                .containsEntry("routeCode", "R2");
    }

    @Test
    void aRefusedCaseSendsTheRefusalWithTheReason() throws Exception {
        listener.on(new CaseDecided("HX-A-2023-000102", 2L, 7L, "Marram Haulage", "R1", "REFUSED",
                "Bridge rating exceeded", null, null, null, "officer1", NOW));

        assertThat(sentValues("case-refused", "HX-A-2023-000102")).containsEntry("reason", "Bridge rating exceeded");
    }

    @Test
    void aRevokedPermitTellsTheHaulierWhy() throws Exception {
        listener.on(new PermitStatusChanged("HX-P-2023-00042", "HX-A-2023-000101", 7L, "ACTIVE", "REVOKED",
                "Load details were wrong", "officer1", NOW));

        assertThat(sentValues("permit-revoked", "HX-P-2023-00042")).containsEntry("reason", "Load details were wrong");
    }

    @ParameterizedTest
    @CsvSource({ "ACTIVE, SUSPENDED", "SUSPENDED, ACTIVE", "ACTIVE, EXPIRED", "ACTIVE, SUPERSEDED" })
    void otherPermitChangesAreNotMailedFromHere(String from, String to) throws Exception {
        listener.on(new PermitStatusChanged("HX-P-2023-00042", "HX-A-2023-000101", 7L, from, to, null, "SYSTEM", NOW));

        verify(mailNotifier, never()).send(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void aMailThatCannotBeSentFailsTheAction() throws Exception {
        when(mailNotifier.send(anyString(), anyString(), anyMap(), anyString()))
                .thenThrow(new TollgateException(ErrorCodes.MAIL_FAILED, "Mail server not available"));

        assertThatThrownBy(() -> listener.on(new PermitStatusChanged("HX-P-2023-00042", "HX-A-2023-000101", 7L,
                "ACTIVE", "REVOKED", "Load details were wrong", "officer1", NOW)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Mail server not available");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> sentValues(String template, String reference) throws Exception {
        ArgumentCaptor<Map<String, String>> values = ArgumentCaptor.forClass(Map.class);
        verify(mailNotifier).send(eq(template), eq("permits@marram.example"), values.capture(), eq(reference));
        return values.getValue();
    }
}
