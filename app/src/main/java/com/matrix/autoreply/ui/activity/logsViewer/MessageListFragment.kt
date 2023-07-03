package com.matrix.autoreply.ui.activity.logsViewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.matrix.autoreply.databinding.FragmentMessageListBinding
import com.matrix.autoreply.store.database.MessageLogsDB
import com.matrix.autoreply.ui.adapters.ContactMessageAdapter

class MessageListFragment : Fragment(), RefreshListener {

    private var _binding: FragmentMessageListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ContactMessageAdapter
    private lateinit var userMessageList: List<Pair<String?, Long>>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMessageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ContactMessageAdapter()
        binding.messageListRecyclerView.adapter = adapter
        binding.messageListRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)

        // Call a method to fetch and set the list of messages based on the selected notification title
        val selectedTitle = arguments?.getString(ARG_SELECTED_TITLE)
        if (selectedTitle != null) {
            fetchAndSetMessages(selectedTitle)
        }
    }

    private fun fetchAndSetMessages(contactName: String) {
        userMessageList = getMessageLogsWithTitle(contactName)
        adapter.submitList(userMessageList)
    }

    private fun getMessageLogsWithTitle(contactName: String): List<Pair<String?, Long>> {
        // TODO: 1. To make all the calls using suspend function and coroutines to make database IO calls
        // TODO: 2. To use Dagger Dependency Injection to get singleton of DB
        val messageLogWithTitleList =
            MessageLogsDB.getInstance(requireContext())!!.messageLogsDao()!!.getMessageLogsWithTitle(contactName)

        return messageLogWithTitleList
            .filter { it.notifMessage != null }
            .map { messageLog ->
                val message = messageLog.notifMessage
                val timestamp = messageLog.notifArrivedTime

                // Return the extracted items as a pair or any other desired structure
                Pair(message, timestamp)
            }.reversed()
    }

    companion object {
        private const val ARG_SELECTED_TITLE = "selected_title"

        fun newInstance(selectedTitle: String): MessageListFragment {
            val fragment = MessageListFragment()
            val args = Bundle().apply {
                putString(ARG_SELECTED_TITLE, selectedTitle)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onRefresh() {
        userMessageList = getMessageLogsWithTitle(arguments?.getString(ARG_SELECTED_TITLE)!!)
        adapter.submitList(userMessageList)
    }
}
