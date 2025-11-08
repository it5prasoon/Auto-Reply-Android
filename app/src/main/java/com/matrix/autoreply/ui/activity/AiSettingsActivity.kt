package com.matrix.autoreply.ui.activity

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import com.matrix.autoreply.R
import com.matrix.autoreply.databinding.ActivityAiSettingsBinding
import com.matrix.autoreply.ui.fragment.AiSettingsFragment

class AiSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityAiSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        val colorDrawable = "#171D3B".toColorInt().toDrawable()
        actionBar?.setBackgroundDrawable(colorDrawable)
        actionBar?.title = "AI Settings"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AiSettingsFragment())
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}