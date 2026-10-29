package cl.bcs.reports.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** X-Internal-Token on the servlet's endpoint: required, constant-time, allow-list, health stays public. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:restauth;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "services.internal-token=secret-token"
})
@AutoConfigureMockMvc
class ReportRestAuthTest {

    @Autowired MockMvc mvc;

    @Test
    void noTokenOrWrongTokenIs401WithTheServletsText() throws Exception {
        mvc.perform(post("/report").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized()).andExpect(content().string("401 - NOT AUTHORIZED"));
        mvc.perform(post("/report").header("X-Internal-Token", "wrong").content("{}"))
                .andExpect(status().isUnauthorized()).andExpect(content().string("401 - NOT AUTHORIZED"));
        mvc.perform(get("/report").header("X-Internal-Token", "secret-token-x"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/report").param("upload", "")).andExpect(status().isUnauthorized());
        mvc.perform(post("/report").param("update", "")).andExpect(status().isUnauthorized());
        mvc.perform(post("/report").param("rpt", "")).andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenReachesTheController() throws Exception {
        mvc.perform(post("/report").header("X-Internal-Token", "secret-token").content("{}"))
                .andExpect(status().isInternalServerError()).andExpect(content().string("500 - INTERNAL ERROR"));
    }

    @Test
    void pathTricksStayProtected() throws Exception {
        mvc.perform(post("/report;x=1")).andExpect(status().isUnauthorized());
        mvc.perform(post("//report")).andExpect(status().isUnauthorized());
        mvc.perform(get("/reports/health;x=1/../../report")).andExpect(status().isUnauthorized());
    }

    @Test
    void anythingNotOnTheAllowListIsProtected() throws Exception {
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
        mvc.perform(get("/nope")).andExpect(status().isUnauthorized());
    }

    @Test
    void trailingSlashEncodedAndDuplicatedPathsDoNotReachPublicOrProtectedHandlers() throws Exception {
        mvc.perform(post("/report/")).andExpect(status().isUnauthorized());
        mvc.perform(get("/report/")).andExpect(status().isUnauthorized());
        mvc.perform(post("/reports/%67enerate").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());   // raw URI is not the exact public path: protected
        mvc.perform(post("/reports//generate/").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/health/")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/health;x=1/../env")).andExpect(status().isUnauthorized());
        mvc.perform(get("/REPORT")).andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorInfoIsNoLongerPublicAndIsNotExposed() throws Exception {
        mvc.perform(get("/actuator/info")).andExpect(status().isUnauthorized());
        // with the token it is still not an exposed endpoint (management.endpoints.web.exposure.include=health)
        mvc.perform(get("/actuator/info").header("X-Internal-Token", "secret-token")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/env").header("X-Internal-Token", "secret-token")).andExpect(status().isNotFound());
    }

    @Test
    void otherMethodsAreRejectedWithAToken() throws Exception {
        mvc.perform(put("/report").header("X-Internal-Token", "secret-token")).andExpect(status().isInternalServerError());   // servlet: 405; here the pre-existing catch-all GlobalExceptionHandler answers 500 (documented)
    }

    @Test
    void healthStaysPublic() throws Exception {
        mvc.perform(get("/reports/health")).andExpect(status().isOk()).andExpect(content().string("BcsCrystalReports UP"));
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void generateIsStillOpenUntilItsConsumersSendTheToken() throws Exception {
        mvc.perform(post("/reports/generate").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
