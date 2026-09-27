package com.halvern.tollgate.web;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.intake.ApplicationManager;
import com.halvern.tollgate.intake.ApplicationTrip;
import com.halvern.tollgate.intake.MovementApplication;
import com.halvern.tollgate.notify.MailNotifier;
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
 * CR-170 base for page tests: embedded database, MockMvc, flags and mail mocked.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(type = DatabaseType.POSTGRES, provider = DatabaseProvider.ZONKY)
abstract class AbstractPageTest {

    @MockBean
    protected FlagService flagService;

    @MockBean
    protected MailNotifier mailNotifier;

    @Autowired
    protected MockMvc mvc;

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
    void flagsAndUser() {
        when(flagService.isEnabled(anyString())).thenReturn(true);
        when(flagService.isEnabled("permit.pdf.legacy-renderer")).thenReturn(false);
        when(flagService.isEnabled("consultation.lapse-grace-day")).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("officer1", null,
                List.of(new SimpleGrantedAuthority("ROLE_OFFICER"), new SimpleGrantedAuthority("ROLE_ENGINEER"))));
    }

    @AfterEach
    void clearUser() {
        SecurityContextHolder.clearContext();
    }

    protected String draft(String loadType, int kg, String width, String route) {
        MovementApplication app = new MovementApplication();
        app.setHaulierId(1L);
        app.setVehicleId(vehicleDao.findByHaulier(1L).get(0).getId());
        app.setLoadTypeId(loadTypeDao.findByCode(loadType).getId());
        app.setLoadDescription("Page test load\nsecond line");
        app.setGrossMassKg(kg);
        app.setWidthM(new BigDecimal(width));
        // profile dimensions from the test plan
        app.setLengthM(new BigDecimal("TRANSFORMER".equals(loadType) ? "22.00" : "MOBILE_CRANE".equals(loadType) ? "25.00" : "16.00"));
        app.setHeightM(new BigDecimal("MOBILE_CRANE".equals(loadType) ? "5.00" : "4.00"));
        app.setRouteId(routeDao.findByCode(route).getId());
        LocalDate first = LocalDate.now().plusDays(45);
        app.setFirstMoveDate(Date.valueOf(first));
        app.setLastMoveDate(Date.valueOf(first.plusDays(1)));
        ApplicationTrip trip = new ApplicationTrip();
        trip.setTripNo(1);
        trip.setDepartureAt(java.sql.Timestamp.valueOf(first.atTime(9, 0)));
        List<ApplicationTrip> trips = new ArrayList<>();
        trips.add(trip);
        app.setTrips(trips);
        return applicationManager.createDraft(app, "officer1");
    }

    protected String lodged(String loadType, int kg, String width, String route) throws Exception {
        String ref = draft(loadType, kg, width, route);
        caseService.lodge(ref, "officer1");
        return ref;
    }

    protected String assessed(String loadType, int kg, String width, String route) throws Exception {
        String ref = lodged(loadType, kg, width, route);
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
