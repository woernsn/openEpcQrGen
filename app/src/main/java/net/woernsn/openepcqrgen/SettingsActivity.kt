package net.woernsn.openepcqrgen

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doAfterTextChanged
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.color.DynamicColors

private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivitiesIfAvailable(this.application)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        setSupportActionBar(findViewById(R.id.materialToolBar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()

        return true
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment!!
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        title = pref.title

        return true
    }

    class HeaderFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)
        }
    }

    class ProfilesFragment : PreferenceFragmentCompat(), OnPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.profiles_preferences, rootKey)

            // check for enabled profiles
            for (profileNumber in 2..3) {
                val isEnabled = preferenceManager.sharedPreferences!!.getBoolean(
                    "profile${profileNumber}_enabled",
                    false
                )
                enableProfile(this, profileNumber, isEnabled)

                findPreference<SwitchPreference>("profile${profileNumber}_enabled")!!
                    .onPreferenceChangeListener = this
            }

            // set onPreferenceChangeListeners
            for (profileNumber in 1..3) {
                findPreference<EditTextPreference>("profile${profileNumber}_iban")!!
                    .setOnBindEditTextListener { editText ->
                        editText.doAfterTextChanged {
                            val okBtn = editText.rootView.findViewById<Button>(android.R.id.button1)
                            try {
                                EPCData(
                                    name = "",
                                    iban = editText.text.toString(),
                                    text = "validate",
                                    amount = 1.0
                                )

                                // enable okay button
                                okBtn.isEnabled = true
                            } catch (e: Exception) {
                                editText.error = e.message
                                okBtn.isEnabled = false
                            }
                        }
                    }
            }
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
            val profileRegex = """profile(\d)_enabled""".toRegex()
            val profileResult = profileRegex.find(preference.key)

            if (profileResult != null) {
                val profileNumber = profileResult.groups[1]!!.value.toInt()
                enableProfile(this@ProfilesFragment, profileNumber, newValue as Boolean)
            }

            return true
        }

        private fun enableProfile(
            context: PreferenceFragmentCompat,
            profileNumber: Number,
            enable: Boolean
        ) {
            arrayOf("name", "holder", "iban", "bic").forEach { setting ->
                val pref =
                    context.findPreference<EditTextPreference>("profile${profileNumber}_${setting}")
                pref?.isEnabled = enable
            }
        }
    }


    class GeneralFragment : PreferenceFragmentCompat(), OnPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.general_preferences, rootKey)
            findPreference<ListPreference>("theme")!!.onPreferenceChangeListener = this
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
            if (preference.key == "theme") {
                AppCompatDelegate.setDefaultNightMode(
                    when ((newValue as String)) {
                        "light" -> AppCompatDelegate.MODE_NIGHT_NO
                        "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                )
            }

            return true
        }
    }
}
