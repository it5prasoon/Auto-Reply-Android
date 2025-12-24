package com.matrix.autoreply.ui

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.matrix.autoreply.R

class ApiKeyGuideDialog : DialogFragment() {
    
    private var provider: String = "groq"
    private var currentStep = 0
    private val totalSteps = 4
    
    private lateinit var stepTitle: TextView
    private lateinit var stepDescription: TextView
    private lateinit var stepImage: ImageView
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var btnOpenWebsite: Button
    private lateinit var stepIndicator: TextView
    
    companion object {
        fun newInstance(provider: String): ApiKeyGuideDialog {
            val dialog = ApiKeyGuideDialog()
            val args = Bundle()
            args.putString("provider", provider)
            dialog.arguments = args
            return dialog
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provider = arguments?.getString("provider") ?: "groq"
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Dialog_NoActionBar)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_api_key_guide, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        stepTitle = view.findViewById(R.id.stepTitle)
        stepDescription = view.findViewById(R.id.stepDescription)
        stepImage = view.findViewById(R.id.stepImage)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        btnNext = view.findViewById(R.id.btnNext)
        btnOpenWebsite = view.findViewById(R.id.btnOpenWebsite)
        stepIndicator = view.findViewById(R.id.stepIndicator)
        
        view.findViewById<Button>(R.id.btnClose).setOnClickListener { dismiss() }
        
        btnPrevious.setOnClickListener {
            if (currentStep > 0) {
                currentStep--
                updateStep()
            }
        }
        
        btnNext.setOnClickListener {
            if (currentStep < totalSteps - 1) {
                currentStep++
                updateStep()
            } else {
                dismiss()
            }
        }
        
        btnOpenWebsite.setOnClickListener {
            openProviderWebsite()
        }
        
        updateStep()
    }
    
    private fun updateStep() {
        stepIndicator.text = "Step ${currentStep + 1} of $totalSteps"
        
        when (currentStep) {
            0 -> showStep1()
            1 -> showStep2()
            2 -> showStep3()
            3 -> showStep4()
        }
        
        btnPrevious.visibility = if (currentStep > 0) View.VISIBLE else View.GONE
        btnNext.text = if (currentStep < totalSteps - 1) "Next" else "Done"
    }
    
    private fun showStep1() {
        stepTitle.text = "Welcome to ${getProviderName()}"
        stepDescription.text = when (provider) {
            "groq" -> "Groq provides FREE AI models that work great for auto-replies. No credit card required!"
            "openai" -> "OpenAI provides powerful AI models. Note: This requires payment after free trial."
            "anthropic" -> "Anthropic Claude provides intelligent AI models. Requires API credits for usage."
            "google" -> "Google Gemini offers powerful AI capabilities. Free tier available with usage limits."
            "ollama" -> "Ollama runs AI models locally on your computer. FREE but requires setup on your PC/Mac."
            "bedrock" -> "AWS Bedrock provides enterprise AI models. Requires AWS account and billing setup."
            else -> "Let's get your API key to enable smart AI replies."
        }
        stepImage.setImageResource(android.R.drawable.ic_dialog_info)
        btnOpenWebsite.visibility = View.GONE
    }
    
    private fun showStep2() {
        stepTitle.text = "Create Your Account"
        stepDescription.text = when (provider) {
            "groq" -> "1. Click 'Open Groq Console' below\n2. Click 'Sign Up' if you don't have an account\n3. Use your Google account or email to sign up\n4. It's completely FREE!"
            "openai" -> "1. Click 'Open OpenAI Platform' below\n2. Click 'Sign Up' if you don't have an account\n3. Use your Google account or email to sign up\n4. Note: You'll need to add payment info after free trial"
            "anthropic" -> "1. Click 'Open Anthropic Console' below\n2. Sign up with your email address\n3. Verify your email and complete setup\n4. Add payment method for API credits"
            "google" -> "1. Click 'Open Google AI Studio' below\n2. Sign in with your Google account\n3. Accept the terms of service\n4. Free tier includes generous usage limits"
            "ollama" -> "1. Click 'Download Ollama' below\n2. Install Ollama on your computer\n3. Open terminal/command prompt\n4. Run: ollama serve (to start the server)"
            "bedrock" -> "1. Click 'Open AWS Console' below\n2. Create AWS account if needed\n3. Set up billing and payment\n4. Request access to Bedrock models"
            else -> "Create your account on the provider's website"
        }
        stepImage.setImageResource(android.R.drawable.ic_menu_add)
        btnOpenWebsite.visibility = View.VISIBLE
        btnOpenWebsite.text = when (provider) {
            "ollama" -> "Download Ollama"
            else -> "Open ${getProviderName()} Console"
        }
    }
    
    private fun showStep3() {
        stepTitle.text = "Get Your API Key"
        stepDescription.text = when (provider) {
            "groq" -> "1. After logging in, look for 'API Keys' in the menu\n2. Click 'Create API Key'\n3. Give it a name like 'AutoReply'\n4. Copy the key that appears (it starts with 'gsk_')"
            "openai" -> "1. After logging in, go to 'API Keys' section\n2. Click 'Create new secret key'\n3. Give it a name like 'AutoReply'\n4. Copy the key that appears (it starts with 'sk-')"
            "anthropic" -> "1. Go to 'API Keys' in your console\n2. Click 'Create Key'\n3. Give it a name like 'AutoReply'\n4. Copy the key (it starts with 'sk-ant-')"
            "google" -> "1. In AI Studio, click 'Get API key'\n2. Click 'Create API key'\n3. Select your Google Cloud project\n4. Copy the generated API key"
            "ollama" -> "1. Make sure Ollama is running: ollama serve\n2. In the API Key field, enter your server URL\n3. Example: http://192.168.1.100:11434\n4. Use localhost:11434 if running on same device"
            "bedrock" -> "1. Set up AWS CLI credentials\n2. In the API Key field, enter your AWS Access Key\n3. You'll also need Secret Key and Region\n4. Contact admin for enterprise setup"
            else -> "Navigate to the API Keys section and create a new key"
        }
        stepImage.setImageResource(android.R.drawable.ic_menu_manage)
        btnOpenWebsite.visibility = if (provider == "bedrock") View.GONE else View.VISIBLE
        btnOpenWebsite.text = when (provider) {
            "ollama" -> "Download Ollama"
            else -> "Open ${getProviderName()} Console"
        }
    }
    
    private fun showStep4() {
        stepTitle.text = "Complete Your Setup"
        stepDescription.text = "1. Go back to the AI Settings\n2. Paste your API key in the 'AI API Key' field\n3. Select your preferred AI model\n4. Customize the System Prompt if needed\n5. Enable AI Smart Replies and test!"
        stepImage.setImageResource(android.R.drawable.ic_dialog_alert)
        btnOpenWebsite.visibility = View.GONE
    }
    
    private fun getProviderName(): String {
        return when (provider) {
            "groq" -> "Groq"
            "openai" -> "OpenAI"
            "anthropic" -> "Anthropic"
            "google" -> "Google AI"
            "ollama" -> "Ollama"
            "bedrock" -> "AWS Bedrock"
            else -> "AI Provider"
        }
    }
    
    private fun openProviderWebsite() {
        val url = when (provider) {
            "groq" -> "https://console.groq.com/keys"
            "openai" -> "https://platform.openai.com/api-keys"
            "anthropic" -> "https://console.anthropic.com/settings/keys"
            "google" -> "https://aistudio.google.com/app/apikey"
            "ollama" -> "https://ollama.com/download"
            "bedrock" -> "https://console.aws.amazon.com/bedrock/"
            else -> "https://console.groq.com/keys"
        }
        
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return dialog
    }
}
