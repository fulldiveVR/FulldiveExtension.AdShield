package com.fulldive.wallet.presentation.keyboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class KeyboardPagerAdapter(
    fragmentManager: FragmentManager,
    listener: KeyboardListener
) :
    FragmentPagerAdapter(fragmentManager) {
    val fragments: List<KeyboardFragment>

    init {
        fragments = listOf(
            NumberKeyboardFragment.newInstance().apply {
                setListener(listener)
            },
            AlphabetKeyboardFragment.newInstance().apply {
                setListener(listener)
            }
        )
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}