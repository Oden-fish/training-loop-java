package com.example.looppractice.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.looppractice.text.TextService;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
  @DisplayName("text が空なら、どのフィールドがなぜ弾かれたかを ProblemDetail で返す")
  void rejectsBlankText() throws Exception {
    mockMvc
        .perform(
            post("/api/slugs")
                .contentType(MediaType.APPLICATION_JSON)
                .locale(Locale.ENGLISH)
                .content("{\"text\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.detail").isNotEmpty())
        .andExpect(jsonPath("$.errors[0].field").value("text"))
        .andExpect(jsonPath("$.errors[0].message").value("must not be blank"));
  }

  @Test
  @DisplayName("slug を作れない非 ASCII だけの text は 400 を返す（500 にしない）")
  void rejectsNonAsciiOnlyText() throws Exception {
    mockMvc
        .perform(
            post("/api/slugs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"\u65e5\u672c\u8a9e\u306e\u30bf\u30a4\u30c8\u30eb\"}"))
        .andExpect(status().isBadRequest());
  }
}
