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

class LensEnginePreview(context: Context, attrs: AttributeSet?)
    : ViewGroup(context,attrs) {

    private val mContext: Context = context
    private val mSurfaceView: SurfaceView
    private var mStartRequested: Boolean
    private var mSurfaceAvailable: Boolean
    private var mLensEngine: LensEngine? = null
    private var mOverlay: GraphicOverlay? = null

    init {
        mStartRequested = false
        mSurfaceAvailable = false
        mSurfaceView = SurfaceView(context)
        //Nuestra vista va a pedir un callback para agregar el lente
        this.addView(mSurfaceView)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        TODO("Not yet implemented")
    }

    //Exception en caso de que la camara no arranque
    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?, overlay: GraphicOverlay?){
        mOverlay = overlay
        start(lensEngine)
    }

    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?){
        if (lensEngine == null){
            //Llamada a la funcion stop
            stop()
        }
        mLensEngine = lensEngine
        if (mLensEngine!=null){
            mStartRequested = true
            //Vamos a crea una funcion que nos va a decir
            //si la camara esta lista

        }
    }
    //Funcioin que detiene el lente de la camara
    fun stop() {
        if(mLensEngine != null){
            mLensEngine!!.close()
        }
    }

    //Funcion para liberar la camara
    fun release(){
        if (mLensEngine!=null){
            mLensEngine!!.release()//Liberamos la camara
            mLensEngine = null//La volvemos a inicializar en nulo
        }
    }

    //Funcion para verificar que nuestra camara esta lista para vorvel a funciona despues de liberar
    @Throws(IOException::class)
    fun startIfReady(){
        if (mStartRequested && mSurfaceAvailable){
            mLensEngine!!.run(mSurfaceView.holder)
            if (overlay!= null){
                val size: Size = mLensEngine!!.displayDimension
                val min: Int = size.width.coerceAtMost(size.height)
                val max: Int = size.width.coerceAtLeast(size.height)
                if (Configuration.ORIENTATION_PORTRAIT == mContext.resources.configuration.orientation){
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
    private inner class SurfaceCallback: SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            mSurfaceAvailable = true
            try {
                startIfReady()
            } catch (e: IOException){
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