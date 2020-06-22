package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import androidx.navigation.NavDestination
import com.google.android.material.bottomnavigation.BottomNavigationView

fun View.setMarginOptionally(
        left: Int = marginLeft,
        top: Int = marginTop,
        right: Int = marginRight,
        bottom: Int = marginBottom
) {
    val marginLayoutParams = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    layoutParams = marginLayoutParams.apply {
        setMargins(left, top, right, bottom)
    }
}

fun View.setPaddingOptionally(
        left: Int = paddingLeft,
        top: Int = paddingTop,
        right: Int = paddingRight,
        bottom: Int = paddingBottom
) = setPadding(left, top, right, bottom)

fun BottomNavigationView.selectDestination(destination: NavDestination) {
    menu.forEach { menuItem ->
        if (destination.id == menuItem.itemId) menuItem.isChecked = true
    }
}