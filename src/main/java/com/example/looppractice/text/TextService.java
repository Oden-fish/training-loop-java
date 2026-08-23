package com.example.looppractice.text;

import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** 練習用の文字列ユーティリティ。ここに機能を足していく。 */
@Service
public class TextService {

  private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

  /** 文字列を URL に使える slug に変換する。 */
  public String slugify(String value) {
    String lowered = value.toLowerCase().strip();
    String replaced = NON_ALNUM.matcher(lowered).replaceAll("-");
    return replaced.replaceAll("^-+|-+$", "");
  }
}
