package com.esgomez.hselfiecamera.face

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.esgomez.hselfiecamera.R
import com.esgomez.hselfiecamera.camera.LensEnginePreview
import com.esgomez.hselfiecamera.overlay.GraphicOverlay
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting

class LiveFaceActivityCamera : AppCompatActivity() {

    //Objeto para analizar nuestros rostros
    private var analyzer: MLFaceAnalyzer? = null
    private var mLensEngine: LensEngine? = null
    private var mPreview: LensEnginePreview? = null
    private var overlay: GraphicOverlay? = null
    private var lensType = LensEngine.FRONT_LENS//Inicializamos la camara en frontal
    private var restart: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_face_camera)

        //Verificar el estado
        if (savedInstanceState != null){
            lensType = savedInstanceState.getInt("-lensType")
        }
        mPreview = findViewById(R.id.preview)
        overlay = findViewById(R.id.faceOverlay)
        restart = findViewById(R.id.restart)

        //Funciona como el motor de nuestro lente
        createLensEngine()
    }

    private fun createLensEngine(){
        //Poder analizar en que posision se encuntra nuestro Rostro
        val setting = MLFaceAnalyzerSetting.Factory()
            .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
            .setKeyPointType(MLFaceAnalyzerSetting.TYPE_UNSUPPORT_KEYPOINTS)
            .setMinFaceProportion(0.1F)//MAntiene una proporcion minima
            .setTracingAllowed(true)//Si se encuntra en movimiento siguie el movimiento
            .create()
        analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting)
        val context: Context = this.applicationContext
        mLensEngine = LensEngine.Creator(context, analyzer).setLensType(lensType)
            .applyDisplayDimension(640, 480)
            .applyFps(25.0f)
            .enableAutomaticFocus(true)
            .create()
    }
}