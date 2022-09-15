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

package ui.rewards.board

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.joom.lightsaber.getInstance
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ui.rewards.board.base.BaseExperienceLayout
import org.adshield.databinding.LayoutExperienceBoardBinding

class ExperienceBoardLayout :
    BaseExperienceLayout<LayoutExperienceBoardBinding, ExperienceBoardPresenter>,
    ExperienceBoardView {

    override val experienceProgressViewLayout: ExperienceProgressViewLayout? get() = binding?.experienceProgressViewLayout
    override val experienceProgressTextView: TextView? = binding?.experienceProgressTextView
    override val experienceTextView: TextView? get() = binding?.experienceTextView
    override val exchangeButton: TextView? get() = binding?.exchangeButton
    override val timeIconView: ImageView? get() = binding?.timeIconView

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @InjectPresenter
    override lateinit var presenter: ExperienceBoardPresenter

    @ProvidePresenter
    fun providePresenter(): ExperienceBoardPresenter = appInjector.getInstance()

    override fun getViewBinding() = LayoutExperienceBoardBinding
        .inflate(LayoutInflater.from(context), this, true)
}