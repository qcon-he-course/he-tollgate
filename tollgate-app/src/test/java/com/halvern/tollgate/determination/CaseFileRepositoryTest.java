package com.halvern.tollgate.determination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureEmbeddedDatabase(type = DatabaseType.POSTGRES, provider = DatabaseProvider.ZONKY)
@Sql("/fixtures/open-cases.sql")
class CaseFileRepositoryTest {

    @Autowired
    private CaseFileRepository cases;

    @Test
    void aCaseIsFoundByItsReference() {
        assertThat(cases.findByReference("HX-A-2023-009002")).get()
                .extracting(CaseFile::getState, CaseFile::getTrack)
                .containsExactly("AWAITING_HAULIER", "ENGINEER");
    }

    @Test
    void anUnknownReferenceFindsNothing() {
        assertThat(cases.findByReference("HX-A-2023-000000")).isEmpty();
    }

    @Test
    void casesAreFoundByState() {
        assertThat(cases.findByStateIn(List.of("IN_REVIEW", "LODGED")))
                .extracting(CaseFile::getReference)
                .containsExactlyInAnyOrder("HX-A-2023-009001", "HX-A-2023-009003");
    }
}
