package com.example.looppractice.text;

import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** 練習用の文字列ユーティリティ。ここに機能を足していく。 */
@Service
public class TextService {

  private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

  /** 切り詰めたことを示す記号。U+2026 HORIZONTAL ELLIPSIS。 */
  private static final String ELLIPSIS = "\u2026";

  /** 文字列を URL に使える slug に変換する。 */
  public String slugify(String value) {
    String lowered = value.toLowerCase().strip();
    String replaced = NON_ALNUM.matcher(lowered).replaceAll("-");
    return replaced.replaceAll("^-+|-+$", "");
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
}
