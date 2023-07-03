package com.matrix.autoreply.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.matrix.autoreply.R
import com.matrix.autoreply.ui.activity.logsViewer.MsgLogViewerActivity


open class DeletedMessageFragment : Fragment() {

    private val checkEmoji = String(Character.toChars(0x2714))

    companion object {
        private const val WHATSAPP = "whatsapp";
        private const val APP = "app";
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_deleted_message, container, false)

        // Widgets
        val msgLogStatus = view.findViewById<TextView>(R.id.msg_log_status)
        val viewWALogBtn = view.findViewById<Button>(R.id.view_wa_log_btn)

        msgLogStatus.text = getString(R.string.msg_log_status_str, checkEmoji)

        // WhatsApp Message Logs viewer
        viewWALogBtn.setOnClickListener {
            val intent = Intent(requireActivity(), MsgLogViewerActivity::class.java)
            intent.putExtra(APP, WHATSAPP)
            startActivity(intent)
        }
        return view
    }
}