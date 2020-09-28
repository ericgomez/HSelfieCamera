package com.esgomez.hselfiecamera.overlay

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.esgomez.hselfiecamera.camera.CameraConfiguration
import java.util.*
import kotlin.collections.ArrayList

//Creamos un constructor con dos variables
class GraphicOverlay(
    context: Context,
    atts: AttributeSet?//Puede ser nula
): View(context, atts) {
    //Creamos variable para blqouear la camera en el momento que se tome la foto
    private val lock = Any()
    private var previewWidth = 0
    private var previewHeight = 0
    var widthScaleValue = 1.0f
        private  set //El private set Quiere decir que solo puede ser accedido por la camara cuando inicialize
    var heightScaleValue = 1.0f
        private set
    //Cuando camera facing este en front va a tomas la selfie
    var cameraFacing = CameraConfiguration.CAMERA_FACING_FRONT
        private set

    private val graphics: MutableList<BaseGraphic> = ArrayList()

    fun addGraphic(graphic: BaseGraphic){
        synchronized(lock) {graphics.add(graphic)}
    }
    //Con esta funcion le decimos a la camara que puede volver a reutilizarse
    fun clear(){
        synchronized(lock) {graphics.clear()}
        this.postInvalidate()
    }

    //Setear la informacion de la camara
    fun setCameraInfo (width: Int, height: Int, facing: Int ){
        synchronized(lock){
            previewWidth =  width
            previewHeight = height
            cameraFacing = facing
        }
        //Cuando esta funcion deje de funcionar postInvalidate esto para que no se quede pegada la sincronizacion
        // en un lup infinito
        this.postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //Crea un cuadrito para rodrear nuestro rostro de nuestra selfie cam
        synchronized(lock){
            //Si previewWidth y previewHeight son mayores que cero va a hacer algo
            if (previewWidth != 0 && previewHeight != 0 ){
                widthScaleValue =
                    width.toFloat() / previewWidth.toFloat()
                heightScaleValue =
                    height.toFloat() / previewHeight.toFloat()
            }
            //Paracada grafica en graphic nos dibuje nuestro cuadrito que nos va a bordear nuestro rostro en la camara
            for (graphic in graphics){
                graphic.draw(canvas)
            }
        }
    }


}