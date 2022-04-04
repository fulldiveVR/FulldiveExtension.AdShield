package ui.utils

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.fulldive.evry.presentation.tabs.runIfNotNullable

class Tab {
    var tag: Any? = null
        private set
    var text: CharSequence? = null
    var contentDesc: CharSequence? = null
    var position = INVALID_POSITION

    var customView: View? = null
        private set

    val isSelected: Boolean
        get() {
            requireNotNull(parent) { "Tab not attached to a TabLayout" }
            return parent?.selectedTabPosition == position
        }


    @TabLayout.LabelVisibility
    var tabLabelVisibility = TabLayout.TAB_LABEL_VISIBILITY_LABELED

    var parent: TabLayout? = null

    var view: TabView? = null

    fun setCustomView(view: View?, update: Boolean = true): Tab {
        runIfNotNullable(customView, customView?.parent as? ViewGroup) { currentView, parent ->
            parent.removeView(currentView)
        }
        customView = view
        if (update) {
            updateView()
        }
        return this
    }

    fun setCustomView(@LayoutRes resId: Int): Tab {
        val inflater = LayoutInflater.from(view!!.context)
        return setCustomView(inflater.inflate(resId, view, false))
    }

    fun setTabText(text: CharSequence?): Tab {
        if (TextUtils.isEmpty(contentDesc) && !TextUtils.isEmpty(text)) {
            view?.contentDescription = text
        }
        this.text = text
        updateView()
        return this
    }

    fun setTabText(@StringRes resId: Int): Tab {
        requireNotNull(parent) { "Tab not attached to a TabLayout" }
        return setTabText(parent!!.resources.getText(resId))
    }

    fun setTabLabelVisibility(@TabLayout.LabelVisibility mode: Int): Tab {
        tabLabelVisibility = mode
        if (parent?.tabGravity == TabLayout.GRAVITY_CENTER || parent?.mode == TabLayout.MODE_AUTO) {
            parent?.updateTabViews(true)
        }
        updateView()
        return this
    }

    fun select() {
        requireNotNull(parent) { "Tab not attached to a TabLayout" }
        parent?.selectTab(this)
    }

    fun setContentDescription(contentDesc: CharSequence?): Tab {
        this.contentDesc = contentDesc
        updateView()
        return this
    }

    fun updateView() {
        view?.update()
    }

    fun reset() {
        parent = null
        view = null
        tag = null
        text = null
        contentDesc = null
        position = INVALID_POSITION
        customView = null
    }

    fun updateSize() {
        view?.updateSize()
    }

    fun updateFont() {
        view?.updateFont()
    }

    companion object {
        const val INVALID_POSITION = -1
    }
}