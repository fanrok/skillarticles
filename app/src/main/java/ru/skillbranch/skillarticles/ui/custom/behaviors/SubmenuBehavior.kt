package ru.skillbranch.skillarticles.ui.custom.behaviors

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import ru.skillbranch.skillarticles.extensions.dpToPx
import kotlin.math.hypot

class SubmenuBehavior<V: View>(context: Context, attributeSet: AttributeSet):CoordinatorLayout.Behavior<V>(context, attributeSet) {
    private var cX: Float = context.dpToPx(100)
    private var cY: Float = context.dpToPx(48)

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        if(child.visibility == View.VISIBLE){
            child.translationX = maxOf(0f, minOf(hypot(cX, cY), child.translationX + dy))
            child.translationY = maxOf(0f, minOf(hypot(cX, cY), child.translationY + dy))
        }
    }
}