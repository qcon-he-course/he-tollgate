package com.halvern.tollgate.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class CasePageTest extends AbstractPageTest {

    @Test
    void expressCasePage() throws Exception {
        String ref = lodged("CONSTRUCTION_PLANT", 30000, "2.50", "R4");
        mvc.perform(get("/cases/" + ref).with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/cases/" + ref + "/text").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(post("/cases/" + ref + "/reprint").with(user("officer1").roles("OFFICER"))).andExpect(status().is3xxRedirection());
        mvc.perform(post("/cases/" + ref + "/resend-decision").with(user("officer1").roles("OFFICER"))).andExpect(status().is3xxRedirection());
        mvc.perform(post("/cases/" + ref + "/permit/revoke").param("reason", "Test").with(user("officer1").roles("OFFICER")))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void deskCasePageAndActions() throws Exception {
        String ref = assessed("TRANSFORMER", 60000, "3.50", "R4");
        mvc.perform(get("/cases/" + ref).with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(post("/cases/" + ref + "/officer").param("officer", "officer1").with(user("officer1").roles("OFFICER")))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/cases/" + ref + "/comments").param("comments", "Checked").with(user("officer1").roles("OFFICER")))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/cases/" + ref + "/query-rounds").param("questions", "Axle spacing?").with(user("officer1").roles("OFFICER")))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/cases/" + ref + "/decision").param("outcome", "REFUSE").param("reason", "Route closed")
                .with(user("officer1").roles("OFFICER"))).andExpect(status().is3xxRedirection());
        mvc.perform(get("/cases/" + ref).with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
    }

    @Test
    void escalateAndReassess() throws Exception {
        String ref = assessed("TRANSFORMER", 60000, "3.50", "R4");
        mvc.perform(post("/cases/" + ref + "/escalate").param("reason", "Busy bridge").with(user("officer1").roles("OFFICER")))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/cases/" + ref + "/reassess").with(user("officer1").roles("OFFICER"))).andExpect(status().is3xxRedirection());
        mvc.perform(post("/cases/" + ref + "/withdraw").param("reason", "Haulier asked").with(user("officer1").roles("OFFICER")))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void answerReviewAjax() throws Exception {
        String ref = lodged("TRANSFORMER", 60000, "3.50", "R4");
        mvc.perform(get("/applications/" + ref + "/review").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(post("/cases/" + ref + "/answers/999999/review").param("action", "accept").with(user("officer1").roles("OFFICER")))
                .andExpect(status().isOk());
    }
}
