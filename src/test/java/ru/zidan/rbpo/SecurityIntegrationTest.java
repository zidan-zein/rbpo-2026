package ru.zidan.rbpo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import ru.zidan.rbpo.entity.User;
import ru.zidan.rbpo.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestExecutionListeners(listeners = DependencyInjectionTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS)
class SecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    private static final String PASSWORD = "Museum-Test-2026!";

    @BeforeEach
    void prepare() {
        for (String table : new String[]{"exhibit_placements", "exhibitions", "exhibits", "exhibition_halls", "users"}) {
            jdbc.update("DELETE FROM app." + table);
        }
        account("admin", "ADMIN", true);
        account("reader", "USER", true);
        account("blocked", "USER", false);
    }

    private User account(String name, String role, boolean enabled) {
        User user = new User();
        user.setUsername(name);
        user.setFullName(name);
        user.setPassword(encoder.encode(PASSWORD));
        user.setRole(role);
        user.setEnabled(enabled);
        user.setCreatedAt(LocalDateTime.now());
        return users.save(user);
    }

    @ParameterizedTest
    @ValueSource(strings = {"users", "exhibits", "exhibitions", "exhibition-halls", "exhibit-placements"})
    void anonymousCannotRead(String path) throws Exception {
        mvc.perform(get("/api/" + path)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"exhibits", "exhibitions", "exhibition-halls", "exhibit-placements"})
    void readerCanReadMuseum(String path) throws Exception {
        mvc.perform(get("/api/" + path).with(httpBasic("reader", PASSWORD))).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"users", "exhibits", "exhibitions", "exhibition-halls", "exhibit-placements"})
    void readerCannotCreate(String path) throws Exception {
        mvc.perform(post("/api/" + path).with(httpBasic("reader", PASSWORD)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"users", "exhibits", "exhibitions", "exhibition-halls", "exhibit-placements"})
    void readerCannotPatchOrDelete(String path) throws Exception {
        mvc.perform(patch("/api/" + path + "/1").with(httpBasic("reader", PASSWORD)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/" + path + "/1").with(httpBasic("reader", PASSWORD)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void readerCannotChangeCurator() throws Exception {
        mvc.perform(put("/api/exhibitions/1/curator/2").with(httpBasic("reader", PASSWORD)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void readerCannotListAccounts() throws Exception {
        mvc.perform(get("/api/users").with(httpBasic("reader", PASSWORD))).andExpect(status().isForbidden());
    }

    @Test
    void servletErrorDispatchKeepsBusinessStatus() throws Exception {
        mvc.perform(get("/error").with(request -> {
                    request.setDispatcherType(DispatcherType.ERROR);
                    return request;
                })
                .requestAttr("jakarta.servlet.error.status_code", 409)
                .requestAttr("jakarta.servlet.error.request_uri", "/api/exhibits/1/restoration"))
                .andExpect(status().isConflict());
        mvc.perform(get("/error")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordAndBlockedAccount() throws Exception {
        mvc.perform(get("/api/auth/me").with(httpBasic("reader", "wrong"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").with(httpBasic("blocked", PASSWORD))).andExpect(status().isUnauthorized());
    }

    @Test
    void registrationHashesPasswordAndIgnoresRoleAndId() throws Exception {
        String body = json.writeValueAsString(Map.of("username", "newreader", "fullName", "New reader",
                "password", PASSWORD, "role", "ADMIN", "enabled", false, "id", 99999));
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.role").value("USER")).andExpect(jsonPath("$.enabled").value(true));
        User saved = users.findByUsername("newreader");
        assertNotEquals(99999L, saved.getId());
        assertNotEquals(PASSWORD, saved.getPassword());
        assertTrue(encoder.matches(PASSWORD, saved.getPassword()));
        mvc.perform(get("/api/auth/me").with(httpBasic("newreader", PASSWORD))).andExpect(status().isOk());
        mvc.perform(get("/api/users").with(httpBasic("newreader", PASSWORD))).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(strings = {"short", "alllowercase123!", "ALLUPPERCASE123!", "NoDigitsAtAll!", "NoSpecialChars123", "Aa1!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void weakOrOversizedPasswordRejected(String password) throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username", "newreader", "fullName", "Name", "password", password))))
                .andExpect(status().isBadRequest());
        assertNull(users.findByUsername("newreader"));
    }

    @Test
    void realCsrfTokenAndSessionRequired() throws Exception {
        String body = "{\"name\":\"Hall\",\"area\":50,\"available\":true}";
        mvc.perform(post("/api/exhibition-halls").with(httpBasic("admin", PASSWORD))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        var result = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        var token = json.readTree(result.getResponse().getContentAsString());
        var session = (MockHttpSession) result.getRequest().getSession(false);
        String header = token.get("headerName").asText();
        String value = token.get("token").asText();
        mvc.perform(post("/api/exhibition-halls").with(httpBasic("admin", PASSWORD)).session(session)
                .header(header, "wrong").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/exhibition-halls").with(httpBasic("admin", PASSWORD))
                .header(header, value).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/exhibition-halls").with(httpBasic("admin", PASSWORD)).session(session)
                .header(header, value).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanBlockAndRestoreAccount() throws Exception {
        Long id = users.findByUsername("reader").getId();
        mvc.perform(patch("/api/users/" + id + "/access").with(httpBasic("reader", PASSWORD)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/users/" + id + "/access").with(httpBasic("admin", PASSWORD)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/auth/me").with(httpBasic("reader", PASSWORD))).andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/users/" + id + "/access").with(httpBasic("admin", PASSWORD)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").with(httpBasic("reader", PASSWORD))).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/exhibitions/1/open", "/api/exhibitions/1/close", "/api/exhibitions/1/cancel", "/api/exhibits/1/restoration"})
    void readerCannotRunBusinessOperations(String path) throws Exception {
        mvc.perform(post(path).with(httpBasic("reader", PASSWORD)).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void patchCannotEscalateAndAdminReplyHidesHash() throws Exception {
        Long id = users.findByUsername("reader").getId();
        mvc.perform(patch("/api/users/" + id).with(httpBasic("admin", PASSWORD)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\",\"enabled\":false,\"password\":\"changed\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true)).andExpect(jsonPath("$.password").doesNotExist());
        assertTrue(encoder.matches(PASSWORD, users.findByUsername("reader").getPassword()));
        mvc.perform(get("/api/users").with(httpBasic("admin", PASSWORD)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].password").isEmpty());
    }

    @Test
    void allBusinessOperationsStillWorkWithAdmin() throws Exception {
        long curator = users.findByUsername("reader").getId();
        long other = users.findByUsername("blocked").getId();
        long hall = create("exhibition-halls", Map.of("name", "Hall", "area", 100, "available", true));
        long exhibit = create("exhibits", Map.of("name", "Vase", "condition", "GOOD", "requiredArea", 5));
        long exhibition = create("exhibitions", Map.of("name", "Museum", "startDate", "2026-01-01", "endDate", "2099-12-31", "curatorId", curator));
        create("exhibit-placements", Map.of("exhibitId", exhibit, "exhibitionId", exhibition, "hallId", hall));
        mvc.perform(put("/api/exhibitions/" + exhibition + "/curator/" + other).with(httpBasic("admin", PASSWORD)).with(csrf()))
                .andExpect(status().isOk());
        mvc.perform(post("/api/exhibitions/" + exhibition + "/open").with(httpBasic("admin", PASSWORD)).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("OPEN"));
        mvc.perform(post("/api/exhibits/" + exhibit + "/restoration").with(httpBasic("admin", PASSWORD)).with(csrf()))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/exhibitions/" + exhibition + "/close").with(httpBasic("admin", PASSWORD)).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CLOSED"));
        mvc.perform(post("/api/exhibits/" + exhibit + "/restoration").with(httpBasic("admin", PASSWORD)).with(csrf()))
                .andExpect(status().isOk());
        long planned = create("exhibitions", Map.of("name", "Later", "startDate", "2026-01-01", "endDate", "2099-12-31"));
        mvc.perform(post("/api/exhibitions/" + planned + "/open").with(httpBasic("admin", PASSWORD)).with(csrf()))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/exhibitions/" + planned + "/cancel").with(httpBasic("admin", PASSWORD)).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private long create(String path, Map<String, Object> data) throws Exception {
        var result = mvc.perform(post("/api/" + path).with(httpBasic("admin", PASSWORD)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(data)))
                .andExpect(status().isCreated()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
