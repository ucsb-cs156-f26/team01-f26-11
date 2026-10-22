package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/ucsborganization/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization").param("orgCode", "ULT"))
        .andExpect(status().is(403)); // logged out users can't get by orgCode
  }

  // Authorization tests for /api/ucsbogranization/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsborganization/post")
                .param("orgCode", "ULT")
                .param("orgTranslationShort", "SMASH CLUB")
                .param("orgTranslation", "SMASH BROS CLUB")
                .param("inactive", "true")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsborganization/post")
                .param("orgCode", "ULT")
                .param("orgTranslationShort", "SMASH CLUB")
                .param("orgTranslation", "SMASH BROS CLUB")
                .param("inactive", "true")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    UCSBOrganization smash =
        UCSBOrganization.builder()
            .orgCode("ULT")
            .orgTranslationShort("SMASH CLUB")
            .orgTranslation("SMASH BROS CLUB")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById(eq("ULT"))).thenReturn(Optional.of(smash));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization").param("orgCode", "ULT"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("ULT"));
    String expectedJson = mapper.writeValueAsString(smash);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbOrganizationRepository.findById(eq("TNS"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization").param("orgCode", "TNS"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("TNS"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id TNS not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_organizations() throws Exception {

    // arrange

    UCSBOrganization smash =
        UCSBOrganization.builder()
            .orgCode("ULT")
            .orgTranslationShort("SMASH CLUB")
            .orgTranslation("SMASH BROS CLUB")
            .inactive(true)
            .build();

    UCSBOrganization badminton =
        UCSBOrganization.builder()
            .orgCode("BMN")
            .orgTranslationShort("BADMINTON CLUB")
            .orgTranslation("BADMINTON SPORT CLUB")
            .inactive(true)
            .build();

    ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
    expectedOrganizations.addAll(Arrays.asList(smash, badminton));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrganizations);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {
    // arrange

    UCSBOrganization smash =
        UCSBOrganization.builder()
            .orgCode("ULT")
            .orgTranslationShort("SMASH CLUB")
            .orgTranslation("SMASH BROS CLUB")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(smash))).thenReturn(smash);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganization/post")
                    .param("orgCode", "ULT")
                    .param("orgTranslationShort", "SMASH CLUB")
                    .param("orgTranslation", "SMASH BROS CLUB")
                    .param("inactive", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(smash);
    String expectedJson = mapper.writeValueAsString(smash);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_organization() throws Exception {
    // arrange

    UCSBOrganization smash =
        UCSBOrganization.builder()
            .orgCode("ULT")
            .orgTranslationShort("SMASH CLUB")
            .orgTranslation("SMASH BROS CLUB")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById(eq("ULT"))).thenReturn(Optional.of(smash));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization").param("orgCode", "ULT").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("ULT");
    verify(ucsbOrganizationRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id ULT deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(ucsbOrganizationRepository.findById(eq("123"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization").param("orgCode", "123").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("123");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id 123 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_organization() throws Exception {
    // arrange

    UCSBOrganization smashOrig =
        UCSBOrganization.builder()
            .orgCode("ULT")
            .orgTranslationShort("SMASH ULT CLUB")
            .orgTranslation("SMASH BROS ULTIMATE CLUB")
            .inactive(true)
            .build();

    UCSBOrganization smashEdit =
        UCSBOrganization.builder()
            .orgCode("SMS")
            .orgTranslationShort("SMASH CLUB")
            .orgTranslation("SMASH BROS CLUB")
            .inactive(false)
            .build();

    String requestBody = mapper.writeValueAsString(smashEdit);

    when(ucsbOrganizationRepository.findById(eq("ULT"))).thenReturn(Optional.of(smashOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization")
                    .param("orgCode", "ULT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("ULT");
    verify(ucsbOrganizationRepository, times(1))
        .save(smashEdit); // should be saved with updated info
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
    // arrange

    UCSBOrganization basketballEdit =
        UCSBOrganization.builder()
            .orgCode("BSK")
            .orgTranslationShort("BASKETBALL CLUB")
            .orgTranslation("BASKETBALL SPORT CLUB")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(basketballEdit);

    when(ucsbOrganizationRepository.findById(eq("BSK"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization")
                    .param("orgCode", "BSK")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("BSK");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id BSK not found", json.get("message"));
  }
}
