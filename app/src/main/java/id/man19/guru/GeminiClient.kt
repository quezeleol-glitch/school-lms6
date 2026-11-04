package id.man19.guru

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Msg(val fromUser: Boolean, val text: String)

object GeminiClient {

    private const val SYSTEM_PROMPT = """
Kamu adalah "AI Guru", asisten untuk guru MAN 19. Jawab dalam Bahasa Indonesia yang jelas dan ringkas.
Tugasmu membantu guru: membuat kunci jawaban dan pembahasan soal, menyusun soal (PG/esai) sesuai
kurikulum Merdeka, mengoreksi dan memberi nilai jawaban esai siswa dengan rubrik, serta menyusun
balasan yang sopan untuk pertanyaan siswa/orang tua. Untuk soal PG, tulis jawaban + alasan singkat.
Jika soal ambigu, sebutkan asumsimu.
"""

    suspend fun ask(history: List<Msg>): String = withContext(Dispatchers.IO) {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isBlank()) return@withContext "API key Gemini belum diisi. Isi GEMINI_API_KEY lalu build ulang."

        val contents = JSONArray()
        history.takeLast(20).forEach { m ->
            contents.put(JSONObject()
                .put("role", if (m.fromUser) "user" else "model")
                .put("parts", JSONArray().put(JSONObject().put("text", m.text))))
        }
        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT.trim()))))
            .put("contents", contents)
            .put("generationConfig", JSONObject().put("temperature", 0.4))

        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/${BuildConfig.GEMINI_MODEL}:generateContent")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("x-goog-api-key", key)
        }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val ok = conn.responseCode in 200..299
            val raw = (if (ok) conn.inputStream else conn.errorStream)?.bufferedReader()?.readText().orEmpty()
            if (!ok) return@withContext "Gagal (${conn.responseCode}). ${raw.take(300)}"
            val parts = JSONObject(raw).getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts")
            buildString { for (i in 0 until parts.length()) append(parts.getJSONObject(i).optString("text")) }
                .ifBlank { "(AI tidak memberi jawaban)" }
        } catch (e: Exception) {
            "Koneksi gagal: ${e.message}"
        } finally {
            conn.disconnect()
        }
    }
}
