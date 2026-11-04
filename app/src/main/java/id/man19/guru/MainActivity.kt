package id.man19.guru

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.man19.guru.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val web by lazy { supportFragmentManager.findFragmentByTag("web") as? WebFragment ?: WebFragment() }
    private val ai by lazy { supportFragmentManager.findFragmentByTag("ai") as? AiFragment ?: AiFragment() }
    private var guruUnlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, web, "web")
                .add(R.id.container, ai, "ai").hide(ai)
                .commit()
        }

        b.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_web -> { show(web); true }
                R.id.nav_ai -> {
                    if (guruUnlocked) { show(ai); true } else { askPin(); false }
                }
                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (web.isVisible && web.goBack()) return
                if (ai.isVisible) { b.bottomNav.selectedItemId = R.id.nav_web; return }
                finish()
            }
        })
    }

    private fun show(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(if (f === web) ai else web).show(f).commit()
    }

    /** Gerbang sederhana: tab AI hanya untuk guru yang tahu PIN. */
    private fun askPin() {
        val input = EditText(this).apply {
            hint = "PIN guru"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            setPadding(48, 32, 48, 32)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Khusus Guru")
            .setMessage("Masukkan PIN guru untuk membuka AI Guru.")
            .setView(input)
            .setPositiveButton("Buka") { _, _ ->
                if (input.text.toString().trim().equals(BuildConfig.GURU_PIN, ignoreCase = true)) {
                    guruUnlocked = true
                    b.bottomNav.selectedItemId = R.id.nav_ai
                } else {
                    Toast.makeText(this, "PIN salah", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
