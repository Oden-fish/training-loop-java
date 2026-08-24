package com.example.looppractice.web;

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

  public SlugController(TextService textService) {
    this.textService = textService;
  }

  /** 受け取った文字列を slug に変換して返す。 */
  @PostMapping
  public ResponseEntity<SlugResponse> create(@Valid @RequestBody SlugRequest request) {
    SlugResponse body = new SlugResponse(textService.slugify(request.text()));
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
  }

  /**
   * クエリで受け取った文字列を slug に変換して返す。
   *
   * <p>引数を {@code @RequestParam} にせず {@code @ModelAttribute}（アノテーション無し）で束縛しているのは、 エラー形式を POST
   * と揃えるため。{@code @RequestParam} だと text 未指定は {@code MissingServletRequestParameterException}
   * になり、ボディなしの 400 に落ちる。 {@code @ModelAttribute} なら未指定・空文字のどちらも {@code @NotBlank} 違反として {@code
   * MethodArgumentNotValidException} に合流し、既存の ProblemDetail 変換にそのまま乗る。
   */
  @GetMapping
  public SlugResponse get(@Valid SlugRequest query) {
    return new SlugResponse(textService.slugify(query.text()));
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
