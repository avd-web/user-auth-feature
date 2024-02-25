package com.avd.security.api;

import com.avd.security.TestUtilities;
import com.avd.security.config.JwtService;
import com.avd.security.user.User;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
public class AuthenticationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestEntityManager testEntityManager;

    @ParameterizedTest
    @ValueSource(strings = {"ABCD1234", "ABCD1234  "})
    public void testRegister(String input) throws Exception {

        String requestBody = "{\"username\" : \"user@mail.com\", \"password\" : \""+ input + "\"}";
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .content(requestBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
//        assertTrue(Strings.isBlank(input));

    }

    @Test
    @Transactional
    public void testAuthenticate() throws Exception {

        User mockUser = TestUtilities.createMockUser1();
        testEntityManager.persist(mockUser);

        String token = jwtService.generateToken(mockUser);
        String jsonRequest = "{ \"username\": \"user1@mail.com\", \"password\": \"password\" }";

        mockMvc.perform(
                        post("http://localhost:8080/api/v1/auth/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content(jsonRequest))
                .andExpect(status().isOk());

    }
}
