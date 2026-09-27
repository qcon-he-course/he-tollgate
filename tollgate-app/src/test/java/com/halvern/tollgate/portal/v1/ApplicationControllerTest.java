// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal.v1;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType;
import org.springframework.test.web.servlet.MockMvc;

import com.halvern.tollgate.notify.MailNotifier;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(type = DatabaseType.POSTGRES, provider = DatabaseProvider.ZONKY)
class ApplicationControllerTest {

    private static final String KEY = "HXPK-test0000000000000000000000000001";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MailNotifier mailNotifier;

    private String json(String width) {
        String first = LocalDate.now().plusDays(40).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return "{\"vehicleRegistration\":null,\"loadType\":\"CONSTRUCTION_PLANT\",\"loadDescription\":\"Excavator\","
                + "\"grossMassKg\":30000,\"widthM\":" + width + ",\"lengthM\":16.00,\"heightM\":4.00,\"route\":\"R4\","
                + "\"firstMoveDate\":\"" + first + "\",\"lastMoveDate\":\"" + first + "\","
                + "\"trips\":[{\"departure\":\"" + first + " 09:00\"}]}";
    }

    @Test
    void rejectsMissingKey() throws Exception {
        mvc.perform(get("/api/v1/applications")).andExpect(status().isUnauthorized());
    }

    @Test
    void createsADraft() throws Exception {
        mvc.perform(post("/api/v1/applications").header("X-Api-Key", KEY)
                .contentType(MediaType.APPLICATION_JSON).content(json("2.50")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference", startsWith("HX-A-")))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.indicativeTrack").value("EXPRESS"));
    }

    @Test
    void loadUpToFourAndAHalfMetresIsIndicativelyExpress() throws Exception {
        mvc.perform(post("/api/v1/applications").header("X-Api-Key", KEY)
                .contentType(MediaType.APPLICATION_JSON).content(json("4.80")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.indicativeTrack").value("EXPRESS"));
    }

    @Test
    void listsOwnApplications() throws Exception {
        mvc.perform(get("/api/v1/applications").header("X-Api-Key", KEY)).andExpect(status().isOk());
    }

    @Test
    void unknownApplicationIsNotFound() throws Exception {
        mvc.perform(get("/api/v1/applications/HX-A-1999-000000").header("X-Api-Key", KEY)).andExpect(status().isNotFound());
    }
}
