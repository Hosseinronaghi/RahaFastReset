package ir.raha.fastreset

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity

/** All potentially destructive actions originate only from the foreground Activity. */
class MainActivity : AppCompatActivity() {
    private lateinit var controller: ResetController
    private lateinit var status: TextView
    private lateinit var description: TextView
    private lateinit var primaryButton: MaterialButton
    private lateinit var oneTapCheck: MaterialCheckBox
    private val prefs by lazy { getSharedPreferences("local_preferences", MODE_PRIVATE) }
    private var armed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_RahaFastReset)
        setContentView(R.layout.activity_main)
        controller = ResetController(this)
        status = findViewById(R.id.status)
        description = findViewById(R.id.description)
        primaryButton = findViewById(R.id.primary_button)
        oneTapCheck = findViewById(R.id.one_tap)
        findViewById<TextView>(R.id.build_variant).text = getString(R.string.build_info, getString(R.string.variant_label), Build.VERSION.RELEASE)
        oneTapCheck.isChecked = prefs.getBoolean("one_tap_enabled", false)
        oneTapCheck.setOnCheckedChangeListener { _, checked ->
            if (checked && !armed) {
                oneTapCheck.isChecked = false
                showOneTapConsent()
            } else if (!checked) {
                prefs.edit().putBoolean("one_tap_enabled", false).apply()
            }
        }
        primaryButton.setOnClickListener { handlePrimaryClick() }
        findViewById<MaterialButton>(R.id.settings_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_PRIVACY_SETTINGS))
        }
        findViewById<MaterialButton>(R.id.disable_admin_button).setOnClickListener {
            if (controller.adminActive && !controller.deviceOwner) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.disable_admin_title)
                    .setMessage(R.string.disable_admin_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.disable_admin) { _, _ ->
                        try {
                            (getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager)
                                .removeActiveAdmin(controller.admin)
                        } catch (e: Exception) { showError(e) }
                        refresh()
                    }.show()
            } else if (controller.deviceOwner) {
                MaterialAlertDialogBuilder(this).setTitle(R.string.managed_warning_title)
                    .setMessage(R.string.managed_warning).setPositiveButton(R.string.ok, null).show()
            }
        }
        refresh()
    }

    override fun onResume() { super.onResume(); if (::controller.isInitialized) refresh() }

    private fun refresh() {
        when (controller.availability()) {
            ResetAvailability.Ready -> {
                status.setText(R.string.ready)
                description.setText(R.string.ready_description)
                primaryButton.setText(R.string.reset_button)
                primaryButton.isEnabled = true
            }
            ResetAvailability.NeedsAdmin -> {
                status.setText(R.string.admin_missing)
                description.setText(R.string.admin_instructions)
                primaryButton.setText(R.string.enable_admin)
                primaryButton.isEnabled = true
            }
            ResetAvailability.NeedsDeviceOwner -> {
                status.setText(R.string.owner_missing)
                description.setText(R.string.owner_instructions)
                primaryButton.setText(R.string.owner_missing)
                primaryButton.isEnabled = false
            }
            ResetAvailability.NotPrimaryUser -> {
                status.setText(R.string.not_primary)
                description.setText(R.string.not_primary_description)
                primaryButton.isEnabled = false
            }
            ResetAvailability.NotSupported -> {
                status.setText(R.string.not_supported)
                primaryButton.isEnabled = false
            }
        }
    }

    private fun handlePrimaryClick() {
        when (controller.availability()) {
            ResetAvailability.NeedsAdmin -> {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, controller.admin)
                    .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.admin_explanation))
                try { startActivity(intent) } catch (e: Exception) { showError(e) }
            }
            ResetAvailability.Ready -> {
                if (prefs.getBoolean("one_tap_enabled", false)) executeReset()
                else showFinalConfirmation()
            }
            else -> refresh()
        }
    }

    private fun showOneTapConsent() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.one_tap_consent_title)
            .setMessage(R.string.one_tap_consent_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.enable_one_tap) { _, _ ->
                armed = true
                prefs.edit().putBoolean("one_tap_enabled", true).apply()
                oneTapCheck.isChecked = true
                armed = false
            }.show()
    }

    private fun showFinalConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.final_warning_title)
            .setMessage(R.string.final_warning_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.erase_now) { _, _ -> executeReset() }
            .show()
    }

    private fun executeReset() {
        if (controller.availability() != ResetAvailability.Ready) { refresh(); return }
        primaryButton.isEnabled = false
        try {
            controller.execute()
            status.setText(R.string.reset_requested)
        } catch (e: SecurityException) {
            showError(e)
        } catch (e: IllegalStateException) {
            showError(e)
        } catch (e: RuntimeException) {
            showError(e)
        } finally {
            primaryButton.isEnabled = true
        }
    }

    private fun showError(e: Exception) {
        MaterialAlertDialogBuilder(this).setTitle(R.string.reset_failed)
            .setMessage(getString(R.string.reset_failed_message, e.javaClass.simpleName))
            .setPositiveButton(R.string.ok, null).show()
    }
}
