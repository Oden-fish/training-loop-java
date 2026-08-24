package com.example.looppractice.web;

import com.example.looppractice.history.SlugHistory;
import com.example.looppractice.text.TextService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** slug 変換 API。 */
@RestController
@RequestMapping("/api/slugs")
public class SlugController {

  private final TextService textService;

  private final SlugHistory history;

  public SlugController(TextService textService, SlugHistory history) {
    this.textService = textService;
    this.history = history;
  }

  /** 受け取った文字列を slug に変換して返し、履歴に残す。 */
  @PostMapping
  public ResponseEntity<SlugResponse> create(@Valid @RequestBody SlugRequest request) {
    String slug = textService.slugify(request.text());
    history.record(slug);
    return ResponseEntity.status(HttpStatus.CREATED).body(new SlugResponse(slug));
  }

  /** 直近に変換した slug を新しい順で返す。 */
  @GetMapping("/recent")
  public ResponseEntity<RecentSlugsResponse> recent() {
    return ResponseEntity.ok(new RecentSlugsResponse(history.recent()));
  }

  /**
   * slug を作れない text を 400 に写像する。
   *
   * <p>{@code TextService#slugify} は slug にできない入力を {@code IllegalArgumentException} で弾く。 素通しすると既定の
   * 500 になり、サーバ側の障害と区別がつかなくなるため、ここで入力エラーとして返す。 レスポンスボディの形は #20 で ProblemDetail
   * に揃える予定なので、ここではステータスだけを扱う。
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Void> handleInvalidText() {
    return ResponseEntity.badRequest().build();
  }
}
