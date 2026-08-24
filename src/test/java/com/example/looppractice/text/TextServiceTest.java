package com.example.looppractice.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

  @DisplayName("非 ASCII を含み slug が空になる入力は例外にする（URL に使えない空文字を返さないため）")
  @ParameterizedTest(name = "[{index}] \"{0}\"")
  @ValueSource(
      strings = {
        "\u65e5\u672c\u8a9e\u306e\u30bf\u30a4\u30c8\u30eb",
        "\u65e5\u672c\u8a9e \u306e \u30bf\u30a4\u30c8\u30eb",
        "\u65e5\u672c\u8a9e!!!",
      })
  void slugifyRejectsNonAsciiOnly(String input) {
    assertThatThrownBy(() -> service.slugify(input))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(input);
  }

  @DisplayName("非 ASCII を含んでも ASCII 部分から slug を作れるならそのまま返す")
  @ParameterizedTest(name = "[{index}] \"{0}\" -> \"{1}\"")
  @CsvSource({
    "'\u65e5\u672c\u8a9e Title', title",
    "'caf\u00e9', caf",
  })
  void slugifyKeepsAsciiPartOfMixedInput(String input, String expected) {
    assertThat(service.slugify(input)).isEqualTo(expected);
  }

  @Test
  @DisplayName("ASCII の記号だけなら従来どおり空文字（例外にするのは非 ASCII を含むときだけ）")
  void slugifyAsciiOnlySymbolsStaysEmpty() {
    assertThat(service.slugify("!!!")).isEmpty();
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

  @Test
  @DisplayName("キー関数の戻り値ごとに要素をまとめる")
  void groupByGroupsElementsByKey() {
    Map<Character, List<String>> grouped =
        service.groupBy(List.of("apple", "avocado", "banana"), value -> value.charAt(0));

    assertThat(grouped)
        .containsOnlyKeys('a', 'b')
        .containsEntry('a', List.of("apple", "avocado"))
        .containsEntry('b', List.of("banana"));
  }

  @Test
  @DisplayName("空のコレクションは空のマップになる（null や例外にはしない）")
  void groupByReturnsEmptyMapForEmptyInput() {
    assertThat(service.groupBy(List.<String>of(), String::length)).isEmpty();
  }

  @Test
  @DisplayName("同じキーの要素は元のコレクションの順序のままグループ内に並ぶ")
  void groupByKeepsEncounterOrderWithinGroup() {
    Map<Character, List<String>> grouped =
        service.groupBy(List.of("b1", "a1", "b2", "a2", "b3"), value -> value.charAt(0));

    assertThat(grouped.get('b')).containsExactly("b1", "b2", "b3");
    assertThat(grouped.get('a')).containsExactly("a1", "a2");
  }

  @Test
  @DisplayName("キーの並びは初出順で安定する（HashMap の不定順に戻さないための番人）")
  void groupByKeepsFirstSeenKeyOrder() {
    Map<Character, List<String>> grouped =
        service.groupBy(List.of("b1", "a1", "b2", "a2", "b3"), value -> value.charAt(0));

    assertThat(grouped.keySet()).containsExactly('b', 'a');
  }

  @Test
  @DisplayName("キーの型は関数の戻り値から決まる（キャストなしで代入できる）")
  void groupByInfersTypesForDifferentKeyTypes() {
    Map<Integer, List<String>> byLength = service.groupBy(List.of("a", "bb", "cc"), String::length);
    Map<Boolean, List<Integer>> byParity =
        service.groupBy(List.of(1, 2, 3, 4), number -> number % 2 == 0);

    assertThat(byLength).containsEntry(2, List.of("bb", "cc"));
    assertThat(byParity).containsEntry(true, List.of(2, 4));
  }

  @Test
  @DisplayName("コレクションが null なら、どの引数が null かが分かる NullPointerException にする")
  void groupByRejectsNullValues() {
    assertThatThrownBy(() -> service.<String, Integer>groupBy(null, String::length))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("values");
  }

  @Test
  @DisplayName("キー関数が null なら、どの引数が null かが分かる NullPointerException にする")
  void groupByRejectsNullKeyFunction() {
    assertThatThrownBy(() -> service.<String, Integer>groupBy(List.of("a"), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("keyFunction");
  }

  @Test
  @DisplayName("キー関数が null を返したら NullPointerException（null キーは許容しない）")
  void groupByRejectsNullKey() {
    Function<String, Character> nullKeyFunction = value -> null;

    assertThatThrownBy(() -> service.groupBy(List.of("a"), nullKeyFunction))
        .isInstanceOf(NullPointerException.class);
  }
}
