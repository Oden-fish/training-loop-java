package com.example.looppractice.web;

import com.example.looppractice.text.TextService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
