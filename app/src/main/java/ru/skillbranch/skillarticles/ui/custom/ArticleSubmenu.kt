package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import com.google.android.material.shape.MaterialShapeDrawable
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.ui.custom.behaviors.SubmenuBehavior
import kotlin.math.hypot

class ArticleSubmenu @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {
    var isOpen = false

    private var centerX: Float = context.dpToPx(200)
    private var centerY: Float = context.dpToPx(96)

    init {
        View.inflate(context, R.layout.layout_submenu, this)
        //add material bg for handle elevation and color surface
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<ArticleSubmenu> {
        return SubmenuBehavior()
    }

    fun open() {
        if (isOpen || !isAttachedToWindow) return
        isOpen = true
        animatedShow()
    }

    fun close() {
        if (!isOpen || !isAttachedToWindow) return
        isOpen = false
        animatedHide()
    }

    private fun animatedShow() {
        val endRadius = hypot(centerX, centerY).toInt()
        val anim = ViewAnimationUtils.createCircularReveal(
                this,
                centerX.toInt(),
                centerY.toInt(),
                0F,
                endRadius.toFloat()
        )
        anim.doOnEnd {
            visibility = View.VISIBLE
        }
        anim.start()
    }

    private fun animatedHide() {
        val endRadius = hypot(centerX, centerY).toInt()
        val anim = ViewAnimationUtils.createCircularReveal(
                this,
                centerX.toInt(),
                centerY.toInt(),
                endRadius.toFloat(),
                0F
        )
        anim.doOnEnd {
            visibility = View.GONE
        }
        anim.start()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.ssIsOpen = isOpen
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            isOpen = state.ssIsOpen
            visibility = if (isOpen) View.VISIBLE else View.GONE
        }
    }

    private class SavedState : BaseSavedState, Parcelable {
        var ssIsOpen: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(src: Parcel) : super(src) {
            ssIsOpen = src.readInt() == 1
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeInt(if (ssIsOpen) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }


    }
}