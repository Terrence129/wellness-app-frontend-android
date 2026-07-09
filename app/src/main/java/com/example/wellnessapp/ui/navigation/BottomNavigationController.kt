// Team5
// @author: Wu Aomo

package com.example.wellnessapp.ui.navigation

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import com.example.wellnessapp.R
import com.example.wellnessapp.ui.ai.AiCoachActivity
import com.example.wellnessapp.ui.home.HomeActivity
import com.example.wellnessapp.ui.log.AddWellnessLogActivity
import com.example.wellnessapp.ui.profile.ProfileActivity
import com.example.wellnessapp.ui.summary.WeeklySummaryActivity

/**
 * Adds the app's persistent bottom navigation to authenticated screens.
 */
object BottomNavigationController {

    enum class ActiveItem {
        HOME,
        TRENDS_HISTORY,
        ADD,
        AI,
        PROFILE
    }

    fun attach(
        activity: Activity,
        activeItem: ActiveItem,
        onHomeReselected: (() -> Unit)? = null
    ) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        val contentRoot = content.getChildAt(0) ?: return
        val navHeight = activity.dp(78)

        contentRoot.setPadding(
            contentRoot.paddingLeft,
            contentRoot.paddingTop,
            contentRoot.paddingRight,
            contentRoot.paddingBottom.coerceAtLeast(navHeight)
        )

        val navContainer = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                navHeight,
                Gravity.BOTTOM
            )
            clipChildren = false
            clipToPadding = false
        }

        val bottomNavigation = LinearLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                navHeight,
                Gravity.BOTTOM
            )
            background = ContextCompat.getDrawable(activity, R.drawable.bg_bottom_nav)
            elevation = activity.dp(8).toFloat()
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            setPadding(activity.dp(18), activity.dp(10), activity.dp(18), activity.dp(12))
        }

        bottomNavigation.addView(
            navItem(
                activity = activity,
                label = "Home",
                iconRes = R.drawable.ic_nav_home,
                selected = activeItem == ActiveItem.HOME
            ) {
                if (activeItem == ActiveItem.HOME) {
                    onHomeReselected?.invoke()
                } else {
                    activity.openTopLevel(HomeActivity::class.java)
                }
            }
        )
        bottomNavigation.addView(
            navItem(
                activity = activity,
                label = "Trend",
                iconRes = R.drawable.ic_nav_trends,
                selected = activeItem == ActiveItem.TRENDS_HISTORY
            ) {
                if (activeItem != ActiveItem.TRENDS_HISTORY) {
                    activity.openTopLevel(WeeklySummaryActivity::class.java)
                }
            }
        )
        bottomNavigation.addView(
            FrameLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(activity.dp(72), ViewGroup.LayoutParams.MATCH_PARENT)
                addView(
                    AppCompatImageButton(activity).apply {
                        layoutParams = FrameLayout.LayoutParams(activity.dp(56), activity.dp(56), Gravity.CENTER)
                        background = ContextCompat.getDrawable(activity, R.drawable.bg_nav_add_button)
                        contentDescription = "Add Wellness Log"
                        elevation = activity.dp(10).toFloat()
                        foreground = activity.obtainStyledSelectableBorderless()
                        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.white))
                        scaleType = ImageView.ScaleType.CENTER
                        setImageResource(R.drawable.ic_nav_add)
                        setPadding(activity.dp(13), activity.dp(13), activity.dp(13), activity.dp(13))
                        setOnClickListener {
                            if (activeItem != ActiveItem.ADD) {
                                activity.openTopLevel(AddWellnessLogActivity::class.java)
                            }
                        }
                    }
                )
            }
        )
        bottomNavigation.addView(
            navItem(
                activity = activity,
                label = "AI",
                iconRes = R.drawable.ic_nav_ai,
                selected = activeItem == ActiveItem.AI
            ) {
                if (activeItem != ActiveItem.AI) {
                    activity.openTopLevel(AiCoachActivity::class.java)
                }
            }
        )
        bottomNavigation.addView(
            navItem(
                activity = activity,
                label = "Profile",
                iconRes = R.drawable.ic_nav_profile,
                selected = activeItem == ActiveItem.PROFILE
            ) {
                if (activeItem != ActiveItem.PROFILE) {
                    activity.openTopLevel(ProfileActivity::class.java)
                }
            }
        )

        navContainer.addView(bottomNavigation)
        content.addView(navContainer)
    }

    private fun navItem(
        activity: Activity,
        label: String,
        iconRes: Int,
        selected: Boolean,
        onClick: () -> Unit
    ): LinearLayout {
        val tint = ContextCompat.getColor(
            activity,
            if (selected) R.color.health_orange else R.color.health_text_secondary
        )

        return LinearLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            foreground = activity.obtainStyledSelectableBorderless()
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            orientation = LinearLayout.VERTICAL
            setOnClickListener { onClick() }

            addView(
                ImageView(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(activity.dp(22), activity.dp(22))
                    contentDescription = null
                    imageTintList = ColorStateList.valueOf(tint)
                    setImageResource(iconRes)
                }
            )
            addView(
                TextView(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = activity.dp(2)
                    }
                    gravity = Gravity.CENTER
                    text = label
                    setTextColor(tint)
                    textSize = 11f
                }
            )
        }
    }

    private fun Activity.openTopLevel(target: Class<out Activity>) {
        val intent = Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun Activity.obtainStyledSelectableBorderless(): android.graphics.drawable.Drawable? {
        val outValue = android.util.TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
        return ContextCompat.getDrawable(this, outValue.resourceId)
    }

    private fun Activity.dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
