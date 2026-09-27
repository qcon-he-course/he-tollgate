package com.halvern.tollgate.flows;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.intake.ApplicationManager;
import com.halvern.tollgate.intake.ApplicationTrip;
import com.halvern.tollgate.intake.MovementApplication;
import com.halvern.tollgate.platform.flags.FlagService;
import com.halvern.tollgate.questionnaire.AnswerForm;
import com.halvern.tollgate.questionnaire.CaseQuestion;
import com.halvern.tollgate.questionnaire.QuestionnaireManager;
import com.halvern.tollgate.reference.LoadTypeDao;
import com.halvern.tollgate.reference.RouteDao;
import com.halvern.tollgate.reference.VehicleDao;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType;

/**
 * CR-170 base for end to end service flows. Mail goes to a server that is not there, which the notifier logs.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(type = DatabaseType.POSTGRES, provider = DatabaseProvider.ZONKY)
abstract class AbstractFlowTest {

    @MockBean
    protected FlagService flagService;

    @Autowired
    protected CaseService caseService;

    @Autowired
    protected ApplicationManager applicationManager;

    @Autowired
    protected QuestionnaireManager questionnaireManager;

    @Autowired
    protected RouteDao routeDao;

    @Autowired
    protected LoadTypeDao loadTypeDao;

    @Autowired
    protected VehicleDao vehicleDao;

    @BeforeEach
    void setUp() {
        when(flagService.isEnabled(anyString())).thenReturn(false);
        when(flagService.isEnabled("express.auto-issue")).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("officer1", null,
                List.of(new SimpleGrantedAuthority("ROLE_OFFICER"), new SimpleGrantedAuthority("ROLE_ENGINEER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    protected String lodge(String loadType, int kg, String width, String route, LocalDate first) throws Exception {
        MovementApplication app = new MovementApplication();
        app.setHaulierId(2L);
        app.setVehicleId(vehicleDao.findByHaulier(2L).get(0).getId());
        app.setLoadTypeId(loadTypeDao.findByCode(loadType).getId());
        app.setLoadDescription("Flow test load");
        app.setGrossMassKg(kg);
        app.setWidthM(new BigDecimal(width));
        // profile dimensions from the test plan
        app.setLengthM(new BigDecimal("TRANSFORMER".equals(loadType) ? "22.00" : "MOBILE_CRANE".equals(loadType) ? "25.00" : "16.00"));
        app.setHeightM(new BigDecimal("MOBILE_CRANE".equals(loadType) ? "5.00" : "4.00"));
        app.setRouteId(routeDao.findByCode(route).getId());
        app.setFirstMoveDate(Date.valueOf(first));
        app.setLastMoveDate(Date.valueOf(first.plusDays(1)));
        ApplicationTrip trip = new ApplicationTrip();
        trip.setTripNo(1);
        trip.setDepartureAt(java.sql.Timestamp.valueOf(first.atTime(9, 0)));
        if (route.equals("R2") || route.equals("R3")) {
            trip.setCrossingAt(java.sql.Timestamp.valueOf(first.atTime(10, 0)));
        }
        List<ApplicationTrip> trips = new ArrayList<>();
        trips.add(trip);
        app.setTrips(trips);
        String ref = applicationManager.createDraft(app, "officer1");
        caseService.lodge(ref, "officer1");
        long appId = applicationManager.findByReference(ref).getId();
        List<Long> ids = new ArrayList<>();
        List<String> answers = new ArrayList<>();
        for (CaseQuestion q : questionnaireManager.findQuestions(appId)) {
            ids.add(q.getId());
            answers.add("Yes");
        }
        if (!ids.isEmpty()) {
            AnswerForm form = new AnswerForm();
            form.setCaseQuestionIds(ids);
            form.setAnswers(answers);
            questionnaireManager.saveAnswers(appId, form);
            for (CaseQuestion q : questionnaireManager.findQuestions(appId)) {
                if (q.isMandatory()) {
                    caseService.acceptAnswer(ref, q.getId(), "officer1", null);
                }
            }
        }
        return ref;
    }
}
