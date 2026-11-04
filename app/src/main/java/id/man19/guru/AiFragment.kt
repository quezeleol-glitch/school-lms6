package id.man19.guru

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import id.man19.guru.databinding.FragmentAiBinding
import kotlinx.coroutines.launch

class AiFragment : Fragment() {

    private var _b: FragmentAiBinding? = null
    private val b get() = _b!!
    private val history = mutableListOf<Msg>()
    private var busy = false

    private val presets = listOf(
        "Kunci jawaban" to "Buatkan kunci jawaban + pembahasan singkat untuk soal berikut:\n",
        "Koreksi esai" to "Koreksi dan beri nilai (0-100) jawaban esai siswa ini dengan rubrik singkat.\nSoal:\n\nJawaban siswa:\n",
        "Balas siswa" to "Buatkan balasan sopan dan jelas untuk pertanyaan siswa ini:\n",
        "Buat soal" to "Buatkan 5 soal pilihan ganda HOTS beserta kunci jawaban untuk materi:\n"
    )

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAiBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        presets.forEach { (label, template) ->
            b.chips.addView(Chip(requireContext()).apply {
                text = label
                setOnClickListener {
                    b.input.setText(template)
                    b.input.setSelection(b.input.text.length)
                    b.input.requestFocus()
                }
            })
        }
        if (history.isEmpty()) {
            addBubble(Msg(false, "Halo, Bapak/Ibu guru! Tempel soal, jawaban siswa, atau pertanyaan siswa, nanti saya jawab otomatis. Tekan lama pesan untuk menyalin."), save = false)
        } else history.forEach { addBubble(it, save = false) }
        b.send.setOnClickListener { send() }
    }

    private fun send() {
        val text = b.input.text.toString().trim()
        if (text.isEmpty() || busy) return
        b.input.setText("")
        addBubble(Msg(true, text))
        busy = true
        b.send.isEnabled = false
        val typing = addBubble(Msg(false, "Sedang menjawab..."), save = false)
        viewLifecycleOwner.lifecycleScope.launch {
            val reply = GeminiClient.ask(history)
            _b?.messages?.removeView(typing)
            addBubble(Msg(false, reply))
            busy = false
            _b?.send?.isEnabled = true
        }
    }

    private fun addBubble(m: Msg, save: Boolean = true): View {
        if (save) history.add(m)
        val ctx = requireContext()
        val tv = TextView(ctx).apply {
            text = m.text
            textSize = 16f
            setTextIsSelectable(false)
            setPadding(36, 24, 36, 24)
            setTextColor(ContextCompat.getColor(ctx, if (m.fromUser) R.color.white else R.color.text_dark))
            background = ContextCompat.getDrawable(ctx, if (m.fromUser) R.drawable.bubble_user else R.drawable.bubble_ai)
            setOnLongClickListener {
                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("AI Guru", m.text))
                Toast.makeText(ctx, "Disalin", Toast.LENGTH_SHORT).show()
                true
            }
        }
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = if (m.fromUser) Gravity.END else Gravity.START
            topMargin = 16
            if (m.fromUser) marginStart = 96 else marginEnd = 96
        }
        b.messages.addView(tv, lp)
        b.scroll.post { _b?.scroll?.fullScroll(View.FOCUS_DOWN) }
        return tv
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}
