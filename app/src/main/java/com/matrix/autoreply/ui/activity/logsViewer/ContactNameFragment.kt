package com.matrix.autoreply.ui.activity.logsViewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.matrix.autoreply.R
import com.matrix.autoreply.databinding.FragmentContactNameBinding
import com.matrix.autoreply.store.database.MessageLogsDB
import com.matrix.autoreply.ui.adapters.ContactNameAdapter

class ContactNameFragment : Fragment(), RefreshListener {

    private var _binding: FragmentContactNameBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageLogsDB: MessageLogsDB
    private val adapter: ContactNameAdapter by lazy {
        ContactNameAdapter(messageLogsDB) { contactName ->
            // Handle the click event here
            // Create and show the MessageListFragment with the selected contact name
            val fragment = MessageListFragment.newInstance(contactName)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
    private lateinit var contactNameList: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
        messageLogsDB = MessageLogsDB.getInstance(requireContext())!!
        
        // Getting all notification titles i.e. WhatsApp user-name from room DB
        updateUserNameList()

        // Set up RecyclerView to display notification titles
        binding.contactNameRv.adapter = adapter
        binding.contactNameRv.layoutManager = LinearLayoutManager(requireContext())
        binding.contactNameRv.setHasFixedSize(true)
        adapter.submitList(contactNameList)

        // Set up SwipeRefreshLayout for refreshing the message log
        binding.swipeRefreshLayout.setOnRefreshListener {
            this.onRefresh()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun updateUserNameList() {
        // TODO: 1. To make all the calls using suspend function and coroutines to make database IO calls
        // TODO: 2. To use Dagger Dependency Injection to get singleton of DB
        contactNameList = MessageLogsDB.getInstance(requireContext())!!.messageLogsDao()!!.getDistinctNotificationTitles()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Refresh the message log RecyclerView with the updated contents of the file
    override fun onRefresh() {
        updateUserNameList()
        adapter.submitList(contactNameList)
    }
}
