package com.matrix.autoreply.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.matrix.autoreply.R
import com.matrix.autoreply.constants.PromptTemplate
import com.matrix.autoreply.constants.PromptTemplates
import com.matrix.autoreply.preferences.PreferencesManager

class PromptSelectorFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PromptTemplateAdapter
    private lateinit var customPromptCard: MaterialCardView
    private lateinit var customPromptInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var preferencesManager: PreferencesManager
    
    private var selectedTemplateId: String = "friendly"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_prompt_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager.getPreferencesInstance(requireActivity())!!
        
        recyclerView = view.findViewById(R.id.templates_recycler_view)
        customPromptCard = view.findViewById(R.id.custom_prompt_card)
        customPromptInput = view.findViewById(R.id.custom_prompt_input)
        saveButton = view.findViewById(R.id.save_button)
        
        setupRecyclerView()
        setupSaveButton()
        loadCurrentSelection()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PromptTemplateAdapter(
            PromptTemplates.TEMPLATES,
            selectedTemplateId
        ) { template ->
            onTemplateSelected(template)
        }
        recyclerView.adapter = adapter
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            saveSelectedPrompt()
        }
    }

    private fun loadCurrentSelection() {
        // Load the currently saved template ID and custom prompt
        val savedTemplateId = preferencesManager.aiPromptTemplateId ?: "friendly"
        val savedPrompt = preferencesManager.aiSystemMessage
        
        selectedTemplateId = savedTemplateId
        adapter.setSelectedTemplate(selectedTemplateId)
        
        // Always show the edit field
        customPromptCard.visibility = View.VISIBLE
        
        // Load the saved prompt
        if (!savedPrompt.isNullOrEmpty()) {
            customPromptInput.setText(savedPrompt)
        } else {
            val template = PromptTemplates.getTemplateById(savedTemplateId)
            customPromptInput.setText(template?.prompt ?: "")
        }
    }

    private fun onTemplateSelected(template: PromptTemplate) {
        selectedTemplateId = template.id
        adapter.setSelectedTemplate(selectedTemplateId)
        
        // Always show the edit field and load the template's prompt
        customPromptCard.visibility = View.VISIBLE
        
        // Check if user has a customized version of this template
        val savedTemplateId = preferencesManager.aiPromptTemplateId
        val savedPrompt = preferencesManager.aiSystemMessage
        
        if (savedTemplateId == template.id && !savedPrompt.isNullOrEmpty()) {
            // User has previously edited this template, load their version
            customPromptInput.setText(savedPrompt)
        } else {
            // Load the default template prompt
            customPromptInput.setText(template.prompt)
        }
        
        customPromptInput.requestFocus()
    }

    private fun saveSelectedPrompt() {
        val selectedTemplate = PromptTemplates.getTemplateById(selectedTemplateId)
        
        if (selectedTemplate == null) {
            Toast.makeText(requireContext(), "Please select a prompt template", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Always get the prompt from the edit field (allows editing any template)
        val promptToSave = customPromptInput.text.toString().trim()
        if (promptToSave.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a prompt", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save to preferences
        preferencesManager.aiPromptTemplateId = selectedTemplateId
        preferencesManager.aiSystemMessage = promptToSave
        
        Toast.makeText(
            requireContext(),
            "AI prompt style saved successfully!",
            Toast.LENGTH_SHORT
        ).show()
        
        // Navigate back
        parentFragmentManager.popBackStack()
    }

    override fun onResume() {
        super.onResume()
        activity?.title = "Select AI Prompt Style"
    }
}

class PromptTemplateAdapter(
    private val templates: List<PromptTemplate>,
    private var selectedTemplateId: String,
    private val onTemplateClick: (PromptTemplate) -> Unit
) : RecyclerView.Adapter<PromptTemplateAdapter.TemplateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prompt_template, parent, false)
        return TemplateViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(templates[position], templates[position].id == selectedTemplateId)
    }

    override fun getItemCount(): Int = templates.size

    fun setSelectedTemplate(templateId: String) {
        val oldPosition = templates.indexOfFirst { it.id == selectedTemplateId }
        val newPosition = templates.indexOfFirst { it.id == templateId }
        
        selectedTemplateId = templateId
        
        if (oldPosition >= 0) notifyItemChanged(oldPosition)
        if (newPosition >= 0) notifyItemChanged(newPosition)
    }

    inner class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.template_icon)
        private val name: TextView = itemView.findViewById(R.id.template_name)
        private val description: TextView = itemView.findViewById(R.id.template_description)
        private val radio: MaterialRadioButton = itemView.findViewById(R.id.template_radio)

        fun bind(template: PromptTemplate, isSelected: Boolean) {
            icon.text = template.icon
            name.text = template.name
            description.text = template.description
            radio.isChecked = isSelected
            
            // Change stroke width on the card
            (itemView as? MaterialCardView)?.strokeWidth = if (isSelected) 4 else 1
            
            itemView.setOnClickListener {
                onTemplateClick(template)
            }
        }
    }
}
