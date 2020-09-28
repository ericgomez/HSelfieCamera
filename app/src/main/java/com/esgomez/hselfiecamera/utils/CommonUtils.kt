package com.esgomez.hselfiecamera.utils

import android.content.Context

object CommonUtils {
    //Convirtiendo los density pixel en pixeles reales
    fun dp2px(context: Context, dipValue: Float): Float {
        return dipValue * context.resources.displayMetrics.density + 0.5f
    }
}