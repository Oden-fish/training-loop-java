package com.example.looppractice.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TextServiceTest {

  private final TextService service = new TextService();

  @DisplayName("記号と空白をハイフンにまとめ、前後のハイフンを落とす")
  @ParameterizedTest(name = "[{index}] \"{0}\" -> \"{1}\"")
  @CsvSource({
    "'Hello, World!', hello-world",
    "'  --Hello--  ', hello",
    "'a___b   c', a-b-c",
  })
  void slugify(String input, String expected) {
    assertThat(service.slugify(input)).isEqualTo(expected);
  }

  @Test
  @DisplayName("空文字は空文字のまま")
  void slugifyEmpty() {
    assertThat(service.slugify("")).isEmpty();
  }
}
