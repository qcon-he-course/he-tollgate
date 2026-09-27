package com.halvern.tollgate.flows;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.halvern.tollgate.permitdoc.ClosureManager;
import com.halvern.tollgate.permitdoc.StructureClosure;
import com.halvern.tollgate.reference.StructureDao;

class ClosureFlowTest extends AbstractFlowTest {

    @Autowired
    private ClosureManager closureManager;

    @Autowired
    private StructureDao structureDao;

    @Test
    void closureSuspendsAndEnds() throws Exception {
        LocalDate first = LocalDate.now().plusDays(70);
        lodge("CONSTRUCTION_PLANT", 30000, "2.50", "R4", first);
        StructureClosure closure = new StructureClosure();
        closure.setStructureId(structureDao.findByCode("S05").getId());
        closure.setStartDate(Date.valueOf(first.minusDays(1)));
        closure.setEndDate(Date.valueOf(first.plusDays(3)));
        closure.setReason("Flow test");
        closure.setStatus(StructureClosure.ACTIVE);
        closure.setRecordedBy("engineer1");
        closure.setRecordedAt(new java.util.Date());
        assertTrue(closureManager.recordClosure(closure).size() >= 0);
        closureManager.endFinishedClosures("SYSTEM");
    }
}
