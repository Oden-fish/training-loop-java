package com.example.looppractice.web;

import java.util.List;
import java.util.Map;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** リクエストボディ / クエリのバリデーションエラーを RFC 9457 の ProblemDetail に写像する。 */
@RestControllerAdvice
public class ValidationExceptionHandler {

  /**
   * 制約違反を、どのフィールドがなぜ弾かれたかが分かる ProblemDetail に変換する。
   *
   * <p>既定では {@code MethodArgumentNotValidException} はボディなしの 400 になり、 呼び出し側は「何が悪かったのか」を推測するしかない。
   * 例外が持つ {@code ProblemDetail}（status と detail が入っている）に、フィールドごとの内訳を {@code errors} として足して返す。
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationFailure(MethodArgumentNotValidException ex) {
    List<Map<String, String>> errors =
        ex.getFieldErrors().stream()
            .map(
                error ->
                    Map.of(
                        "field",
                        error.getField(),
                        "message",
                        String.valueOf(error.getDefaultMessage())))
            .toList();

    ProblemDetail body = ex.getBody();
    body.setProperty("errors", errors);
    return body;
  }
}
