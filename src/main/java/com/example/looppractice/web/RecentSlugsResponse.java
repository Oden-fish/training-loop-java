package com.example.looppractice.web;

import java.util.List;

/** GET /api/slugs/recent のレスポンスボディ。 */
public record RecentSlugsResponse(List<String> slugs) {}
