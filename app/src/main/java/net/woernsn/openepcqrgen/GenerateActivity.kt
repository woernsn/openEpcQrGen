package net.woernsn.openepcqrgen

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlin.concurrent.thread

class GenerateActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.topbarmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            startActivity(Intent(this@GenerateActivity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // support dynamic colors
        DynamicColors.applyToActivitiesIfAvailable(this.application)

        // set content!
        setContentView(R.layout.activity_generate)

        // default prefs
        PreferenceManager.setDefaultValues(this, R.xml.general_preferences, false)
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)

        // debug
        val mytheme = PreferenceManager.getDefaultSharedPreferences(this)
        println(mytheme.getString("theme", "none"))

        // add toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.materialToolBar)
        setSupportActionBar(toolbar)
        toolbar.inflateMenu(R.menu.topbarmenu)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imageViewQrCode = findViewById<ImageView>(R.id.imageViewQrCode)
        imageViewQrCode.visibility = View.INVISIBLE

        val textInput = findViewById<TextInputLayout>(R.id.textInput)
        val amountInput = findViewById<TextInputLayout>(R.id.amountInput)

        val view = findViewById<View>(R.id.main)

        val themeAccentColor = resources.getColor(R.color.md_theme_secondary, theme)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if ((amountInput.editText == null || amountInput.editText!!.text.isNullOrEmpty()) ||
                    (textInput.editText == null || textInput.editText!!.text.isNullOrEmpty())){
                    imageViewQrCode.visibility = View.INVISIBLE
                    return
                }

                imageViewQrCode.visibility = View.VISIBLE

                val amount = amountInput.editText!!.text.toString().toDouble()
                val text = textInput.editText!!.text.toString()

                val epcData = EPCData(
                    amount = amount,
                    text = text,
                    iban = "AT52 1919 0000 5505 0363",
                    name = "Werner Kapferer"
                )

                thread {

                    view.post {
                        imageViewQrCode.setImageBitmap(
                            generateQrCode(epcData, imageViewQrCode.width, imageViewQrCode.height, themeAccentColor)
                        )
                    }

                }
            }

        }

        textInput.editText?.addTextChangedListener(textWatcher)
        amountInput.editText?.addTextChangedListener(textWatcher)
    }

    private fun generateQrCode(epcData: EPCData, width: Int, height: Int, themeAccentColor: Int): Bitmap {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(epcData.toString(), BarcodeFormat.QR_CODE, width, height)

        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (bitMatrix[x, y]) themeAccentColor else Color.TRANSPARENT
            }
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)

        return bitmap
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        // set theme
        if (p1 == "theme") {
            val man = AppCompatDelegate.setDefaultNightMode(when (p0!!.getString("theme", "auto")) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            })
        }
    }
}