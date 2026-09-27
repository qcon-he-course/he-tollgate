package com.halvern.tollgate.reference;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(type = DatabaseType.POSTGRES, provider = DatabaseProvider.ZONKY)
class RouteDaoTest {

    @Autowired
    private RouteDao routeDao;

    @Test
    void longestRouteHasEightStructures() {
        assertThat(routeDao.findByCode("R8").getSegments()).hasSize(8);
    }

    @Test
    void minimumClearanceIsTheLowestOverbridge() {
        assertThat(routeDao.findMinimumClearance(routeDao.findByCode("R1").getId())).isEqualByComparingTo(new BigDecimal("5.10"));
    }
}
