// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal.v1;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.intake.ApplicationDao;
import com.halvern.tollgate.intake.ApplicationManager;
import com.halvern.tollgate.intake.ApplicationTrip;
import com.halvern.tollgate.intake.LegacyDateUtil;
import com.halvern.tollgate.intake.MovementApplication;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.portal.security.ApiKeyAuthFilter;
import com.halvern.tollgate.portal.v1.dto.V1AnswerRequest;
import com.halvern.tollgate.portal.v1.dto.V1ApplicationRequest;
import com.halvern.tollgate.portal.v1.dto.V1TripRequest;
import com.halvern.tollgate.portal.v1.support.IndicativeTrackEstimator;
import com.halvern.tollgate.questionnaire.AnswerForm;
import com.halvern.tollgate.questionnaire.CaseQuestion;
import com.halvern.tollgate.questionnaire.QuestionnaireManager;
import com.halvern.tollgate.reference.LoadType;
import com.halvern.tollgate.reference.LoadTypeDao;
import com.halvern.tollgate.reference.Route;
import com.halvern.tollgate.reference.RouteDao;
import com.halvern.tollgate.reference.Vehicle;
import com.halvern.tollgate.reference.VehicleDao;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Partner API v1: applications (PI-14).
 *
 * @author Marco Bellini
 */
@RestController("v1ApplicationController")
@RequestMapping("/api/v1/applications")
@Api(tags = "Applications v1")
public class ApplicationController {

    private static final String LIST_SQL =
            "SELECT reference, status, first_move_date, last_move_date, created_at FROM movement_application WHERE haulier_id = ";

    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private CaseService caseService;

    @Autowired
    private QuestionnaireManager questionnaireManager;

    @Autowired
    private IndicativeTrackEstimator indicativeTrackEstimator;

    @Autowired
    private VehicleDao vehicleDao;

    @Autowired
    private LoadTypeDao loadTypeDao;

    @Autowired
    private RouteDao routeDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ApiOperation("Create a draft application")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody V1ApplicationRequest request) {
        MovementApplication app = toApplication(request, ApiKeyAuthFilter.currentHaulierId());
        String reference = applicationManager.createDraft(app, ApiKeyAuthFilter.currentActor());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reference", reference);
        body.put("status", MovementApplication.STATUS_DRAFT);
        body.put("indicativeTrack", indicativeTrackEstimator.estimate(app));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @ApiOperation("Lodge a draft application")
    @PostMapping("/{reference}/lodge")
    public ResponseEntity<?> lodge(@PathVariable String reference) throws TollgateException {
        ownApplication(reference);
        caseService.lodge(reference, ApiKeyAuthFilter.currentActor());
        return get(reference);
    }

    @ApiOperation("Get an application and its case")
    @GetMapping("/{reference}")
    public ResponseEntity<?> get(@PathVariable String reference) {
        MovementApplication app = applicationDao.findByReference(reference);
        if (app == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reference", app.getReference());
        body.put("status", app.getStatus());
        body.put("indicativeTrack", indicativeTrackEstimator.estimate(app));
        body.put("application", app);
        body.put("case", caseService.exists(reference) ? caseService.findByRef(reference) : null);
        return ResponseEntity.ok(body);
    }

    @ApiOperation("List your applications, optionally by status")
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String status) {
        String sql = LIST_SQL + ApiKeyAuthFilter.currentHaulierId();
        if (status != null && !status.isEmpty()) {
            sql = sql + " AND status = '" + status + "'";
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql + " ORDER BY created_at DESC");
        return ResponseEntity.ok(rows);
    }

    @ApiOperation("Answer the questionnaire")
    @PostMapping("/{reference}/answers")
    public ResponseEntity<?> answers(@PathVariable String reference, @RequestBody List<V1AnswerRequest> answers)
            throws TollgateException {
        MovementApplication app = ownApplication(reference);
        List<CaseQuestion> questions = questionnaireManager.findQuestions(app.getId());
        AnswerForm form = new AnswerForm();
        List<Long> ids = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        for (V1AnswerRequest a : answers) {
            for (CaseQuestion q : questions) {
                if (q.getSeq() == a.getSeq()) {
                    ids.add(q.getId());
                    texts.add(a.getAnswer());
                }
            }
        }
        form.setCaseQuestionIds(ids);
        form.setAnswers(texts);
        caseService.reviseAnswers(reference, form, ApiKeyAuthFilter.currentActor());
        return get(reference);
    }

    private MovementApplication ownApplication(String reference) {
        MovementApplication app = applicationDao.findByReference(reference);
        if (app == null || !app.getHaulierId().equals(ApiKeyAuthFilter.currentHaulierId())) {
            throw new NoSuchElementException(reference);
        }
        return app;
    }

    private MovementApplication toApplication(V1ApplicationRequest r, Long haulierId) {
        MovementApplication app = new MovementApplication();
        app.setHaulierId(haulierId);
        Vehicle vehicle = r.getVehicleRegistration() == null ? null : vehicleDao.findByRegistration(r.getVehicleRegistration().toUpperCase());
        if (vehicle != null && !vehicle.getHaulierId().equals(haulierId)) {
            throw new BusinessException("VEHICLE", "Vehicle " + r.getVehicleRegistration() + " is not one of yours");
        }
        app.setVehicleId(vehicle == null ? null : vehicle.getId());
        LoadType loadType = r.getLoadType() == null ? null : loadTypeDao.findByCode(r.getLoadType());
        app.setLoadTypeId(loadType == null ? null : loadType.getId());
        app.setLoadDescription(r.getLoadDescription());
        app.setGrossMassKg(r.getGrossMassKg());
        app.setWidthM(r.getWidthM());
        app.setLengthM(r.getLengthM());
        app.setHeightM(r.getHeightM());
        app.setHazardous(r.isHazardous());
        app.setUnNumber(r.getUnNumber());
        Route route = r.getRoute() == null ? null : routeDao.findByCode(r.getRoute());
        app.setRouteId(route == null ? null : route.getId());
        app.setFirstMoveDate(LegacyDateUtil.parseDate(r.getFirstMoveDate()));
        app.setLastMoveDate(LegacyDateUtil.parseDate(r.getLastMoveDate()));
        List<ApplicationTrip> trips = new ArrayList<>();
        int n = 1;
        for (V1TripRequest t : r.getTrips()) {
            Date departure = LegacyDateUtil.parseDateTime(t.getDeparture());
            if (departure == null) {
                throw new BusinessException("TRIP", "Trip " + n + " needs a departure as dd/MM/yyyy HH:mm");
            }
            ApplicationTrip trip = new ApplicationTrip();
            trip.setTripNo(n++);
            trip.setDepartureAt(new Timestamp(departure.getTime()));
            Date crossing = LegacyDateUtil.parseDateTime(t.getCrossing());
            trip.setCrossingAt(crossing == null ? null : new Timestamp(crossing.getTime()));
            trips.add(trip);
        }
        app.setTrips(trips);
        return app;
    }
}
