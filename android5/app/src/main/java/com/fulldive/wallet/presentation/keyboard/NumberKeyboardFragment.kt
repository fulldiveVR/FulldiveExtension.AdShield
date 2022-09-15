/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.fulldive.wallet.presentation.keyboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import com.fulldive.wallet.extensions.or
import org.adshield.R
import java.util.*

class NumberKeyboardFragment : KeyboardFragment(), View.OnClickListener {
    private var rootView: View? = null
    private val numberButtons = mutableListOf<Button>()
    private val numberArray = "0123456789".split("").filter(String::isNotEmpty).toMutableList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_keyboard_number, container, false)
        numberArray.shuffle(Random(System.nanoTime()))

        val packageName = view.context.packageName
        numberArray.forEachIndexed { i, text ->
            val button: Button = view.findViewById(
                resources.getIdentifier(
                    "password_number$i", "id", packageName
                )
            )
            button.text = text
            button.setOnClickListener(this)
            numberButtons.add(button)
        }
        view.findViewById<View>(R.id.deleteButton).setOnClickListener(this)
        rootView = view
        return rootView
    }

    override fun shuffleKeyboard() {
        val fadeInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        fadeInAnimation.reset()
        val fadeOutAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
        fadeOutAnimation.reset()
        fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                numberArray.shuffle(Random(System.nanoTime()))
                for (i in numberButtons.indices) {
                    numberButtons[i].text = numberArray[i]
                }
                rootView?.startAnimation(fadeInAnimation)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        rootView?.startAnimation(fadeOutAnimation)
    }

    override fun onClick(view: View) {
        if (view is Button) {
            keyboardListener?.userInsertKey(view.text.toString().trim().getOrNull(0).or('0'))
        } else if (view is ImageButton) {
            keyboardListener?.userDeleteKey()
        }
    }

    companion object {
        fun newInstance(): NumberKeyboardFragment {
            return NumberKeyboardFragment()
        }
    }
}