package com.example.looppractice.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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

  @DisplayName("上限以下はそのまま、超えたら末尾を … にして全体を上限ちょうどにする")
  @ParameterizedTest(name = "[{index}] \"{0}\" ({1}) -> \"{2}\"")
  @CsvSource({
    "'hello', 5, 'hello'",
    "'hi', 5, 'hi'",
    "'hello!', 5, 'hell\u2026'",
    "'hello', 1, '\u2026'",
  })
  void truncate(String input, int maxLength, String expected) {
    assertThat(service.truncate(input, maxLength)).isEqualTo(expected);
  }

  @Test
  @DisplayName("切り詰めた結果の長さは上限ちょうどで、… は上限の内側に収まる")
  void truncateKeepsExactLength() {
    assertThat(service.truncate("abcdefghij", 4)).hasSize(4).endsWith("\u2026");
  }

  @Test
  @DisplayName("空文字は空文字のまま")
  void truncateEmpty() {
    assertThat(service.truncate("", 3)).isEmpty();
  }

  @DisplayName("上限が 1 未満なら例外にする（切り詰め結果に … を収められないため）")
  @ParameterizedTest(name = "[{index}] maxLength={0}")
  @ValueSource(ints = {0, -1})
  void truncateRejectsNonPositiveMax(int maxLength) {
    assertThatThrownBy(() -> service.truncate("hello", maxLength))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(String.valueOf(maxLength));
  }

  @Test
  @DisplayName("入力が空でも上限 0 は例外（引数の検証を先に行う）")
  void truncateRejectsZeroMaxEvenForEmptyInput() {
    assertThatThrownBy(() -> service.truncate("", 0)).isInstanceOf(IllegalArgumentException.class);
  }
}
