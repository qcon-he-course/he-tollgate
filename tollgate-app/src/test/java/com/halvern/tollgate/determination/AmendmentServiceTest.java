package com.halvern.tollgate.determination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(type = DatabaseType.POSTGRES, provider = DatabaseProvider.ZONKY)
class AmendmentServiceTest {

    @MockBean
    private com.halvern.tollgate.notify.MailNotifier mailNotifier;

    @MockBean
    private com.halvern.tollgate.permitdoc.PermitRenderer permitRenderer;

    // CR-118 flags table is not in the test schema, so switch on what the test needs
    @MockBean
    private com.halvern.tollgate.platform.flags.FlagService flagService;

    // H2 has no ON CONFLICT
    @MockBean
    private com.halvern.tollgate.permitdoc.PermitReferenceGenerator permitReferenceGenerator;

    private int next = 1;

    @org.junit.jupiter.api.BeforeEach
    void references() throws Exception {
        org.mockito.Mockito.when(permitReferenceGenerator.nextReference())
                .thenAnswer(i -> new com.halvern.tollgate.permitdoc.PermitReference(2020, next++));
        org.mockito.Mockito.when(permitRenderer.render(org.mockito.ArgumentMatchers.any())).thenReturn(new byte[] {1});
        org.mockito.Mockito.when(flagService.isEnabled("express.auto-issue")).thenReturn(true);
    }

    @Autowired
    private com.halvern.tollgate.intake.ApplicationManager applicationManager;

    @Autowired
    private com.halvern.tollgate.reference.RouteDao routeDao;

    @Autowired
    private com.halvern.tollgate.reference.LoadTypeDao loadTypeDao;

    @Autowired
    private com.halvern.tollgate.reference.VehicleDao vehicleDao;

    private String draft(String loadType, int kg, String width, String length, String height, String route, boolean hazardous) {
        com.halvern.tollgate.intake.MovementApplication app = new com.halvern.tollgate.intake.MovementApplication();
        app.setHaulierId(1L);
        app.setVehicleId(vehicleDao.findByHaulier(1L).get(0).getId());
        app.setLoadTypeId(loadTypeDao.findByCode(loadType).getId());
        app.setLoadDescription("Test load");
        app.setGrossMassKg(kg);
        app.setWidthM(new BigDecimal(width));
        app.setLengthM(new BigDecimal(length));
        app.setHeightM(new BigDecimal(height));
        app.setHazardous(hazardous);
        app.setUnNumber(hazardous ? "1203" : null);
        app.setRouteId(routeDao.findByCode(route).getId());
        LocalDate first = LocalDate.now().plusDays(40);
        app.setFirstMoveDate(Date.valueOf(first));
        app.setLastMoveDate(Date.valueOf(first.plusDays(1)));
        com.halvern.tollgate.intake.ApplicationTrip trip = new com.halvern.tollgate.intake.ApplicationTrip();
        trip.setTripNo(1);
        trip.setDepartureAt(java.sql.Timestamp.valueOf(first.atTime(9, 0)));
        List<com.halvern.tollgate.intake.ApplicationTrip> trips = new ArrayList<>();
        trips.add(trip);
        app.setTrips(trips);
        return applicationManager.createDraft(app, "officer1");
    }

    @Autowired
    private CaseService caseService;

    @Autowired
    private AmendmentService amendmentService;

    @Autowired
    private PermitVersionRepository permitVersionRepository;

    @Test
    @WithMockUser(username = "officer1", roles = "OFFICER")
    void approvedAmendmentSupersedesTheOldVersion() throws Exception {
        String ref = draft("CONSTRUCTION_PLANT", 30000, "2.50", "16.00", "4.00", "R4", false);
        CaseFile caseFile = caseService.lodge(ref, "officer1");
        PermitVersion v1 = permitVersionRepository.findByCaseIdOrderByVersionAsc(caseFile.getId()).get(0);
        LocalDate first = v1.getWindowStart().plusDays(7);
        AmendmentRequest req = amendmentService.request(v1.getReference(), first, first.plusDays(1), "officer1");
        PermitVersion v2 = amendmentService.approve(req.getId(), "officer1");
        assertThat(v2.getVersion()).isEqualTo(2);
        assertThat(permitVersionRepository.findById(v1.getId()).get().getStatus()).isEqualTo(PermitVersion.SUPERSEDED);
    }
}
