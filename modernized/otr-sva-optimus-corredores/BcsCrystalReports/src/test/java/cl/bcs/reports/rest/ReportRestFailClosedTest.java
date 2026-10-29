package cl.bcs.reports.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** No INTERNAL_TOKEN configured (empty): every protected call is rejected, whatever header it sends. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:restclosed;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "services.internal-token="
})
@AutoConfigureMockMvc
class ReportRestFailClosedTest {

    @Autowired MockMvc mvc;

    @Test
    void emptySecretRejectsEverythingProtected() throws Exception {
        mvc.perform(post("/report").header("X-Internal-Token", "").content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/report").header("X-Internal-Token", "anything").content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/report").content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    void healthStillWorks() throws Exception {
        mvc.perform(get("/reports/health")).andExpect(status().isOk());
    }
}
