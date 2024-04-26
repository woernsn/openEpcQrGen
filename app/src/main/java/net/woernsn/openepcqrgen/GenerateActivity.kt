package net.woernsn.openepcqrgen

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlin.concurrent.thread

class GenerateActivity : AppCompatActivity() {

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
        PreferenceManager.setDefaultValues(this, R.xml.profiles_preferences, false)
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putBoolean("profile1_enabled", true)
        }

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

        val noProfileMessage = findViewById<TextView>(R.id.noProfileMessage)
        noProfileMessage.visibility = View.INVISIBLE

        val textInput = findViewById<TextInputLayout>(R.id.textInput)
        val amountInput = findViewById<TextInputLayout>(R.id.amountInput)
        val profileRadioGroup = findViewById<RadioGroup>(R.id.radioGroup).setOnCheckedChangeListener { _, _ ->
            imageViewQrCode.visibility = View.INVISIBLE
            textInput.editText!!.text.clear()
            amountInput.editText!!.text.clear()
        }

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

                val activeProfile = getActiveProfile()
                val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

                val name = defaultPreferences.getString("profile${activeProfile}_name", null)
                val iban = defaultPreferences.getString("profile${activeProfile}_iban", null)
                val bic = defaultPreferences.getString("profile${activeProfile}_bic", "")
                val amount = amountInput.editText!!.text.toString().toDouble()
                val text = textInput.editText!!.text.toString()

                val epcData = EPCData(
                    amount = amount,
                    text = text,
                    name = name!!,
                    iban = iban!!,
                    bic = bic!!
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

    override fun onResume() {
        super.onResume()
        checkForValidProfile()
        setRadioButtonNames()
    }

    private fun setRadioButtonNames() {
        val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val profileButtons = arrayOf<RadioButton>(
            findViewById(R.id.radio_profile1),
            findViewById(R.id.radio_profile2),
            findViewById(R.id.radio_profile3),
        )

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val defaultProfileButton = profileButtons[0]

        for (profileNumber in 1 .. 3) {
            val profileButton = profileButtons[profileNumber-1]

            val name = defaultPreferences.getString("profile${profileNumber}_name", null)
            val isEnabled = defaultPreferences.getBoolean("profile${profileNumber}_enabled", false)

            // check if profile is enabled and profile name is set
            if (isEnabled && !name.isNullOrEmpty()) {
                profileButton.text = name
                profileButton.isVisible = true
            } else {
                // check if this one was used before being invalid
                if (profileButton.isChecked) {
                    defaultProfileButton.isChecked = true
                }

                profileButton.isVisible = false
            }
        }
    }

    private fun checkForValidProfile(): Boolean {
        val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val noProfileMessage = findViewById<TextView>(R.id.noProfileMessage)
        val textInput = findViewById<TextInputLayout>(R.id.textInput)
        val amountInput = findViewById<TextInputLayout>(R.id.amountInput)

        try {
            EPCData(
                name = defaultPreferences.getString("profile1_name", null)!!,
                iban = defaultPreferences.getString("profile1_iban", null)!!,
                bic = defaultPreferences.getString("profile1_bic", "")!!,
                text = "init",
                amount = 1.0
            )

            noProfileMessage.visibility = View.INVISIBLE
            textInput.isEnabled = true
            amountInput.isEnabled = true

            return true
        } catch (e: Exception) {
            noProfileMessage.visibility = View.VISIBLE
            textInput.isEnabled = false
            amountInput.isEnabled = false

            return false
        }
    }

    private fun getActiveProfile(): Number {
        val profileButtons = arrayOf<RadioButton>(
            findViewById(R.id.radio_profile1),
            findViewById(R.id.radio_profile2),
            findViewById(R.id.radio_profile3),
        )
        return profileButtons.indexOfFirst { it.isChecked } + 1
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
}