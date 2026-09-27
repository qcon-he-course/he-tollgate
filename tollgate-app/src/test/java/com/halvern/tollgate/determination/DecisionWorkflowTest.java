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
class DecisionWorkflowTest {

    @MockBean
    private com.halvern.tollgate.notify.MailNotifier mailNotifier;

    @MockBean
    private com.halvern.tollgate.permitdoc.PermitRenderer permitRenderer;

    // H2 has no ON CONFLICT
    @MockBean
    private com.halvern.tollgate.permitdoc.PermitReferenceGenerator permitReferenceGenerator;

    private int next = 1;

    @org.junit.jupiter.api.BeforeEach
    void references() throws Exception {
        org.mockito.Mockito.when(permitReferenceGenerator.nextReference())
                .thenAnswer(i -> new com.halvern.tollgate.permitdoc.PermitReference(2019, next++));
        org.mockito.Mockito.when(permitRenderer.render(org.mockito.ArgumentMatchers.any())).thenReturn(new byte[] {1});
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
    private DecisionService decisionService;

    @Autowired
    private PermitVersionRepository permitVersionRepository;

    @Test
    @WithMockUser(username = "officer1", roles = "OFFICER")
    void deskCaseIssuedWithConditions() throws Exception {
        String ref = draft("CONSTRUCTION_PLANT", 60000, "3.50", "22.00", "4.00", "R4", false);
        CaseFile caseFile = caseService.lodge(ref, "officer1");
        PermitCondition c = new PermitCondition();
        c.setConditionType(PermitCondition.TIME_WINDOW);
        c.setDetail("Off-peak");
        Decision decision = decisionService.decide(ref, true, null, Collections.singletonList(c), "officer1");
        assertThat(decision.getOutcome()).isEqualTo(Outcomes.ISSUED_WITH_CONDITIONS);
        assertThat(permitVersionRepository.findByCaseIdOrderByVersionAsc(caseFile.getId())).hasSize(1);
    }

    @Test
    @WithMockUser(username = "officer1", roles = "OFFICER")
    void refusalNeedsAReason() throws Exception {
        String ref = draft("CONSTRUCTION_PLANT", 60000, "3.50", "22.00", "4.00", "R4", false);
        caseService.lodge(ref, "officer1");
        assertThatThrownBy(() -> decisionService.decide(ref, false, " ", null, "officer1")).hasMessageContaining("reason");
    }
}
