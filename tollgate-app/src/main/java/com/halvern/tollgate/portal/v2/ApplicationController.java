// Partner API - Integration Squad 2021
// v2: copied from v1, see PI-44
package com.halvern.tollgate.portal.v2;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.halvern.tollgate.consultation.QueryRound;
import com.halvern.tollgate.consultation.QueryRoundService;
import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.intake.ApplicationDao;
import com.halvern.tollgate.intake.ApplicationManager;
import com.halvern.tollgate.intake.ApplicationTrip;
import com.halvern.tollgate.intake.MovementApplication;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.portal.security.ApiKeyAuthFilter;
import com.halvern.tollgate.portal.v2.dto.V2AnswerRequest;
import com.halvern.tollgate.portal.v2.dto.V2ApplicationDto;
import com.halvern.tollgate.portal.v2.dto.V2ApplicationRequest;
import com.halvern.tollgate.portal.v2.dto.V2QueryRoundDto;
import com.halvern.tollgate.portal.v2.dto.V2QuestionDto;
import com.halvern.tollgate.portal.v2.dto.V2TripRequest;
import com.halvern.tollgate.portal.v2.mapper.CaseStatusMapper;
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
 * Partner API v2: applications (PI-44).
 *
 * @author Marco Bellini
 */
@RestController("v2ApplicationController")
@RequestMapping("/api/v2/applications")
@Api(tags = "Applications v2")
public class ApplicationController {

    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private CaseService caseService;

    @Autowired
    private QuestionnaireManager questionnaireManager;

    @Autowired
    private QueryRoundService queryRoundService;

    @Autowired
    private VehicleDao vehicleDao;

    @Autowired
    private LoadTypeDao loadTypeDao;

    @Autowired
    private RouteDao routeDao;

    @ApiOperation("Create a draft application")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody V2ApplicationRequest request) {
        MovementApplication app = toApplication(request, new MovementApplication(), ApiKeyAuthFilter.currentHaulierId());
        String reference = applicationManager.createDraft(app, ApiKeyAuthFilter.currentActor());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto(applicationDao.findByReference(reference)));
    }

    // v2 change: drafts can be updated
    @ApiOperation("Update a draft application")
    @PutMapping("/{reference}")
    public ResponseEntity<?> update(@PathVariable String reference, @RequestBody V2ApplicationRequest request)
            throws TollgateException {
        MovementApplication app = ownApplication(reference);
        toApplication(request, app, app.getHaulierId());
        applicationManager.updateDraft(app);
        return ResponseEntity.ok(dto(applicationDao.findByReference(reference)));
    }

    @ApiOperation("Lodge a draft application")
    @PostMapping("/{reference}/lodge")
    public ResponseEntity<?> lodge(@PathVariable String reference) throws TollgateException {
        ownApplication(reference);
        caseService.lodge(reference, ApiKeyAuthFilter.currentActor());
        return ResponseEntity.ok(dto(applicationDao.findByReference(reference)));
    }

    @ApiOperation("Get the status and details of an application")
    @GetMapping("/{reference}")
    public ResponseEntity<?> get(@PathVariable String reference) {
        // v2 change: only the haulier's own applications
        return ResponseEntity.ok(dto(ownApplication(reference)));
    }

    @ApiOperation("List your applications, optionally by status")
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String status) {
        // v2 change: filtered in code, no SQL
        List<V2ApplicationDto> result = new ArrayList<>();
        for (MovementApplication app : applicationDao.findRecent(1000)) {
            if (!app.getHaulierId().equals(ApiKeyAuthFilter.currentHaulierId())) {
                continue;
            }
            V2ApplicationDto dto = dto(app);
            if (status == null || status.isEmpty() || status.equals(dto.getStatus())) {
                result.add(dto);
            }
        }
        return ResponseEntity.ok(result);
    }

    @ApiOperation("Answer the questionnaire")
    @PostMapping("/{reference}/answers")
    public ResponseEntity<?> answers(@PathVariable String reference, @RequestBody List<V2AnswerRequest> answers)
            throws TollgateException {
        MovementApplication app = ownApplication(reference);
        List<CaseQuestion> questions = questionnaireManager.findQuestions(app.getId());
        AnswerForm form = new AnswerForm();
        List<Long> ids = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        for (V2AnswerRequest a : answers) {
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
        return ResponseEntity.ok(dto(applicationDao.findByReference(reference)));
    }

    // v2 change: query round answers from the portal
    @ApiOperation("Answer a query round")
    @PostMapping("/{reference}/query-rounds/{roundNo}/answers")
    public ResponseEntity<?> queryRoundAnswers(@PathVariable String reference, @PathVariable int roundNo,
            @RequestBody List<String> answers) {
        ownApplication(reference);
        queryRoundService.answerRound(reference, roundNo, answers, ApiKeyAuthFilter.currentActor());
        return ResponseEntity.ok(dto(applicationDao.findByReference(reference)));
    }

    // v2 change: withdrawal from the portal
    @ApiOperation("Withdraw an application")
    @PostMapping("/{reference}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable String reference) {
        ownApplication(reference);
        caseService.withdraw(reference, ApiKeyAuthFilter.currentActor(), "Withdrawn by the haulier");
        return ResponseEntity.ok(dto(applicationDao.findByReference(reference)));
    }

    private V2ApplicationDto dto(MovementApplication app) {
        CaseFile caseFile = caseService.exists(app.getReference()) ? caseService.findByRef(app.getReference()) : null;
        V2ApplicationDto dto = V2ApplicationDto.toDto(app, caseFile, CaseStatusMapper.toStatus(caseFile));
        if (caseFile != null) {
            for (CaseQuestion q : questionnaireManager.findQuestions(app.getId())) {
                dto.getQuestions().add(V2QuestionDto.toDto(q));
            }
            for (QueryRound r : queryRoundService.findRounds(caseFile.getId())) {
                dto.getQueryRounds().add(V2QueryRoundDto.toDto(r));
            }
            if (caseService.currentPermit(caseFile) != null) {
                dto.setPermitReference(caseService.currentPermit(caseFile).getReference());
            }
        }
        return dto;
    }

    private MovementApplication ownApplication(String reference) {
        MovementApplication app = applicationDao.findByReference(reference);
        if (app == null || !app.getHaulierId().equals(ApiKeyAuthFilter.currentHaulierId())) {
            throw new NoSuchElementException(reference);
        }
        return app;
    }

    private MovementApplication toApplication(V2ApplicationRequest r, MovementApplication app, Long haulierId) {
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
        app.setFirstMoveDate(r.getFirstMoveDate() == null ? null : Date.valueOf(r.getFirstMoveDate()));
        app.setLastMoveDate(r.getLastMoveDate() == null ? null : Date.valueOf(r.getLastMoveDate()));
        List<ApplicationTrip> trips = new ArrayList<>();
        int n = 1;
        for (V2TripRequest t : r.getTrips()) {
            if (t.getDeparture() == null) {
                throw new BusinessException("TRIP", "Trip " + n + " needs a departure");
            }
            ApplicationTrip trip = new ApplicationTrip();
            trip.setTripNo(n++);
            trip.setDepartureAt(Timestamp.valueOf(t.getDeparture()));
            trip.setCrossingAt(t.getCrossing() == null ? null : Timestamp.valueOf(t.getCrossing()));
            trips.add(trip);
        }
        app.setTrips(trips);
        return app;
    }
}
