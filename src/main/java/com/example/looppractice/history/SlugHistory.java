package com.example.looppractice.history;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 直近に変換した slug を新しい順で覚えておく。永続化はせず、プロセスが落ちれば消える。
 *
 * <p>コントローラはシングルトンで、複数のリクエストスレッドから同時に {@code record} が呼ばれる。「先頭に足してから上限超過分を捨てる」は複合操作なので、 素の {@code
 * ArrayDeque} のままだと件数が上限を超えたり要素を取りこぼしたりする。並行コレクションには「追加と切り詰めをまとめて不可分にする」手段が無いため、 両メソッドを {@code
 * synchronized} で囲って直列化している。
 */
@Component
public class SlugHistory {

  /** 覚えておく件数の上限。これを超えたら古いものから捨てる。 */
  private static final int MAX_ENTRIES = 20;

  private final Deque<String> entries = new ArrayDeque<>();

  /** slug を1件記録する。上限を超えた分は最も古いものから捨てる。 */
  public synchronized void record(String slug) {
    entries.addFirst(slug);
    if (entries.size() > MAX_ENTRIES) {
      entries.removeLast();
    }
  }

  /**
   * 記録した slug を新しい順で返す。
   *
   * <p>戻り値は変更不可のコピー。内部の {@code Deque} をそのまま渡すと、呼び出し側の反復中に別スレッドの {@code record} が割り込んで {@code
   * ConcurrentModificationException} になるため。
   */
  public synchronized List<String> recent() {
    return List.copyOf(entries);
  }
}
