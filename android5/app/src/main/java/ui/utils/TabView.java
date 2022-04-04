package ui.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.PointerIconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;

import com.fulldive.evry.presentation.tabs.AnimationUtils;
import com.fulldive.evry.presentation.tabs.RippleUtils;

import org.adshield.R;

public final class TabView extends LinearLayout {
    private TabLayout tabLayout;
    private Tab tab;
    private TextView textView;

    @Nullable
    private View customView;
    @Nullable
    private TextView customTextView;
    @Nullable
    private ImageView customImageView;
    @Nullable
    private Drawable baseBackgroundDrawable;

    @Nullable
    private Typeface robotoMedium;
    @Nullable
    private Typeface robotoRegular;

    public TabView(TabLayout tabLayout, @NonNull Context context) {
        super(context);
        this.tabLayout = tabLayout;
        setWillNotDraw(false);

        updateBackgroundDrawable(context);
        ViewCompat.setPaddingRelative(
                this, tabLayout.tabPaddingStart, tabLayout.tabPaddingTop, tabLayout.tabPaddingEnd, tabLayout.tabPaddingBottom);
        setGravity(Gravity.CENTER);
        setOrientation(tabLayout.inlineLabel ? HORIZONTAL : VERTICAL);
        setClickable(true);
        ViewCompat.setPointerIcon(
                this,
                PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND)
        );
        ViewCompat.setAccessibilityDelegate(this, null);
        setTypefaces(context);
    }

    private float getScaleImpact() {
        return tabLayout.tabScaleImpactUnselected;
    }

    void updateBackgroundDrawable(Context context) {
        if (tabLayout.tabBackgroundResId != 0) {
            baseBackgroundDrawable = AppCompatResources.getDrawable(context, tabLayout.tabBackgroundResId);
            if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
                baseBackgroundDrawable.setState(getDrawableState());
            }
        } else {
            baseBackgroundDrawable = null;
        }

        Drawable background;
        Drawable contentDrawable = new GradientDrawable();
        ((GradientDrawable) contentDrawable).setColor(Color.TRANSPARENT);

        if (tabLayout.tabRippleColorStateList != null) {
            GradientDrawable maskDrawable = new GradientDrawable();
            // TODO: Find a workaround for this. Currently on certain devices/versions,
            // LayerDrawable will draw a black background underneath any layer with a non-opaque color,
            // (e.g. ripple) unless we set the shape to be something that's not a perfect rectangle.
            maskDrawable.setCornerRadius(0.00001F);
            maskDrawable.setColor(Color.WHITE);

            ColorStateList rippleColor = RippleUtils.convertToRippleDrawableColor(tabLayout.tabRippleColorStateList);

            // TODO: Add support to RippleUtils.compositeRippleColorStateList for different ripple color
            // for selected items vs non-selected items
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                background =
                        new RippleDrawable(
                                rippleColor,
                                tabLayout.unboundedRipple ? null : contentDrawable,
                                tabLayout.unboundedRipple ? null : maskDrawable);
            } else {
                Drawable rippleDrawable = DrawableCompat.wrap(maskDrawable);
                DrawableCompat.setTintList(rippleDrawable, rippleColor);
                background = new LayerDrawable(new Drawable[]{contentDrawable, rippleDrawable});
            }
        } else {
            background = contentDrawable;
        }
        ViewCompat.setBackground(this, background);
        this.invalidate();
    }

    /**
     * Draw the background drawable specified by tabBackground attribute onto the canvas provided.
     * This method will draw the background to the full bounds of this TabView. We provide a
     * separate method for drawing this background rather than just setting this background on the
     * TabView so that we can control when this background gets drawn. This allows us to draw the
     * tab background underneath the TabLayout selection indicator, and then draw the TabLayout
     * content (icons + labels) on top of the selection indicator.
     *
     * @param canvas canvas to draw the background on
     */
    void drawBackground(@NonNull Canvas canvas) {
        if (baseBackgroundDrawable != null) {
            baseBackgroundDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
            baseBackgroundDrawable.draw(canvas);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        boolean changed = false;
        int[] state = getDrawableState();
        if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
            changed = baseBackgroundDrawable.setState(state);
        }

        if (changed) {
            invalidate();
            this.invalidate(); // Invalidate TabLayout, which draws mBaseBackgroundDrawable
        }
    }

    @Override
    public boolean performClick() {
        final boolean handled = super.performClick();

        if (tab != null) {
            if (!handled) {
                playSoundEffect(SoundEffectConstants.CLICK);
            }
            tab.select();
            return true;
        } else {
            return handled;
        }
    }

    @Override
    public void setSelected(final boolean selected) {
        super.setSelected(selected);

        if (textView != null) {
            textView.setSelected(selected);
        }

        if (customView != null) {
            customView.setSelected(selected);
        }

        if (customImageView != null) {
            customImageView.setSelected(selected);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        // This view masquerades as an action bar tab.
        event.setClassName(ActionBar.Tab.class.getName());
    }

    @TargetApi(14)
    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        // This view masquerades as an action bar tab.
        info.setClassName(ActionBar.Tab.class.getName());
    }

    @Override
    public void onMeasure(final int origWidthMeasureSpec, final int origHeightMeasureSpec) {
        final int specWidthSize = MeasureSpec.getSize(origWidthMeasureSpec);
        final int specWidthMode = MeasureSpec.getMode(origWidthMeasureSpec);
        final int maxWidth = tabLayout.getTabMaxWidth();

        final int widthMeasureSpec;
        final int heightMeasureSpec = origHeightMeasureSpec;

        if (maxWidth > 0 && (specWidthMode == MeasureSpec.UNSPECIFIED || specWidthSize > maxWidth)) {
            // If we have a max width and a given spec which is either unspecified or
            // larger than the max width, update the width spec using the same mode
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(tabLayout.tabMaxWidth, MeasureSpec.AT_MOST);
        } else {
            // Else, use the original width spec
            widthMeasureSpec = origWidthMeasureSpec;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    void reset() {
        setTab(null);
        setSelected(false);
    }

    final void update() {
        final Tab tab = this.tab;
        final View custom = tab != null ? tab.getCustomView() : null;
        if (custom != null) {
            final ViewParent customParent = custom.getParent();
            if (customParent != this) {
                if (customParent != null) {
                    ((ViewGroup) customParent).removeView(custom);
                }
                addView(custom);
            }
            customView = custom;
            if (this.textView != null) {
                this.textView.setVisibility(GONE);
            }
            customTextView = custom.findViewById(android.R.id.text1);
            customImageView = custom.findViewById(android.R.id.icon);
        } else {
            // We do not have a custom view. Remove one if it already exists
            if (customView != null) {
                removeView(customView);
                customView = null;
            }
            customTextView = null;
            customImageView = null;
        }

        if (customView == null) {
            if (this.textView == null) {
                inflateAndAddDefaultTextView();
            }
            updateText(this.textView);
        } else if (customTextView != null) {
            this.textView.setVisibility(View.GONE);
            updateText(customTextView);
        }

        if (tab != null && !TextUtils.isEmpty(tab.getContentDesc())) {
            setContentDescription(tab.getContentDesc());
        }
        setSelected(tab != null && tab.isSelected());
        if (tab != null) {
            updateSize();
            updateFont();
        }
    }

    private void inflateAndAddDefaultTextView() {
        ViewGroup textViewParent = this;
        this.textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.layout_tab_text, textViewParent, false);
        textViewParent.addView(textView);
    }

    final void updateOrientation() {
        setOrientation(tabLayout.inlineLabel ? HORIZONTAL : VERTICAL);
        if (customTextView != null) {
            updateText(customTextView);
        }
    }

    private void setTypefaces(Context context) {
        robotoMedium = ResourcesCompat.getFont(context, R.font.roboto_medium);
        robotoRegular = ResourcesCompat.getFont(context, R.font.roboto_regular);
    }

    private void updateText(@Nullable final TextView textView) {
        final CharSequence text = tab != null ? tab.getText() : null;
        final boolean hasText = !TextUtils.isEmpty(text);
        if (textView != null) {
            TextViewCompat.setTextAppearance(textView, tabLayout.tabTextAppearance);
            if (tabLayout.tabTextColors != null) {
                textView.setTextColor(tabLayout.tabTextColors);
            }
            if (hasText) {
                textView.setText(text);
                if (tab.getTabLabelVisibility() == TabLayout.TAB_LABEL_VISIBILITY_LABELED) {
                    textView.setVisibility(VISIBLE);
                } else {
                    textView.setVisibility(GONE);
                }
                setVisibility(VISIBLE);
            } else {
                textView.setVisibility(GONE);
                textView.setText(null);
            }
        }

        final CharSequence contentDesc = tab != null ? tab.getContentDesc() : null;
        TooltipCompat.setTooltipText(this, hasText ? null : contentDesc);
    }

    int getContentWidth() {
        boolean initialized = false;
        int left = 0;
        int right = 0;

        for (View view : new View[]{textView, customView}) {
            if (view != null && view.getVisibility() == View.VISIBLE) {
                left = initialized ? Math.min(left, view.getLeft()) : view.getLeft();
                right = initialized ? Math.max(right, view.getRight()) : view.getRight();
                initialized = true;
            }
        }

        return right - left;
    }

    @Nullable
    public Tab getTab() {
        return tab;
    }

    void setTab(@Nullable final Tab tab) {
        if (tab != this.tab) {
            this.tab = tab;
            update();
        }
    }

    public void updateTextSize(float impact) {
        float scale = AnimationUtils.INSTANCE.lerp(1f - getScaleImpact(), 1f, impact);
        if (textView != null) {
            textView.setScaleX(scale);
            textView.setScaleY(scale);
        }
        if (customTextView != null) {
            customTextView.setScaleX(scale);
            customTextView.setScaleY(scale);
        }
    }

    public void updateSize() {
        float impact = 0f;
        if (tab != null && tab.isSelected()) {
            impact = 1f;
        }
        updateTextSize(impact);
    }

    public void updateFont() {
        boolean isSelected = false;
        if (tab != null) {
            isSelected = tab.isSelected();
        }
        if (customTextView != null)
            customTextView.setTypeface(isSelected ? robotoMedium : robotoRegular);
        if (textView != null) textView.setTypeface(isSelected ? robotoMedium : robotoRegular);
    }
}
