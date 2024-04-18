package net.woernsn.openepcqrgen

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os. Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    class HeaderFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)
        }
    }

    class ProfilesFragment : PreferenceFragmentCompat() {

        override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
            super.onNavigateToScreen(preferenceScreen)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            return super.onPreferenceTreeClick(preference)
        }

        override fun onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment: Boolean) {
            super.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment)
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
        }

        override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
            super.onInflate(context, attrs, savedInstanceState)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

            // check for enabled profiles
            for (profileNumber in 2..3) {
                val isEnabled = preferenceManager.sharedPreferences!!.getBoolean("profile${profileNumber}_enabled", false)
                arrayOf("name", "iban").forEach { setting ->
                    val pref = findPreference<EditTextPreference>("profile${profileNumber}_${setting}")
                    pref?.isEnabled = isEnabled
                }
            }

            setPreferencesFromResource(R.xml.profiles_preferences, rootKey)

        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
        }

        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            super.onCreateOptionsMenu(menu, inflater)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        override fun onCreateLayoutManager(): RecyclerView.LayoutManager {
            return super.onCreateLayoutManager()
        }

        override fun onCreateRecyclerView(
            inflater: LayoutInflater,
            parent: ViewGroup,
            savedInstanceState: Bundle?
        ): RecyclerView {
            return super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        }
    }

    class GeneralFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.general_preferences, rootKey)
        }
    }
}