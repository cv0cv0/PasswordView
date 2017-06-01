package com.gr.passwordview.util

import android.util.TypedValue
import android.view.View

/**
 * Created by gr on 2017/5/22.
 */
fun View.dp2px(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
}

fun View.sp2px(sp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
}
