package com.example.looppractice.text;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** 練習用の文字列ユーティリティ。ここに機能を足していく。 */
@Service
public class TextService {

  private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

  /** 切り詰めたことを示す記号。U+2026 HORIZONTAL ELLIPSIS。 */
  private static final String ELLIPSIS = "\u2026";

  /**
   * 文字列を URL に使える slug に変換する。
   *
   * <p>英数字以外はハイフンにまとめ、前後のハイフンを落とす。非 ASCII 文字は slug に残らないため、変換結果が空になる入力のうち非 ASCII
   * 文字を含むものは例外にする。空文字を返しても呼び出し側は URL を組み立てられず、失敗に気づけないまま空の URL を作ってしまうため。ASCII だけで構成された入力（{@code
   * ""} や {@code "!!!"}）は従来どおり空文字を返す。
   *
   * @throws IllegalArgumentException 非 ASCII 文字を含み、かつ slug が空になるとき
   */
  public String slugify(String value) {
    String lowered = value.toLowerCase().strip();
    String replaced = NON_ALNUM.matcher(lowered).replaceAll("-");
    String slug = replaced.replaceAll("^-+|-+$", "");
    if (slug.isEmpty() && hasNonAscii(value)) {
      throw new IllegalArgumentException("cannot build a slug from non-ASCII input: " + value);
    }
    return slug;
  }

  /** ASCII 以外の文字を含むか。非 ASCII は slug に残らないため、slug が空になった原因の切り分けに使う。 */
  private static boolean hasNonAscii(String value) {
    return value.chars().anyMatch(codeUnit -> codeUnit > 0x7f);
  }

  /**
   * 文字列を指定長で切り詰める。
   *
   * <p>長さが {@code maxLength} 以下ならそのまま返す。超える場合は末尾を {@code …}（U+2026）に置き換え、戻り値全体が {@code maxLength}
   * ちょうどになるようにする。長さは UTF-16 コード単位で数えるため、末尾がサロゲートペアにかかる場合は分断される。
   *
   * @param maxLength 戻り値の上限。{@code …} を収められない 1 未満は不正
   * @throws IllegalArgumentException {@code maxLength} が 1 未満のとき
   */
  public String truncate(String value, int maxLength) {
    if (maxLength < 1) {
      throw new IllegalArgumentException("maxLength must be >= 1 but was " + maxLength);
    }
    if (value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength - ELLIPSIS.length()) + ELLIPSIS;
  }

  /**
   * コレクションの要素を、キー関数の戻り値ごとにまとめる。
   *
   * <p>グループ内の要素は元のコレクションの順序を保ち、キーも初出順に並ぶ。{@code Collectors#groupingBy} の 1 引数版は {@code HashMap}
   * を返してキーの順序が不定になるため、順序を再現可能にする目的で {@code LinkedHashMap} を明示している。戻り値の可変性は保証しない。
   *
   * @param keyFunction 要素からキーを取り出す関数。{@code null} を返してはいけない
   * @throws NullPointerException 引数が {@code null} のとき、またはキー関数が {@code null} を返したとき
   */
  public <T, K> Map<K, List<T>> groupBy(
      Collection<T> values, Function<? super T, ? extends K> keyFunction) {
    Objects.requireNonNull(values, "values must not be null");
    Objects.requireNonNull(keyFunction, "keyFunction must not be null");
    return values.stream()
        .collect(Collectors.groupingBy(keyFunction, LinkedHashMap::new, Collectors.toList()));
  }
}
