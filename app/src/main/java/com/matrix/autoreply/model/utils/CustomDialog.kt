package com.matrix.autoreply.model.utils

import android.content.Context
import android.os.Bundle
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.matrix.autoreply.R

class CustomDialog(private val mContext: Context) {
    fun showDialog(bundle: Bundle?, type: String?, onClickListener: DialogInterface.OnClickListener) {
        if (bundle != null) {
            val materialAlertDialogBuilder: MaterialAlertDialogBuilder
            if (type != null && type == "AutoStart") {
                materialAlertDialogBuilder = MaterialAlertDialogBuilder(mContext)
                    .setTitle(bundle.getString(Constants.PERMISSION_DIALOG_TITLE))
                    .setMessage(bundle.getString(Constants.PERMISSION_DIALOG_MSG))
                materialAlertDialogBuilder
                    .setNegativeButton(mContext.resources.getString(R.string.decline_auto_start_setting)) { dialog: DialogInterface?, which: Int ->
                        onClickListener.onClick(
                            dialog,
                            which
                        )
                    }
                    .setPositiveButton(mContext.resources.getString(R.string.enable_auto_start_setting)) { dialog: DialogInterface?, which: Int ->
                        onClickListener.onClick(
                            dialog,
                            which
                        )
                    }
            } else if (bundle.containsKey(Constants.PERMISSION_DIALOG_DENIED)
                && bundle.getBoolean(Constants.PERMISSION_DIALOG_DENIED)
            ) {
                materialAlertDialogBuilder = MaterialAlertDialogBuilder(mContext)
                    .setTitle(bundle.getString(Constants.PERMISSION_DIALOG_DENIED_TITLE))
                    .setIcon(mContext.resources.getDrawable(R.drawable.ic_alert))
                    .setMessage(bundle.getString(Constants.PERMISSION_DIALOG_DENIED_MSG))
                materialAlertDialogBuilder
                    .setNegativeButton(mContext.resources.getString(R.string.sure)) { dialog: DialogInterface?, which: Int ->
                        onClickListener.onClick(
                            dialog,
                            which
                        )
                    }
                    .setPositiveButton(mContext.resources.getString(R.string.retry)) { dialog: DialogInterface?, which: Int ->
                        onClickListener.onClick(
                            dialog,
                            which
                        )
                    }
            } else {
                materialAlertDialogBuilder = MaterialAlertDialogBuilder(mContext)
                    .setTitle(bundle.getString(Constants.PERMISSION_DIALOG_TITLE))
                    .setMessage(bundle.getString(Constants.PERMISSION_DIALOG_MSG))
                materialAlertDialogBuilder
                    .setNegativeButton(mContext.resources.getString(R.string.decline)) { dialog: DialogInterface?, which: Int ->
                        onClickListener.onClick(
                            dialog,
                            which
                        )
                    }
                    .setPositiveButton(mContext.resources.getString(R.string.accept)) { dialog: DialogInterface?, which: Int ->
                        onClickListener.onClick(
                            dialog,
                            which
                        )
                    }
            }
            materialAlertDialogBuilder
                .setCancelable(false)
                .show()
        }
    }
}