/*
 * This file is part of Blokada.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright © 2022 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package ui.home

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.adshield.R

class ActivateView : BaseFrameLayout {

    override val layoutResId: Int get() = R.layout.layout_activate_view

    private val activateStateImageView: ImageView get() = findViewById(R.id.activateStateImageView)
    private val activeBackgroundImageView: ImageView get() = findViewById(R.id.activeBackgroundImageView)
    private val pulsatorLayout: PulsatorLayout get() = findViewById(R.id.pulsatorLayout)

    val stateButtonAnimator: ObjectAnimator by lazy {
        ObjectAnimator.ofPropertyValuesHolder(
            activateStateImageView,
            PropertyValuesHolder.ofFloat("scaleX", 1.1f),
            PropertyValuesHolder.ofFloat("scaleY", 1.1f)
        ).apply {
            duration = ANIMATION_DURATION
            repeatCount = ANIMATION_REPEAT_COUNT
            repeatMode = ObjectAnimator.REVERSE
        }
    }

    private val inactiveBackground by lazy {
        ContextCompat.getDrawable(
            context,
            R.drawable.background_circle_gray
        )
    }

    private val activeBackground by lazy {
        ContextCompat.getDrawable(
            context,
            R.drawable.background_circle_orange
        )
    }

    private val inactiveDrawable by lazy {
        ContextCompat.getDrawable(
            context,
            R.drawable.ic_activate_off
        )
    }

    private val activeDrawable by lazy {
        ContextCompat.getDrawable(
            context,
            R.drawable.ic_activate_on
        )
    }

    var inactiveMode = true
        set(value) {
            field = value
            if (field) {
                setInactiveState()
                activateStateImageView.background = inactiveBackground
                activateStateImageView.setImageDrawable(inactiveDrawable)
            }
        }

    var activeMode = false
        set(value) {
            field = value
            if (field) {
                setActiveState()
                activateStateImageView.background = activeBackground
                activateStateImageView.setImageDrawable(activeDrawable)
            }
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun initLayout() {
        super.initLayout()
        pulsatorLayout.onAnimationEndListener = {
            activeBackgroundImageView.isVisible = activeMode
        }
    }

    fun onClicked() {
        stateButtonAnimator.start()
    }

    private fun setActiveState() {
        pulsatorLayout.start()
    }

    private fun setInactiveState() {
        activeBackgroundImageView.isVisible = false
        pulsatorLayout.stop()
    }

    companion object {
        private const val ANIMATION_DURATION = 100L
        private const val ANIMATION_REPEAT_COUNT = 3
    }
}