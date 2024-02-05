package com.fulldive.wallet.presentation.base.subscription

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.view.View
import androidx.navigation.fragment.findNavController
import com.fulldive.wallet.extensions.fromHtmlToSpanned
import com.fulldive.wallet.extensions.getHexColor
import com.fulldive.wallet.extensions.unsafeLazy
import com.fulldive.wallet.presentation.base.BaseMvpFragment
import com.joom.lightsaber.getInstance
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import org.adshield.R
import org.adshield.databinding.FragmentSubscriptionTutorialBinding
import ui.utils.CustomImageSpan
import java.util.regex.Pattern

class SubscriptionTutorialFragment :
    BaseMvpFragment<FragmentSubscriptionTutorialBinding>(),
    SubscriptionTutorialView {

    @InjectPresenter
    lateinit var presenter: SubscriptionTutorialPresenter

    private val tutorialColor by unsafeLazy {
        context.getHexColor(R.color.colorAccent)
    }

    private val tutorial by unsafeLazy {
        SpannableString.valueOf(context.getString(R.string.str_subscription_tutorial_checkmarks))
            .apply {
                process(
                    Pattern.compile(Pattern.quote(CHECK_MARK_TAG)),
                    R.drawable.ic_check_mark,
                    context,
                    this,
                    DynamicDrawableSpan.ALIGN_BASELINE
                )
            }
    }

    @ProvidePresenter
    fun providePresenter(): SubscriptionTutorialPresenter = appInjector.getInstance()

    override fun getViewBinding() = FragmentSubscriptionTutorialBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding {
            tutorialTextView.text = tutorial
            titleTextView.text = fromHtmlToSpanned(
                getString(R.string.str_subscription_tutorial_title).replace(
                    "%tutorialTextColor%",
                    tutorialColor
                )
            )
        }
    }

    override fun showSubscriptionInfo(
        proSubscriptionInfo: ProSubscriptionInfo
    ) {

        binding {
            discountCurrencyTextView.text = proSubscriptionInfo.currency
            fullPriceCurrencyTextView.text = proSubscriptionInfo.currency
            discountTextView.text = proSubscriptionInfo.salePrice
            fullPriceTextView.text = proSubscriptionInfo.price
            subscribeButton.setOnClickListener { presenter.onAddProSubscriptionClicked(context as Activity) }

            disclaimerTextView.text = String.format(
                getString(R.string.str_subscription_tutorial_disclaimer),
                "${proSubscriptionInfo.salePrice} ${proSubscriptionInfo.currency}"
            )
        }
    }

    override fun onDismiss() {
        findNavController()
            .apply {
                popBackStack()
            }
    }

    private fun process(
        key: Pattern,
        value: Int,
        context: Context,
        spannable: Spannable,
        alignment: Int
    ): Boolean {
        var hasChanges = false
        val matcher = key.matcher(spannable)
        while (matcher.find()) {
            var changed = true
            for (span in spannable.getSpans(
                matcher.start(),
                matcher.end(),
                ImageSpan::class.java
            )) {
                if (spannable.getSpanStart(span) >= matcher.start() &&
                    spannable.getSpanEnd(span) <= matcher.end()
                )
                    spannable.removeSpan(span)
                else {
                    changed = false
                    break
                }
            }
            if (changed) {
                hasChanges = true
                spannable.setSpan(
                    CustomImageSpan(context, value, alignment),
                    matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return hasChanges
    }

    companion object {
        const val TAG = "SubTutorialFragment"
        const val CHECK_MARK_TAG = ":check_mark:"
        fun newInstance() = SubscriptionTutorialFragment()
    }
}