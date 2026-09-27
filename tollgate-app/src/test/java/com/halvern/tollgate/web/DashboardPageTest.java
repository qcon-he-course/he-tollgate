package com.halvern.tollgate.web;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class DashboardPageTest extends AbstractPageTest {

    @Test
    void dashboard() throws Exception {
        assessed("TRANSFORMER", 60000, "3.50", "R4");
        mvc.perform(get("/dashboard").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/dashboard").param("status", "GREEN").param("track", "DESK").param("officer", "officer1")
                .with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
    }

    @Test
    void oldDashboardWhenFlagOff() throws Exception {
        when(flagService.isEnabled("dashboard.v2")).thenReturn(false);
        mvc.perform(get("/dashboard").with(user("officer1").roles("OFFICER"))).andExpect(status().is3xxRedirection());
        mvc.perform(get("/dashboard-old").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
    }

    @Test
    void homeAndLogin() throws Exception {
        mvc.perform(get("/").with(user("officer1").roles("OFFICER"))).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
    }
}
