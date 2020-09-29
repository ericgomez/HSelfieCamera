package com.esgomez.hselfiecamera.camera

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import com.esgomez.hselfiecamera.overlay.GraphicOverlay
import com.huawei.hms.common.size.Size
import com.huawei.hms.mlsdk.common.LensEngine
import java.io.IOException

class LensEnginePreview(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    private val mContext: Context = context
    private val mSurfaceView: SurfaceView
    private var mStartRequested: Boolean
    private var mSurfaceAvailable: Boolean
    private var mLensEngine: LensEngine? = null
    private var mOverlay: GraphicOverlay? = null //Variable que nmuestra la imagen de la camara para muchas sonrisas

    init {
        mStartRequested = false
        mSurfaceAvailable = false
        mSurfaceView = SurfaceView(context)
        //Nuestra vista va a pedir un callback para agregar el lente
        mSurfaceView.holder.addCallback(SurfaceCallback())
        this.addView(mSurfaceView)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        rigt: Int,
        bottom: Int
    ) {
        var previewWidth = 320
        var previewHeight = 240
        //Verificamos que nuestro linte exista
        if (mLensEngine != null){
            val size: Size? = mLensEngine!!.displayDimension
            if (size!=null){
                previewHeight = size.height
                previewWidth = size.width
            }
        }
        //Verificamos que nuestro contexto exista
        if (mContext.resources
                .configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        ){
            val tmp = previewWidth
            previewWidth = previewHeight
            //lo igualamos a nuestra variable temporal
            previewHeight = tmp
        }
        //Calculos con los que verificamos que nuestro layoud cumple para poder pintarse
        val viewWidth = right - left
        val viewHeight = bottom - top
        val childWidth: Int
        val childHeight: Int
        var childXOffset = 0
        var childYOffset = 0
        val widthRatio = viewWidth.toFloat() / previewWidth.toFloat()
        val heightRatio = viewHeight.toFloat() / previewHeight.toFloat()
        //Calculo para poder validar que nuestro cuadro y nuestro rostro no se vaya a salir de la camara
        if (widthRatio > heightRatio){
            childWidth = viewWidth
            childHeight = (previewHeight.toFloat() * heightRatio).toInt()//toInt() Llevamos un valor a entero
            childYOffset = (childHeight - viewHeight) / 2
        }else {
            childHeight = viewHeight
            childWidth = (previewWidth.toFloat() * heightRatio).toInt()
            childXOffset = (childWidth - viewWidth) / 2
        }

        for (i in 0 until this.childCount){
            getChildAt(i).layout(
                //-1 quiere decir que no se va a salir del limite
            -1*childXOffset, -1*childYOffset, childWidth - childXOffset,
                childHeight -childYOffset
            )
        }
        try {
            //En caso de que thodo se cumpla pintamos en el lente
            startIfReady()
        }catch (e: IOException){
            Log.e("Error:", "No pudimos uniciar la camara")
        }

    }

    //Exception en caso de que la camara no arranque
    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?, overlay: GraphicOverlay?) {
        mOverlay = overlay
        start(lensEngine)
    }

    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?) {
        if (lensEngine == null) {
            //Llamada a la funcion stop
            stop()
        }
        mLensEngine = lensEngine
        if (mLensEngine != null) {
            mStartRequested = true
            //Vamos a crea una funcion que nos va a decir
            //si la camara esta lista
            startIfReady()

        }
    }

    //Funcioin que detiene el lente de la camara
    fun stop() {
        if (mLensEngine != null) {
            mLensEngine!!.close()
        }
    }

    //Funcion para liberar la camara
    fun release() {
        if (mLensEngine != null) {
            mLensEngine!!.release()//Liberamos la camara
            mLensEngine = null//La volvemos a inicializar en nulo
        }
    }

    //Funcion para verificar que nuestra camara esta lista para vorvel a funciona despues de liberar
    @Throws(IOException::class)
    fun startIfReady() {
        if (mStartRequested && mSurfaceAvailable) {
            mLensEngine!!.run(mSurfaceView.holder)
            if (mOverlay != null) {//En el caso de la camara grupal funcione
                val size: Size = mLensEngine!!.displayDimension
                val min: Int = size.width.coerceAtMost(size.height)
                val max: Int = size.width.coerceAtLeast(size.height)
                if (Configuration.ORIENTATION_PORTRAIT == mContext.resources.configuration.orientation) {
                    mOverlay!!.setCameraInfo(min, max, mLensEngine!!.lensType)
                } else {
                    mOverlay!!.setCameraInfo(max, min, mLensEngine!!.lensType)
                }
                //Limpiar mOverlay
                mOverlay!!.clear()
            }
            mStartRequested = false
        }
    }

    //Clase innes
    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            mSurfaceAvailable = true
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e("Error: ", "No pudimos iniciar la camara $e")
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            mSurfaceAvailable = false
        }

        override fun surfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {

        }


    }

}