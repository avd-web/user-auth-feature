package com.avd.security.api.controller;

import com.avd.security.api.TestUtil;
import com.avd.security.config.JwtService;
//import com.avd.security.user.User;
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
import org.springframework.test.web.servlet.MvcResult;

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
    @ValueSource(strings = {"user1234", "user12345"})
    public void testRegister(String input) throws Exception {

        String jsonRequest = "{\"email\" : \"" + input + "@mail.com\", \"password\" : \"" + input + "\", " + "\"role\" : \"USER\"" + "}";
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
//        assertTrue(Strings.isBlank(input));

    }

    @Test
    @Transactional
    public void testAuthenticate() throws Exception {

        User mockUser = TestUtil.createMockUser1();
        testEntityManager.persist(mockUser);

        String token = jwtService.generateToken(mockUser);
        String jsonRequest = "{ \"email\": \"user1@mail.com\", \"password\": \"password\" }";

        mockMvc.perform(
                        post("http://localhost:8080/api/v1/auth/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content(jsonRequest))
                .andExpect(status().isOk());

    }

    @Test
    @Transactional
    public void testAuthenticateBadCredentials() throws Exception {
        String jsonRequest = "{ \"email\": \"wrong@mail.com\", \"password\": \"wrong-password\" }";

        User mockUser = TestUtil.createMockUser1();
        testEntityManager.persist(mockUser);

        String token = jwtService.generateToken(mockUser);


        mockMvc.perform(
                        post("http://localhost:8080/api/v1/auth/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content(jsonRequest))
                .andExpect(status().is4xxClientError());

    }

    @Test
    @Transactional
    public void testAuthenticateWithoutCredentials() throws Exception {
        String jsonRequest = "{}";

        User mockUser = TestUtil.createMockUser1();
        testEntityManager.persist(mockUser);

        String token = jwtService.generateToken(mockUser);


        mockMvc.perform(
                        post("http://localhost:8080/api/v1/auth/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content(jsonRequest))
                .andExpect(status().is4xxClientError());

    }

    @Test
    @Transactional
    public void testRegisterFollowingAuthenticate() throws Exception {
        String jsonRequest =
                "{\"email\" : \"user1@mail.com\", " +
                "\"password\" : \"password\", " +
                "\"role\" : \"USER\"} ";


        MvcResult response = mockMvc.perform(
                post("/api/v1/auth/register")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)).andReturn();

        String responseBody = response.getResponse().getContentAsString();
        System.out.println(responseBody);
        String token = responseBody.substring(17, responseBody.indexOf("\",\"refresh_token"));

        mockMvc.perform(
                        post("http://localhost:8080/api/v1/auth/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content(jsonRequest))
                .andExpect(status().isOk());

    }

    @ParameterizedTest
    @EmptySource
    public void testRegisterInputEmpty(String input) throws Exception {

        //TODO: add input validation at RegisterRequest class

        String jsonRequest = "" +
                "{\"email\" : \"" + input + "\"," +
                " \"password\" : \"" + input + "\", " +
                "\"role\" : \"USER\"" + "}";
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
//        assertTrue(Strings.isBlank(input));

    }

    @ParameterizedTest
    @NullSource
    public void testRegisterContentIsNullString(String input) throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .content("" + input)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
//        assertTrue(Strings.isBlank(input));

    }

    @ParameterizedTest
    @EmptySource
    public void testRegisterRegisterContentIsEmpty(String input) throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .content(input)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
//        assertTrue(Strings.isBlank(input));

    }

}