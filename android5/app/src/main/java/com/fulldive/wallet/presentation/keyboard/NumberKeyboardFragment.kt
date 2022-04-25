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