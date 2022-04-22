package ui.rewards.board

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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