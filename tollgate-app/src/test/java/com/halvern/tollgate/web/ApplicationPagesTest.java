package com.halvern.tollgate.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class ApplicationPagesTest extends AbstractPageTest {

    @Test
    void applicationPages() throws Exception {
        String ref = draft("CONSTRUCTION_PLANT", 30000, "2.50", "R4");
        mvc.perform(get("/applications").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/applications/new").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/applications/" + ref).with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/applications/" + ref + "/edit").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/applications/" + ref + "/questionnaire").with(user("officer1").roles("OFFICER")));
    }

    @Test
    void permitAndClosurePages() throws Exception {
        lodged("CONSTRUCTION_PLANT", 30000, "2.50", "R4");
        mvc.perform(get("/permits").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/closures").with(user("engineer1").roles("ENGINEER"))).andExpect(status().isOk());
        mvc.perform(get("/closures/new").with(user("engineer1").roles("ENGINEER"))).andExpect(status().isOk());
        mvc.perform(get("/panel").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/amendments").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
    }
}
