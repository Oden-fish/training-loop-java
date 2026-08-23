package com.example.looppractice.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.looppractice.text.TextService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SlugController.class)
@Import(TextService.class)
class SlugControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("文字列を slug に変換して 201 を返す")
  void createsSlug() throws Exception {
    mockMvc
        .perform(
            post("/api/slugs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Hello, World!\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.slug").value("hello-world"));
  }

  @Test
  @DisplayName("text が空なら 400 を返す")
  void rejectsBlankText() throws Exception {
    mockMvc
        .perform(
            post("/api/slugs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"\"}"))
        .andExpect(status().isBadRequest());
  }
}
