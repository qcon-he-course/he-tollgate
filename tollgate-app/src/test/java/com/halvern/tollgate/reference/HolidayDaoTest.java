package com.halvern.tollgate.reference;

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
class HolidayDaoTest {

    @Autowired
    private HolidayDao holidayDao;

    @Test
    void christmasIsAHoliday() {
        assertThat(holidayDao.isHoliday(Date.valueOf(LocalDate.of(2026, 12, 25)))).isTrue();
        assertThat(holidayDao.isHoliday(Date.valueOf(LocalDate.of(2026, 12, 23)))).isFalse();
    }

    @Test
    void holidaysByYear() {
        assertThat(holidayDao.findByYear(2026)).isNotEmpty();
    }
}
