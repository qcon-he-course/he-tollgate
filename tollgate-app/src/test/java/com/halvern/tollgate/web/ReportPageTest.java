package com.halvern.tollgate.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class ReportPageTest extends AbstractPageTest {

    @Test
    void reports() throws Exception {
        lodged("CONSTRUCTION_PLANT", 30000, "2.50", "R4");
        assessed("TRANSFORMER", 60000, "3.50", "R4");
        mvc.perform(get("/reports").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/reports").param("month", "2024-03").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/reports/cases.csv").param("from", "01/01/2020").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/reports/hauliers/1").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(post("/reports/digest").param("to", "lead@halvern-crossings.example").with(user("officer1").roles("OFFICER")))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void searchAndMyCases() throws Exception {
        String ref = assessed("MOBILE_CRANE", 120000, "4.50", "R4");
        mvc.perform(get("/cases/search").param("q", "page test").param("state", "IN_REVIEW").param("from", "01/01/2020")
                .with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/cases/search").param("from", "not a date").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/my-cases").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(post("/cases/assign-engineer").param("references", ref).param("engineer", "engineer1")
                .with(user("officer1").roles("OFFICER"))).andExpect(status().is3xxRedirection());
    }
}
