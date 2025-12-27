package com.matrix.autoreply.ui.activity.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.matrix.autoreply.R
import com.matrix.autoreply.ui.fragment.MainFragment
import com.matrix.autoreply.ui.fragment.SettingsFragment
import com.matrix.autoreply.ui.fragment.DeletedMessageFragment
import com.matrix.autoreply.ui.fragment.LiveChatFragment

private val TAB_TITLES = arrayOf(
        R.string.tab_text_1,
        R.string.tab_text_2,
        R.string.tab_text_3,
        R.string.tab_text_4
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null

        when (position) {
            0 -> fragment = MainFragment()
            1 -> fragment = DeletedMessageFragment()
            2 -> fragment = LiveChatFragment()
            3 -> fragment = SettingsFragment()
        }

        return fragment!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 4 total pages.
        return 4
    }
}
