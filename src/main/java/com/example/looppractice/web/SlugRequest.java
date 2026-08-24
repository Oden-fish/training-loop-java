package com.example.looppractice.web;

import jakarta.validation.constraints.NotBlank;

/** POST /api/slugs のリクエストボディ、および GET /api/slugs のクエリパラメータ。 */
public record SlugRequest(@NotBlank String text) {}
