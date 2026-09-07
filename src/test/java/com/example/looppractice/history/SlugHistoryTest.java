package com.example.looppractice.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SlugHistoryTest {

  private final SlugHistory history = new SlugHistory();

  @Test
  @DisplayName("記録した slug を新しい順で返す")
  void returnsNewestFirst() {
    history.record("first");
    history.record("second");
    history.record("third");

    assertThat(history.recent()).containsExactly("third", "second", "first");
  }

  @Test
  @DisplayName("上限を超えて記録すると、古いものから落ちて最新 20 件だけが残る")
  void keepsOnlyTheNewestTwentyEntries() {
    for (int i = 1; i <= 21; i++) {
      history.record("slug-" + i);
    }

    assertThat(history.recent()).hasSize(20).startsWith("slug-21").endsWith("slug-2");
    assertThat(history.recent()).doesNotContain("slug-1");
  }

  @Test
  @DisplayName("同じ slug を複数回記録しても、重複は取り除かない")
  void keepsDuplicates() {
    history.record("same");
    history.record("same");

    assertThat(history.recent()).containsExactly("same", "same");
  }

  @Test
  @DisplayName("1件も記録していなければ空リストを返す")
  void returnsEmptyListWhenNothingRecorded() {
    assertThat(history.recent()).isEmpty();
  }

  @Test
  @DisplayName("返したリストは変更できない（呼び出し側が履歴を書き換えられない）")
  void returnsUnmodifiableList() {
    history.record("slug");

    List<String> recent = history.recent();

    assertThatThrownBy(() -> recent.add("injected"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @DisplayName("複数スレッドから同時に記録しても、件数と中身が壊れない")
  void survivesConcurrentRecording() throws Exception {
    int threads = 8;
    int perThread = 100;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      for (int t = 0; t < threads; t++) {
        int threadId = t;
        pool.execute(
            () -> {
              for (int i = 0; i < perThread; i++) {
                history.record("t" + threadId + "-" + i);
              }
            });
      }
    } finally {
      pool.shutdown();
      assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
    }

    assertThat(history.recent()).hasSize(20).doesNotContainNull();
  }
}
