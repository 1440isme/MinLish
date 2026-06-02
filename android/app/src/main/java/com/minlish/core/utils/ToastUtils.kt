package com.minlish.core.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast

/**
 * Displays a system Toast shifted upwards to prevent overlapping with the bottom navigation bar (island or docked).
 */
fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
    try {
        val toast = Toast.makeText(context, message, duration)
        // Set gravity to display above the bottom navigation bar (approx 320px offset)
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 320)
        toast.show()
    } catch (e: Exception) {
        // Fallback to standard toast if gravity modification fails
        Toast.makeText(context, message, duration).show()
    }
}
