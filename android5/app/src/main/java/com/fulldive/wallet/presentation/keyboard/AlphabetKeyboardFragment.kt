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

class AlphabetKeyboardFragment : KeyboardFragment(), View.OnClickListener {
    private var rootView: View? = null
    private val alphabetButtons = mutableListOf<Button>()
    private val alphabetArray =
        "ABCDEFGHUJKLMNOPQRSTUVWXYZ".split("").filter(String::isNotEmpty).toMutableList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_keyboard_alphabet, container, false)
        alphabetArray.shuffle(Random(System.nanoTime()))
        val packageName = view.context.packageName
        for (i in alphabetArray.indices) {
            val button: Button = view.findViewById(
                resources.getIdentifier(
                    "alphabetButton$i", "id", packageName
                )
            )
            button.text = alphabetArray[i]
            button.setOnClickListener(this)
            alphabetButtons.add(button)
        }
        view.findViewById<ImageButton>(R.id.deleteButton)
            .setOnClickListener(this)
        rootView = view
        return rootView
    }

    override fun shuffleKeyboard() {
        rootView?.let { view ->
            val fadeInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
            fadeInAnimation.reset()
            val fadeOutAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
            fadeOutAnimation.reset()
            fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    alphabetArray.shuffle(Random(System.nanoTime()))
                    for (i in alphabetButtons.indices) {
                        alphabetButtons[i].text = alphabetArray[i]
                    }
                    view.startAnimation(fadeInAnimation)
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            view.startAnimation(fadeOutAnimation)
        }
    }

    override fun onClick(view: View) {
        if (view is Button) {
            if (keyboardListener != null) {
                keyboardListener.userInsertKey(
                    view.text.toString().trim().getOrNull(0).or('A')
                )
            }
        } else if (view is ImageButton) {
            if (keyboardListener != null) {
                keyboardListener.userDeleteKey()
            }
        }
    }

    companion object {
        fun newInstance(): AlphabetKeyboardFragment {
            return AlphabetKeyboardFragment()
        }
    }
}