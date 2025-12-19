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
    private var platformFilter: String = "all"
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

    companion object {
        fun newInstance(appFilter: String): ContactNameFragment {
            val fragment = ContactNameFragment()
            val args = Bundle()
            args.putString("app_filter", appFilter)
            fragment.arguments = args
            return fragment
        }
    }

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

        // Get platform filter from arguments
        platformFilter = arguments?.getString("app_filter") ?: "all"

        // Initialize database
        messageLogsDB = MessageLogsDB.getInstance(requireContext())!!
        
        // Getting all notification titles filtered by platform
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
        // Get all contacts first
        val allContacts = MessageLogsDB.getInstance(requireContext())!!.messageLogsDao()!!.getDistinctNotificationTitles()
        
        // Filter by platform if specified
        contactNameList = when (platformFilter) {
            "whatsapp" -> {
                // Filter to show only WhatsApp contacts by checking if messages exist for com.whatsapp
                allContacts.filter { contactName ->
                    hasMessagesForPackage(contactName, "com.whatsapp")
                }
            }
            "whatsapp_business" -> {
                // Filter to show only WhatsApp Business contacts by checking if messages exist for com.whatsapp.w4b
                allContacts.filter { contactName ->
                    hasMessagesForPackage(contactName, "com.whatsapp.w4b")
                }
            }
            else -> allContacts // Show all contacts
        }
    }
    
    private fun hasMessagesForPackage(contactName: String, packageName: String): Boolean {
        return try {
            val messages = messageLogsDB.messageLogsDao()!!.getMessageLogsWithTitle(contactName)
            val appPackageDao = messageLogsDB.appPackageDao()!!
            
            messages.any { messageLog ->
                val packageIndex = messageLog.index
                val actualPackageName = appPackageDao.getPackageName(packageIndex)
                actualPackageName == packageName
            }
        } catch (e: Exception) {
            false
        }
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
