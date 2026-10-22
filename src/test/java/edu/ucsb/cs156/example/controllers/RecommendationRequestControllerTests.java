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
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
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

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/recommendationrequests/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().is(200)); // logged
  }

  // Authorization tests for /api/recommendationrequests?id=7

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  // Authorization tests for /api/recommendationrequests/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("professorEmail", "professor@ucsb.edu")
                .param("explanation", "This is a test explanation.")
                .param("dateRequested", "2026-10-20T12:00:00")
                .param("dateNeeded", "2026-11-01T00:00:00")
                .param("done", "true")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("professorEmail", "professor@ucsb.edu")
                .param("explanation", "This is a test explanation.")
                .param("dateRequested", "2026-10-20T12:00:00")
                .param("dateNeeded", "2026-11-01T00:00:00")
                .param("done", "true")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  // Test correctness of /api/recommendationrequests/all

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_correct() throws Exception {

    // arrange
    LocalDateTime dateRequested1 = LocalDateTime.parse("2026-10-20T12:00:00");
    LocalDateTime dateNeeded1 = LocalDateTime.parse("2026-11-01T00:00:00");

    RecommendationRequest recommendationRequest1 =
        RecommendationRequest.builder()
            .requesterEmail("student1@ucsb.edu")
            .professorEmail("professor1@ucsb.edu")
            .explanation("This is a test explanation.")
            .dateRequested(dateRequested1)
            .dateNeeded(dateNeeded1)
            .done(true)
            .build();

    LocalDateTime dateRequested2 = LocalDateTime.parse("2026-10-20T12:00:00");
    LocalDateTime dateNeeded2 = LocalDateTime.parse("2026-11-01T00:00:00");

    RecommendationRequest recommendationRequest2 =
        RecommendationRequest.builder()
            .requesterEmail("student2@ucsb.edu")
            .professorEmail("professor2@ucsb.edu")
            .explanation("This is another test explanation.")
            .dateRequested(dateRequested2)
            .dateNeeded(dateNeeded2)
            .done(false)
            .build();

    ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
    expectedRequests.addAll(Arrays.asList(recommendationRequest1, recommendationRequest2));

    when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Test correctness of /api/recommendationrequests/post

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void logged_in_admin_users_can_post_correct() throws Exception {

    // arrange
    LocalDateTime dateRequested = LocalDateTime.parse("2026-10-20T12:00:00");
    LocalDateTime dateNeeded = LocalDateTime.parse("2026-11-01T00:00:00");

    RecommendationRequest recommendationRequest =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("professor@ucsb.edu")
            .explanation("This is a test explanation.")
            .dateRequested(dateRequested)
            .dateNeeded(dateNeeded)
            .done(true)
            .build();

    when(recommendationRequestRepository.save(eq(recommendationRequest)))
        .thenReturn(recommendationRequest);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequests/post")
                    .param("requesterEmail", "student@ucsb.edu")
                    .param("professorEmail", "professor@ucsb.edu")
                    .param("explanation", "This is a test explanation.")
                    .param("dateRequested", "2026-10-20T12:00:00")
                    .param("dateNeeded", "2026-11-01T00:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).save(recommendationRequest);
    String expectedJson = mapper.writeValueAsString(recommendationRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Test correctness of /api/recommendationrequests?id=7
  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_by_id_when_the_id_exists_correct() throws Exception {

    // arrange
    LocalDateTime dateRequested = LocalDateTime.parse("2026-10-20T12:00:00");
    LocalDateTime dateNeeded = LocalDateTime.parse("2026-11-01T00:00:00");

    RecommendationRequest recommendationRequest =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("professor@ucsb.edu")
            .explanation("This is a test explanation.")
            .dateRequested(dateRequested)
            .dateNeeded(dateNeeded)
            .done(true)
            .build();

    when(recommendationRequestRepository.findById(eq(7L)))
        .thenReturn(Optional.of(recommendationRequest));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(recommendationRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_by_id_when_the_id_does_not_exist_correct() throws Exception {

    // arrange
    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests?id=7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void logged_in_admins_can_edit_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime dateRequested1 = LocalDateTime.parse("2026-10-20T12:00:00");
    LocalDateTime dateNeeded1 = LocalDateTime.parse("2026-11-01T00:00:00");

    RecommendationRequest recommendationRequest1 =
        RecommendationRequest.builder()
            .requesterEmail("student1@ucsb.edu")
            .professorEmail("professor1@ucsb.edu")
            .explanation("This is a test explanation.")
            .dateRequested(dateRequested1)
            .dateNeeded(dateNeeded1)
            .done(true)
            .build();

    LocalDateTime dateRequested2 = LocalDateTime.parse("2026-10-21T12:00:00");
    LocalDateTime dateNeeded2 = LocalDateTime.parse("2026-11-02T00:00:00");

    RecommendationRequest recommendationRequest2 =
        RecommendationRequest.builder()
            .requesterEmail("student2@ucsb.edu")
            .professorEmail("professor2@ucsb.edu")
            .explanation("This is another test explanation.")
            .dateRequested(dateRequested2)
            .dateNeeded(dateNeeded2)
            .done(false)
            .build();

    String requestBody = mapper.writeValueAsString(recommendationRequest2);

    when(recommendationRequestRepository.findById(eq(67L)))
        .thenReturn(Optional.of(recommendationRequest1));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    verify(recommendationRequestRepository, times(1))
        .save(recommendationRequest2); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void logged_in_admins_cannot_edit_when_the_id_does_not_exist() throws Exception {
    // arrange

    LocalDateTime dateRequested = LocalDateTime.parse("2026-10-20T12:00:00");
    LocalDateTime dateNeeded = LocalDateTime.parse("2026-11-01T00:00:00");

    RecommendationRequest recommendationRequest =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("professor@ucsb.edu")
            .explanation("This is a test explanation.")
            .dateRequested(dateRequested)
            .dateNeeded(dateNeeded)
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(recommendationRequest);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void logged_in_admins_can_delete_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime dateRequested = LocalDateTime.parse("2026-10-20T12:00:00");
    LocalDateTime dateNeeded = LocalDateTime.parse("2026-11-01T00:00:00");

    RecommendationRequest recommendationRequest =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("professor@ucsb.edu")
            .explanation("This is a test explanation.")
            .dateRequested(dateRequested)
            .dateNeeded(dateNeeded)
            .done(true)
            .build();

    when(recommendationRequestRepository.findById(eq(15L)))
        .thenReturn(Optional.of(recommendationRequest));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void logged_in_admins_cannot_delete_when_the_id_does_not_exist() throws Exception {

    // arrange
    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
  }
}
