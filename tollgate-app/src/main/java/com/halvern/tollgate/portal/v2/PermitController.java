// Partner API - Integration Squad 2021
// v2: copied from v1, see PI-44
package com.halvern.tollgate.portal.v2;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.halvern.tollgate.determination.AmendmentRequest;
import com.halvern.tollgate.determination.AmendmentService;
import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.determination.PermitVersion;
import com.halvern.tollgate.determination.PermitVersionRepository;
import com.halvern.tollgate.intake.ApplicationDao;
import com.halvern.tollgate.permitdoc.PermitDao;
import com.halvern.tollgate.portal.security.ApiKeyAuthFilter;
import com.halvern.tollgate.portal.v2.dto.V2AmendmentRequest;
import com.halvern.tollgate.portal.v2.dto.V2PermitDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Partner API v2: permits (PI-47).
 *
 * @author Aisha Rahman
 */
@RestController("v2PermitController")
@RequestMapping("/api/v2/permits")
@Api(tags = "Permits v2")
public class PermitController {

    @Autowired
    private PermitVersionRepository permitVersionRepository;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private CaseService caseService;

    @Autowired
    private PermitDao permitDao;

    @Autowired
    private AmendmentService amendmentService;

    @ApiOperation("Latest version of a permit")
    @GetMapping("/{permitReference}")
    public ResponseEntity<?> get(@PathVariable String permitReference) {
        PermitVersion permit = ownPermit(permitReference);
        return ResponseEntity.ok(V2PermitDto.toDto(permit, caseService.conditionsFor(permit)));
    }

    @ApiOperation("Printed permit")
    @GetMapping("/{permitReference}/document")
    public ResponseEntity<?> document(@PathVariable String permitReference) {
        PermitVersion permit = ownPermit(permitReference);
        byte[] pdf = permitDao.findDocument(permit.getId());
        if (pdf == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + permitReference + ".pdf\"")
                .body(pdf);
    }

    @ApiOperation("Request new movement dates")
    @PostMapping("/{permitReference}/amendments")
    public ResponseEntity<?> amend(@PathVariable String permitReference, @RequestBody V2AmendmentRequest request) {
        ownPermit(permitReference);
        AmendmentRequest req = amendmentService.request(permitReference, request.getFirstMoveDate(),
                request.getLastMoveDate(), ApiKeyAuthFilter.currentActor());
        return ResponseEntity.accepted().body(req);
    }

    private PermitVersion ownPermit(String permitReference) {
        PermitVersion permit = permitVersionRepository.findFirstByReferenceOrderByVersionDesc(permitReference)
                .orElseThrow(() -> new NoSuchElementException(permitReference));
        Long owner = applicationDao.findById(permit.getApplicationId()).getHaulierId();
        if (!owner.equals(ApiKeyAuthFilter.currentHaulierId())) {
            throw new NoSuchElementException(permitReference);
        }
        return permit;
    }
}
