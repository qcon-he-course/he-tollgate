package com.halvern.tollgate.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class ApiV2FlowTest extends AbstractPageTest {

    private static final String KEY = "HXPK-test0000000000000000000000000001";

    @Test
    void createLodgeAndRead() throws Exception {
        String day = LocalDate.now().plusDays(45).toString();
        String body = "{\"loadType\":\"CONSTRUCTION_PLANT\",\"loadDescription\":\"Api test load\",\"grossMassKg\":30000,"
                + "\"widthM\":2.50,\"lengthM\":16.00,\"heightM\":4.00,\"route\":\"R4\",\"firstMoveDate\":\"" + day
                + "\",\"lastMoveDate\":\"" + day + "\",\"trips\":[{\"departure\":\"" + day + "T09:00:00\"}]}";
        MvcResult created = mvc.perform(post("/api/v2/applications").header("X-Api-Key", KEY)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        String json = created.getResponse().getContentAsString();
        String ref = json.replaceAll(".*\"reference\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        mvc.perform(post("/api/v2/applications/" + ref + "/lodge").header("X-Api-Key", KEY));
        mvc.perform(get("/api/v2/applications/" + ref).header("X-Api-Key", KEY)).andExpect(status().isOk());
        mvc.perform(get("/api/v2/applications").header("X-Api-Key", KEY)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/applications").header("X-Api-Key", KEY)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/applications/" + ref).header("X-Api-Key", KEY)).andExpect(status().isOk());
    }
}
