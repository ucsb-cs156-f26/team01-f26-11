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
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
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

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

  @MockitoBean UCSBDiningCommonsMenuItemRepository ucsbdiningcmmonsmenuitemrepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/phones/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitem/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitem/all"))
        .andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitem").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  // Authorization tests for /api/phones/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsbdiningcommonsmenuitem/post")
                .param("diningCommonsCode", "Ortega")
                .param("name", "Food")
                .param("station", "Entree")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsbdiningcommonsmenuitem/post")
                .param("diningCommonsCode", "Ortega")
                .param("name", "Food")
                .param("station", "Entree")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  // // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    UCSBDiningCommonsMenuItem ucsbdiningcmmonsmenuitem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("Ortega")
            .name("Food")
            .station("Entree")
            .build();

    when(ucsbdiningcmmonsmenuitemrepository.findById(eq(7L)))
        .thenReturn(Optional.of(ucsbdiningcmmonsmenuitem)); // Check not sure why id is 7

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbdiningcmmonsmenuitemrepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(ucsbdiningcmmonsmenuitem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbdiningcmmonsmenuitemrepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbdiningcmmonsmenuitemrepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItem with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_restaurants() throws Exception {

    // arrange

    UCSBDiningCommonsMenuItem ucsbdiningcmmonsmenuitem1 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("Ortega")
            .name("Food")
            .station("Entree")
            .build();

    UCSBDiningCommonsMenuItem ucsbdiningcmmonsmenuitem2 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("DLG")
            .name("SHIT")
            .station("Blue Plate")
            .build();

    ArrayList<UCSBDiningCommonsMenuItem> expectedUCSBDiningCommonsMenuItem =
        new ArrayList<>(Arrays.asList(ucsbdiningcmmonsmenuitem1, ucsbdiningcmmonsmenuitem2));

    when(ucsbdiningcmmonsmenuitemrepository.findAll())
        .thenReturn(expectedUCSBDiningCommonsMenuItem);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbdiningcmmonsmenuitemrepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedUCSBDiningCommonsMenuItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_restaurant() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem ucsbdiningcmmonsmenuitem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("Ortega")
            .name("Food")
            .station("Entree")
            .build();

    when(ucsbdiningcmmonsmenuitemrepository.save(eq(ucsbdiningcmmonsmenuitem)))
        .thenReturn(ucsbdiningcmmonsmenuitem);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsbdiningcommonsmenuitem/post")
                    .param("diningCommonsCode", "Ortega")
                    .param("name", "Food")
                    .param("station", "Entree")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbdiningcmmonsmenuitemrepository, times(1)).save(ucsbdiningcmmonsmenuitem);
    String expectedJson = mapper.writeValueAsString(ucsbdiningcmmonsmenuitem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_restaurant() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem ucsbdiningcmmonsmenuitemOrig =
        UCSBDiningCommonsMenuItem.builder()
            .id(67L)
            .diningCommonsCode("Ortega")
            .name("Food")
            .station("Entree")
            .build();

    UCSBDiningCommonsMenuItem ucsbdiningcmmonsmenuitemEdited =
        UCSBDiningCommonsMenuItem.builder()
            .id(67L)
            .diningCommonsCode("DLG")
            .name("SHIT")
            .station("Blue Plate")
            .build();

    String requestBody = mapper.writeValueAsString(ucsbdiningcmmonsmenuitemEdited);

    when(ucsbdiningcmmonsmenuitemrepository.findById(eq(67L)))
        .thenReturn(Optional.of(ucsbdiningcmmonsmenuitemOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsbdiningcommonsmenuitem")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbdiningcmmonsmenuitemrepository, times(1)).findById(67L);
    verify(ucsbdiningcmmonsmenuitemrepository, times(1))
        .save(ucsbdiningcmmonsmenuitemEdited); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_restaurant_that_does_not_exist() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem ucsbdiningcmmonsmenuitemEdited =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("Ortega")
            .name("Food")
            .station("Entree")
            .build();

    String requestBody = mapper.writeValueAsString(ucsbdiningcmmonsmenuitemEdited);

    when(ucsbdiningcmmonsmenuitemrepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsbdiningcommonsmenuitem")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbdiningcmmonsmenuitemrepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_restaurant() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem ucsbdiningcmmonsmenuitem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("Ortega")
            .name("Food")
            .station("Entree")
            .build();

    when(ucsbdiningcmmonsmenuitemrepository.findById(eq(15L)))
        .thenReturn(Optional.of(ucsbdiningcmmonsmenuitem));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsbdiningcommonsmenuitem").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbdiningcmmonsmenuitemrepository, times(1)).findById(15L);
    verify(ucsbdiningcmmonsmenuitemrepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_restaurant_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(ucsbdiningcmmonsmenuitemrepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsbdiningcommonsmenuitem").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbdiningcmmonsmenuitemrepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 15 not found", json.get("message"));
  }
}
