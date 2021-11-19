package com.matrix.autoreply.helpers

import android.os.Build
import android.widget.Toast
import com.matrix.autoreply.R
import android.content.DialogInterface
import com.matrix.autoreply.model.utils.CustomDialog
import android.os.Bundle
import kotlin.Throws
import android.content.Intent
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import com.matrix.autoreply.model.utils.Constants
import java.lang.Exception
import java.util.*

//Ref: https://stackoverflow.com/q/44383983
class AutoStartHelper private constructor() {
    /***
     * Xiaomi
     */
    private val BRAND_XIAOMI = "xiaomi"
    private val BRAND_XIAOMI_POCO = "poco"
    private val BRAND_XIAOMI_REDMI = "redmi"
    private val PACKAGE_XIAOMI_MAIN = "com.miui.securitycenter"
    private val PACKAGE_XIAOMI_COMPONENT = "com.miui.permcenter.autostart.AutoStartManagementActivity"

    /***
     * Letv
     */
    private val BRAND_LETV = "letv"
    private val PACKAGE_LETV_MAIN = "com.letv.android.letvsafe"
    private val PACKAGE_LETV_COMPONENT = "com.letv.android.letvsafe.AutobootManageActivity"

    /***
     * ASUS ROG
     */
    private val BRAND_ASUS = "asus"
    private val PACKAGE_ASUS_MAIN = "com.asus.mobilemanager"
    private val PACKAGE_ASUS_COMPONENT = "com.asus.mobilemanager.powersaver.PowerSaverSettings"
    private val PACKAGE_ASUS_COMPONENT_FALLBACK = "com.asus.mobilemanager.autostart.AutoStartActivity"

    /***
     * Honor
     */
    private val BRAND_HONOR = "honor"
    private val PACKAGE_HONOR_MAIN = "com.huawei.systemmanager"
    private val PACKAGE_HONOR_COMPONENT = "com.huawei.systemmanager.optimize.process.ProtectActivity"

    /***
     * Huawei
     */
    private val BRAND_HUAWEI = "huawei"
    private val PACKAGE_HUAWEI_MAIN = "com.huawei.systemmanager"
    private val PACKAGE_HUAWEI_COMPONENT = "com.huawei.systemmanager.optimize.process.ProtectActivity"
    private val PACKAGE_HUAWEI_COMPONENT_FALLBACK =
        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"

    /**
     * Oppo
     */
    private val BRAND_OPPO = "oppo"
    private val PACKAGE_OPPO_MAIN = "com.coloros.safecenter"
    private val PACKAGE_OPPO_FALLBACK = "com.oppo.safe"
    private val PACKAGE_OPPO_COMPONENT = "com.coloros.safecenter.permission.startup.StartupAppListActivity"
    private val PACKAGE_OPPO_COMPONENT_FALLBACK = "com.oppo.safe.permission.startup.StartupAppListActivity"
    private val PACKAGE_OPPO_COMPONENT_FALLBACK_A = "com.coloros.safecenter.startupapp.StartupAppListActivity"

    /**
     * Vivo
     */
    private val BRAND_VIVO = "vivo"
    private val PACKAGE_VIVO_MAIN = "com.iqoo.secure"
    private val PACKAGE_VIVO_FALLBACK = "com.vivo.perm;issionmanager"
    private val PACKAGE_VIVO_COMPONENT = "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
    private val PACKAGE_VIVO_COMPONENT_FALLBACK = "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
    private val PACKAGE_VIVO_COMPONENT_FALLBACK_A = "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"

    /**
     * Nokia
     */
    private val BRAND_NOKIA = "nokia"
    private val PACKAGE_NOKIA_MAIN = "com.evenwell.powersaving.g3"
    private val PACKAGE_NOKIA_COMPONENT = "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity"

    /**
     * Samsung
     */
    private val BRAND_SAMSUNG = "samsung"
    private val PACKAGE_SAMSUNG_MAIN1 = "com.samsung.android.lool"
    private val PACKAGE_SAMSUNG_MAIN2 = "com.samsung.android.sm"
    private val PACKAGE_SAMSUNG_COMPONENT1 = "com.samsung.android.sm.ui.battery.BatteryActivity"
    private val PACKAGE_SAMSUNG_COMPONENT2 = "com.samsung.android.sm.battery.ui.BatteryActivity"

    /***
     * One plus
     */
    private val BRAND_ONE_PLUS = "oneplus"
    private val PACKAGE_ONE_PLUS_MAIN = "com.oneplus.security"
    private val PACKAGE_ONE_PLUS_COMPONENT = "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
    fun getAutoStartPermission(context: Context) {
        val build_info = Build.BRAND.toLowerCase(Locale.getDefault())
        when (build_info) {
            BRAND_ASUS -> autoStartAsus(context)
            BRAND_XIAOMI, BRAND_XIAOMI_POCO, BRAND_XIAOMI_REDMI -> autoStartXiaomi(context)
            BRAND_LETV -> autoStartLetv(context)
            BRAND_HONOR -> autoStartHonor(context)
            BRAND_HUAWEI -> autoStartHuawei(context)
            BRAND_OPPO -> autoStartOppo(context)
            BRAND_VIVO -> autoStartVivo(context)
            BRAND_NOKIA -> autoStartNokia(context)
            BRAND_SAMSUNG -> autoStartSamsung(context)
            BRAND_ONE_PLUS -> autoStartOnePlus(context)
            else -> Toast.makeText(
                context, context.getString(R.string.setting_not_available_for_device),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun autoStartSamsung(context: Context) {
        val packageName: String
        packageName =
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) PACKAGE_SAMSUNG_MAIN1 else PACKAGE_SAMSUNG_MAIN2
        if (isPackageExists(context, packageName)) {
            showAlert(context) { dialog: DialogInterface, which: Int ->
                try {
                    startIntent(context, packageName, PACKAGE_SAMSUNG_COMPONENT1)
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        startIntent(context, packageName, PACKAGE_SAMSUNG_COMPONENT2)
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }
                }
                dialog.dismiss()
            }
        }
    }

    private fun autoStartAsus(context: Context) {
        if (isPackageExists(context, PACKAGE_ASUS_MAIN)) {
            showAlert(context) { dialog: DialogInterface, which: Int ->
                try {
                    startIntent(context, PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT)
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        startIntent(context, PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT_FALLBACK)
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }
                }
                dialog.dismiss()
            }
        }
    }

    private fun showAlert(context: Context, onClickListener: DialogInterface.OnClickListener) {
        val customDialog = CustomDialog(context)
        val bundle = Bundle()
        bundle.putString(
            Constants.PERMISSION_DIALOG_TITLE,
            context.getString(R.string.auto_start_permission_dialog_title)
        )
        bundle.putString(
            Constants.PERMISSION_DIALOG_MSG,
            """
                ${context.getString(R.string.auto_start_permission_dialog_message)}
                
                ${context.getString(R.string.device_based_settings_message)}
                """.trimIndent()
        )
        customDialog.showDialog(bundle, "AutoStart") { dialog, which ->
            if (which == -2) {
                //Decline
            } else {
                //Accept
                onClickListener.onClick(dialog, which)
            }
        }
    }

    private fun autoStartXiaomi(context: Context) {
        if (isPackageExists(context, PACKAGE_XIAOMI_MAIN)) {
            showAlert(context) { dialog: DialogInterface?, which: Int ->
                try {
                    startIntent(context, PACKAGE_XIAOMI_MAIN, PACKAGE_XIAOMI_COMPONENT)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun autoStartLetv(context: Context) {
        if (isPackageExists(context, PACKAGE_LETV_MAIN)) {
            showAlert(context) { dialog, which ->
                try {
                    startIntent(context, PACKAGE_LETV_MAIN, PACKAGE_LETV_COMPONENT)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun autoStartHonor(context: Context) {
        if (isPackageExists(context, PACKAGE_HONOR_MAIN)) {
            showAlert(context) { dialog, which ->
                try {
                    startIntent(context, PACKAGE_HONOR_MAIN, PACKAGE_HONOR_COMPONENT)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun autoStartHuawei(context: Context) {
        if (isPackageExists(context, PACKAGE_HUAWEI_MAIN)) {
            showAlert(context) { dialog: DialogInterface, which: Int ->
                try {
                    startIntent(context, PACKAGE_HUAWEI_MAIN, PACKAGE_HUAWEI_COMPONENT)
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        startIntent(context, PACKAGE_HUAWEI_MAIN, PACKAGE_HUAWEI_COMPONENT_FALLBACK)
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }
                }
                dialog.dismiss()
            }
        }
    }

    private fun autoStartOppo(context: Context) {
        if (isPackageExists(context, PACKAGE_OPPO_MAIN) || isPackageExists(context, PACKAGE_OPPO_FALLBACK)) {
            showAlert(context) { dialog, which ->
                try {
                    startIntent(context, PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT)
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        startIntent(context, PACKAGE_OPPO_FALLBACK, PACKAGE_OPPO_COMPONENT_FALLBACK)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        try {
                            startIntent(context, PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT_FALLBACK_A)
                        } catch (exx: Exception) {
                            exx.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun autoStartVivo(context: Context) {
        if (isPackageExists(context, PACKAGE_VIVO_MAIN) || isPackageExists(context, PACKAGE_VIVO_FALLBACK)) {
            showAlert(context) { dialog, which ->
                try {
                    startIntent(context, PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT)
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        startIntent(context, PACKAGE_VIVO_FALLBACK, PACKAGE_VIVO_COMPONENT_FALLBACK)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        try {
                            startIntent(context, PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT_FALLBACK_A)
                        } catch (exx: Exception) {
                            exx.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun autoStartNokia(context: Context) {
        if (isPackageExists(context, PACKAGE_NOKIA_MAIN)) {
            showAlert(context) { dialog, which ->
                try {
                    startIntent(context, PACKAGE_NOKIA_MAIN, PACKAGE_NOKIA_COMPONENT)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun autoStartOnePlus(context: Context) {
        if (isPackageExists(context, PACKAGE_ONE_PLUS_MAIN)) {
            showAlert(context) { dialog: DialogInterface?, which: Int ->
                try {
                    startIntent(context, PACKAGE_ONE_PLUS_MAIN, PACKAGE_ONE_PLUS_COMPONENT)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun startIntent(context: Context, packageName: String, componentName: String) {
        try {
            val intent = Intent()
            intent.component = ComponentName(packageName, componentName)
            context.startActivity(intent)
        } catch (var5: Exception) {
            var5.printStackTrace()
            throw var5
        }
    }

    private fun isPackageExists(context: Context, targetPackage: String): Boolean {
        val packages: List<ApplicationInfo>
        val pm = context.packageManager
        packages = pm.getInstalledApplications(0)
        for (packageInfo in packages) {
            if (packageInfo.packageName == targetPackage) {
                return true
            }
        }
        return false
    }

    companion object {
        @JvmStatic
        val instance: AutoStartHelper
            get() = AutoStartHelper()
    }
}