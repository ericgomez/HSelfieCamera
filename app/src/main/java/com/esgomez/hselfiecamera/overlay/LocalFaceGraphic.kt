package com.esgomez.hselfiecamera.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.huawei.hms.mlsdk.face.MLFace
import com.huawei.hms.mlsdk.face.MLFaceShape
import com.esgomez.hselfiecamera.utils.CommonUtils.dp2px


class LocalFaceGraphic(
    private val overlay: GraphicOverlay,
    @field:Volatile private var face:MLFace?,
    private val mContext: Context
) : BaseGraphic(overlay) {

    private val facePaint: Paint

    init {
        //Inicializamos el cuadro
        val lineWith = dp2px(mContext, 1f)
        facePaint = Paint()
        //Color de la linea cuando este dentro de la cara sera de un color Rojo
        facePaint.color = Color.parseColor("#EF484B")
        facePaint.style = Paint.Style.STROKE
        facePaint.strokeWidth = lineWith
    }
    override fun draw(canvas: Canvas?) {
        //pintar el cuadro
        //Si nuestra cara es vacia nos retorna Nulo
        if (face == null){
            return
        }

        val faceShape = face!!.getFaceShape(MLFaceShape.TYPE_FACE)
        val points = faceShape.points

        var verticalMin = Float.MAX_VALUE
        var verticalMax = 0f
        var horizontalMin = Float.MAX_VALUE
        var horizontalMax = 0f

        for (i in points.indices){
            val point = points[i]?: continue
            if (point.x != null && point.y != null){
                if (point.x > horizontalMax) horizontalMax = point.x
                if (point.x < horizontalMin) horizontalMin = point.x
                if (point.y > verticalMax) verticalMax = point.y
                if (point.y < verticalMin) verticalMin = point.y
            }
        }
        //Creamos el rectangulo
        val rect = Rect (
            translateX(horizontalMin).toInt(),
            translateY(verticalMin).toInt(),
            translateX(horizontalMax).toInt(),
            translateY(verticalMax).toInt()
        )
        canvas!!.drawRect(rect, facePaint)
    }

}