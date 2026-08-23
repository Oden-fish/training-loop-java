#!/usr/bin/env bash
# ローカルと CI で共有する唯一の検証コマンド。
#   ./scripts/verify.sh          整形チェック + Checkstyle + テスト + カバレッジ下限
#   ./scripts/verify.sh --fast   テストだけ（実装ループ中の高速確認用）
#   ./scripts/verify.sh --fix    Spotless で整形してから全部走らせる
set -euo pipefail
cd "$(dirname "$0")/.."

case "${1:-}" in
  --fast)
    ./gradlew --console=plain test
    ;;
  --fix)
    ./gradlew --console=plain spotlessApply check
    ;;
  *)
    # check = spotlessCheck + checkstyleMain/Test + test + jacocoTestCoverageVerification
    ./gradlew --console=plain check
    ;;
esac

echo "OK"
