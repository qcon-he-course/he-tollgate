package com.halvern.tollgate.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class AdminPagesTest extends AbstractPageTest {

    @Test
    void adminPages() throws Exception {
        for (String page : new String[] { "/admin", "/admin/structures", "/admin/routes", "/admin/load-types", "/admin/questions",
                "/admin/holidays", "/admin/hauliers", "/admin/users", "/admin/reassign", "/admin/structures/1", "/admin/routes/1",
                "/admin/hauliers/1", "/admin/users/1", "/admin/hauliers/new", "/admin/users/new" }) {
            mvc.perform(get(page).with(user("admin1").roles("ADMIN")));
        }
        mvc.perform(get("/admin").with(user("admin1").roles("ADMIN"))).andExpect(status().isOk());
    }

    @Test
    void holidaysAndReassign() throws Exception {
        mvc.perform(post("/admin/holidays").param("holidayDate", "01/07/2031").param("description", "Test day")
                .with(user("admin1").roles("ADMIN"))).andExpect(status().is3xxRedirection());
        mvc.perform(post("/admin/reassign").param("from", "officer1").param("to", "chair1").with(user("admin1").roles("ADMIN")))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/admin/support/resync-status").with(user("admin1").roles("ADMIN"))).andExpect(status().is3xxRedirection());
    }

    @Test
    void officerCannotOpenAdmin() throws Exception {
        mvc.perform(get("/admin").with(user("officer1").roles("OFFICER"))).andExpect(status().isForbidden());
    }
}
