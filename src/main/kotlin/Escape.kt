package burpee

object Escape {
    fun escapeControlCharacters(input: String): String {
        return buildString {
            for (char in input) {
                append(
                    when (char) {
                        '\n' -> "\\n"           // 改行
                        '\r' -> "\\r"           // キャリッジリターン
                        '\t' -> "\\t"           // タブ
                        '\u0000' -> "\\0"       // NULL文字
                        '\u0007' -> "\\a"       // ベル
                        '\u0008' -> "\\b"       // バックスペース
                        '\u000C' -> "\\f"       // フォームフィード
                        '\u001B' -> "\\e"       // ESC (エスケープ)
                        '\u001A' -> "\\x1A"    // SUB (0x1A)
                        else -> if (char.isISOControl()) {
                            "\\u%04x".format(char.code) // その他制御文字は Unicode 表記に
                        } else {
                            char
                        }
                    }
                )
            }
        }
    }
    fun removeNonPrintableChars(input: String): String {
        return input.filter { it.isDefined() && !it.isISOControl() }
    }

    fun isUrlUnsafe(input: String): Boolean {
        // URLセーフな文字以外を判定
        val urlSafeRegex = Regex("^[a-zA-Z0-9-._~]*$")
        return !urlSafeRegex.matches(input)
    }
}