// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal.v1;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.halvern.tollgate.permitdoc.Permit;
import com.halvern.tollgate.permitdoc.PermitDao;
import com.halvern.tollgate.portal.security.ApiKeyAuthFilter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Permit search for the portal's permit list page (PI-27).
 *
 * @author Aisha Rahman
 */
@RestController
@RequestMapping("/api/v1/permits")
@Api(tags = "Permits v1")
public class PermitSearchController {

    @Autowired
    private PermitDao permitDao;

    @ApiOperation("Search your permits by reference prefix")
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(defaultValue = "HX-P-") String prefix) {
        List<Permit> result = new ArrayList<>();
        for (Permit p : permitDao.findRecent(500)) {
            if (p.getHaulierId().equals(ApiKeyAuthFilter.currentHaulierId()) && p.getReference().startsWith(prefix)) {
                result.add(p);
            }
        }
        return ResponseEntity.ok(result);
    }
}
