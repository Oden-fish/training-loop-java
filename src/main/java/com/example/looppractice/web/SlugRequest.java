package com.example.looppractice.web;

import jakarta.validation.constraints.NotBlank;

/** POST /api/slugs のリクエストボディ。 */
public record SlugRequest(@NotBlank String text) {}
