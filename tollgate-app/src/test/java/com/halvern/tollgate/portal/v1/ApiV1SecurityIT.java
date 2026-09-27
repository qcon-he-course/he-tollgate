// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal.v1;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

/**
 * Hauliers must only see their own applications (PI-40).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(type = DatabaseType.POSTGRES, provider = DatabaseProvider.ZONKY)
class ApiV1SecurityIT {

    private static final String KEY_A = "HXPK-test0000000000000000000000000001";
    private static final String KEY_B = "HXPK-test0000000000000000000000000002";

    @Autowired
    private MockMvc mvc;

    @Test
    void anotherHaulierCannotReadAnApplication() throws Exception {
        String first = LocalDate.now().plusDays(40).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        MvcResult created = mvc.perform(post("/api/v1/applications").header("X-Api-Key", KEY_A)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loadType\":\"CONSTRUCTION_PLANT\",\"grossMassKg\":30000,\"widthM\":2.50,\"lengthM\":16.00,"
                        + "\"heightM\":4.00,\"route\":\"R4\",\"firstMoveDate\":\"" + first + "\",\"lastMoveDate\":\"" + first
                        + "\",\"trips\":[{\"departure\":\"" + first + " 09:00\"}]}"))
                .andExpect(status().isCreated()).andReturn();
        String reference = JsonPath.read(created.getResponse().getContentAsString(), "$.reference");

        mvc.perform(get("/api/v1/applications/" + reference).header("X-Api-Key", KEY_B)).andExpect(status().isNotFound());
    }
}
