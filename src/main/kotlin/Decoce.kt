package burpee

import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

object Decode {
    private val utilities = Api.api.utilities()
    private val base64Utils = utilities.base64Utils()
    private val urlUtils = utilities.urlUtils()

    fun decodeIfJWT(input: String): String {
        // "Bearer "で始まるかどうかチェック
        val hasBearer = input.startsWith("Bearer ")
        // "Bearer "で始まる場合はその部分を取り除く
        val token = if (hasBearer) {
            input.substring(7)
        } else {
            input
        }

        val decoded = token.takeUnless { it.startsWith("eyJ") }
            ?: token.split(".")
                .takeIf { it.size == 3 }
                ?.let { parts ->
                    try {
                        val header = base64Utils.decode(parts[0]).toString()
                        val payload = base64Utils.decode(parts[1]).toString()
                        "$header.$payload"
                    } catch (e: Exception) {
                        token
                    }
                } ?: token

        // 元々"Bearer "が付いていた場合は、デコード結果にも付ける
        return if (hasBearer) {
            "Bearer $decoded"
        } else {
            decoded
        }
    }

    private fun isUrlEncoded(input: String): Boolean {
        val regex = "%[0-9A-Fa-f]{2}".toRegex()

        return input.contains("%") &&
                regex.containsMatchIn(input) &&
                regex.findAll(input).all { match -> match.range.last - match.range.first == 2 }
    }

    fun decodeUrlEncoded(input: String): String =
        when {
            isUrlEncoded(input) ->
                try {
                    urlUtils.decode(input)
                } catch (e: IllegalArgumentException) {
                    Api.log("\r\nCaught IllegalArgumentException in function decodeIfNeeded\r\n")
                    input
                }
            else -> input
        }

    private fun isBase64Encoded(input: String): Boolean =
        input.length % 4 == 0 && "^[A-Za-z0-9+/]*={0,2}$".toRegex().matches(input)
    fun decodeBase64(input: String): String =
        input.takeIf { !isBase64Encoded(it) } ?:
        try {
            val decodedBytes = Base64.getDecoder().decode(input)
            String(decodedBytes, StandardCharsets.UTF_8).takeIf { it.toByteArray(StandardCharsets.UTF_8).contentEquals(decodedBytes) } ?: input
        } catch (e: IllegalArgumentException) {
            input
        }

    fun convertTimestamp(input: String): String {
        val trimmed = input.trim()

        if (!trimmed.matches(Regex("^[1-9][0-9]{8,18}$"))) {
            return input
        }

        val number = trimmed.toLong()

        // タイムスタンプとして妥当な範囲かどうかチェック
        return when {
            // 秒単位: 9-10桁で1970年～2050年の範囲内
            trimmed.length in 9..10 && number >= 0 && number <= 2524608000L -> {
                formatDate(number * 1000) + " (Unix seconds)"
            }
            // ミリ秒単位: 12-13桁で1970年～2050年の範囲内
            trimmed.length in 12..13 && number >= 0 && number <= 2524608000000L -> {
                formatDate(number) + " (Unix milliseconds)"
            }
            // マイクロ秒単位: 15-16桁
            trimmed.length in 15..16 && number / 1000 >= 0 && number / 1000 <= 2524608000000L -> {
                formatDate(number / 1000) + " (Unix microseconds)"
            }
            // それ以外は単なる数値と判断
            else -> input
        }
    }

    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter.format(date)
    }

    /**
     * まとめデコード関数。指定された順番で各デコード関数を適用します。
     * 1. JWT デコード
     * 2. URL エンコードデコード
     * 3. Base64 デコード
     * 4. TimeStamp
     * デコードに失敗した場合は元の値が返されます。
     */
    fun autoDecode(input: String): String {
        return input
            .let { decodeUrlEncoded(it) }
            .let { decodeUrlEncoded(it) }
            .let { decodeIfJWT(it) }
            .let { convertTimestamp(it) }
            .let { decodeBase64(it) }
    }

    /**
     * 指定された順番でデコード処理を行う関数です。
     * 変換前後の値に変化があった場合のみ次のデコード処理に進みます。
     * @param input デコードする文字列
     * @param decoders 適用するデコード関数のリスト（適用順）
     * @return デコード結果の文字列
     */
    fun decodeWithOrder(input: String, decoders: List<(String) -> String>): String {
        return decoders.fold(input) { acc, decoder ->
            val decoded = decoder(acc)
            if (decoded != acc) decoded else acc
        }
    }

}