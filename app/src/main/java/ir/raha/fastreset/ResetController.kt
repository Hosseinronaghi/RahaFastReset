package ir.raha.fastreset

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.UserManager

internal sealed interface ResetAvailability {
    data object Ready : ResetAvailability
    data object NeedsAdmin : ResetAvailability
    data object NeedsDeviceOwner : ResetAvailability
    data object NotPrimaryUser : ResetAvailability
    data object NotSupported : ResetAvailability
}

/** No network, root, hidden APIs or background wiping. */
internal class ResetController(private val context: Context) {
    private val policy = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val admin = ComponentName(context, RahaAdminReceiver::class.java)
    val adminActive: Boolean get() = policy.isAdminActive(admin)
    val deviceOwner: Boolean get() = policy.isDeviceOwnerApp(context.packageName)

    fun availability(): ResetAvailability {
        val users = context.getSystemService(Context.USER_SERVICE) as UserManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !users.isSystemUser) {
            return ResetAvailability.NotPrimaryUser
        }
        if (deviceOwner && Build.VERSION.SDK_INT >= 34) return ResetAvailability.Ready
        return if (adminActive) ResetAvailability.Ready else ResetAvailability.NeedsAdmin
    }

    /** Irreversible: call ONLY following the user's foreground tap and explicit prior consent. */
    @Suppress("DEPRECATION")
    fun execute() {
        check(availability() == ResetAvailability.Ready) { "Reset unavailable" }
        when {
            deviceOwner && Build.VERSION.SDK_INT >= 34 -> policy.wipeDevice(0)
            else -> policy.wipeData(0)
        }
    }
}
