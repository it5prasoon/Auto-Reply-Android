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
import com.matrix.autoreply.utils.AnalyticsTracker
import com.matrix.autoreply.network.AiService
import com.matrix.autoreply.network.model.ai.AiMessage
import com.matrix.autoreply.network.model.ai.AiRequest
import com.matrix.autoreply.network.model.ai.AiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.app.ProgressDialog
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText as MaterialTextInputEditText

class PromptSelectorFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PromptTemplateAdapter
    private lateinit var customPromptCard: MaterialCardView
    private lateinit var customPromptInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var generateWithAiButton: MaterialButton
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
        generateWithAiButton = view.findViewById(R.id.generate_with_ai_button)
        
        setupRecyclerView()
        setupSaveButton()
        setupAiGenerateButton()
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
    
    private fun setupAiGenerateButton() {
        generateWithAiButton.setOnClickListener {
            showAiGenerateDialog()
        }
    }
    
    private fun showAiGenerateDialog() {
        // Check if AI is configured
        if (!preferencesManager.isAiEnabled || preferencesManager.aiApiKey.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please configure AI in settings first (API Key required)",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        // Create EditText programmatically
        val input = android.widget.EditText(requireContext()).apply {
            hint = "e.g., Make it more professional, Add humor, Make it shorter"
            setPadding(50, 40, 50, 40)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Modify Prompt with AI")
            .setMessage("Describe how you want to modify the current prompt:")
            .setView(input)
            .setPositiveButton("Generate") { _, _ ->
                val userRequest = input.text?.toString()?.trim() ?: ""
                if (userRequest.isNotEmpty()) {
                    generatePromptWithAi(userRequest)
                } else {
                    Toast.makeText(requireContext(), "Please enter a modification request", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun generatePromptWithAi(userRequest: String) {
        val currentPrompt = customPromptInput.text.toString().trim()
        
        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("AI is generating your prompt...")
            setCancelable(false)
            show()
        }
        
        val systemPrompt = """You are a prompt engineering assistant. Generate a system prompt for an AI auto-reply assistant based on the user's request.
Current prompt: "$currentPrompt"
User wants: $userRequest

Generate an improved system prompt that incorporates the user's request. Return ONLY the new system prompt, nothing else. Keep it concise (1-3 sentences)."""
        
        val messages = listOf(
            AiMessage("user", systemPrompt)
        )
        
        val request = AiRequest(
            model = preferencesManager.aiSelectedModel,
            messages = messages,
            maxTokens = 200,
            temperature = 0.8
        )
        
        val provider = preferencesManager.aiProvider
        val baseUrl = when (provider) {
            "groq" -> "https://api.groq.com/openai/"
            "openai" -> "https://api.openai.com/"
            else -> "https://api.groq.com/openai/"
        }
        
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        val service = retrofit.create(AiService::class.java)
        val call = service.getChatCompletion("Bearer ${preferencesManager.aiApiKey}", request)
        
        call.enqueue(object : Callback<AiResponse> {
            override fun onResponse(call: Call<AiResponse>, response: Response<AiResponse>) {
                progressDialog.dismiss()
                
                if (response.isSuccessful && response.body() != null) {
                    val aiResponse = response.body()!!
                    if (aiResponse.choices.isNotEmpty()) {
                        val generatedPrompt = aiResponse.choices[0].message.content.trim()
                        customPromptInput.setText(generatedPrompt)
                        
                        // Track success
                        AnalyticsTracker.trackAiPromptGenerated(requireContext(), true)
                        
                        Toast.makeText(requireContext(), "Prompt generated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Track failure
                        AnalyticsTracker.trackAiPromptGenerated(requireContext(), false)
                        Toast.makeText(requireContext(), "Failed to generate prompt", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Track failure
                    AnalyticsTracker.trackAiPromptGenerated(requireContext(), false)
                    Toast.makeText(
                        requireContext(),
                        "AI generation failed. Please check your API key.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            
            override fun onFailure(call: Call<AiResponse>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Network error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
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
        
        // Enhanced focus handling for Android 12+ compatibility
        customPromptInput.post {
            customPromptInput.requestFocus()
            customPromptInput.isFocusableInTouchMode = true
            customPromptInput.setSelection(customPromptInput.text?.length ?: 0)
        }
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
        
        // Track analytics
        AnalyticsTracker.trackPromptTemplateSelected(requireContext(), selectedTemplateId)
        
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
