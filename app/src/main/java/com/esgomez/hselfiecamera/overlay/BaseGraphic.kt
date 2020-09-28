package com.esgomez.hselfiecamera.overlay

import android.graphics.Canvas
import com.huawei.hms.mlsdk.common.LensEngine

abstract class BaseGraphic(private val graphicOverlay: GraphicOverlay) {
    abstract fun draw(canvas: Canvas?)

    fun scaleX(x:Float): Float {
        return x * graphicOverlay.widthScaleValue
    }

    fun scaleY(y: Float): Float {
        return y * graphicOverlay.heightScaleValue
    }

    //traslade para que nuestro rostro quede totalmente centrado en nuestro selfie
    fun translateX(x: Float): Float{
        return if (graphicOverlay.cameraFacing == LensEngine.FRONT_LENS){//si esta abierta la camara frontal
            //Se traslada y escala
            graphicOverlay.width - scaleX(x)
        }else {//Si esta en la camara trasera
            //Solo se escala
            scaleX(x)
        }
    }

    fun translateY(y: Float): Float {
        return scaleY(y)
    }
}