package com.esgomez.hselfiecamera.face

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.esgomez.hselfiecamera.R
import com.esgomez.hselfiecamera.camera.LensEnginePreview
import com.esgomez.hselfiecamera.overlay.GraphicOverlay
import com.esgomez.hselfiecamera.overlay.LocalFaceGraphic
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.common.MLResultTrailer
import com.huawei.hms.mlsdk.face.MLFace
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting
import com.huawei.hms.mlsdk.face.MLMaxSizeFaceTransactor
import java.io.IOException
import java.lang.RuntimeException

class LiveFaceActivityCamera : AppCompatActivity() {

    //Objeto para analizar nuestros rostros
    private var analyzer: MLFaceAnalyzer? = null
    private var mLensEngine: LensEngine? = null
    private var mPreview: LensEnginePreview? = null
    private var overlay: GraphicOverlay? = null
    private var lensType = LensEngine.FRONT_LENS//Inicializamos la camara en frontal
    private var detectMode = 0
    private val smilingPossibility = 0.95f //Este valor osila en el 80% y el 90% el 95% es si verdaderamente estas sonriendo
    private var safeToTakePicture = false //Variable para detectar si es seguro o no es seguro tomar la foto
    private var restart: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_face_camera)

        //Verificar el estado
        if (savedInstanceState != null){
            lensType = savedInstanceState.getInt("-lensType")
        }
        mPreview = findViewById(R.id.preview)
        val intent = this. intent
        try {
            detectMode = intent.getIntExtra("detect_mode", 1)
        } catch (e:RuntimeException) {//Exception en tiempo de ejecucion
            Log.e("Error:", "No puedo traer el codigo de deteccion")
        }

        overlay = findViewById(R.id.faceOverlay)
        restart = findViewById(R.id.restart)

        //Creamos el analizador de rostro
        createFaceAnalizer()
        //Funciona como el motor de nuestro lente
        createLensEngine()
    }

    override fun onResume() {
        super.onResume()
        //Inicializa el lente
        startLensEngine()
    }

    override fun onPause() {
        super.onPause()
        //Detenemos la previsaulizcion
        mPreview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        //Si el lente es diferente de null quiere decir que existe
        if (mLensEngine != null){
            //Lo liberamos
            mLensEngine!!.release()
        }

    }

    //Obtiene el estado guardado
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("lensType", lensType)//Nos permite saber si viene en Front o en Back  la camara
        super.onSaveInstanceState(outState)
    }

    //Esta funcion se va a encarga identificar si te encuentras dentro de las posibilidades de sorisa
    // o te encuentras muy cerca de la camara o debe de tomas la foto si esta en modo grupo
    private fun createFaceAnalizer() {
        //Para poder analizar en que posisicion se encuentra nuestro rostro
        val setting = MLFaceAnalyzerSetting.Factory()
            .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
            .setKeyPointType(MLFaceAnalyzerSetting.TYPE_UNSUPPORT_KEYPOINTS)
            .setMinFaceProportion(0.1f)//Mantiene una proporcion minima
            .setTracingAllowed(true)//Si se encuetra en moviemnto sigue el movimento
            .create()
        analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting)
        if (detectMode == 1003){
            val transactor =
                //Recive la provavilida de sonriza
                MLMaxSizeFaceTransactor.Creator(analyzer, object : MLResultTrailer<MLFace?>(){
                    override fun objectCreateCallback(
                        itemId: Int,
                        obj: MLFace?
                    ) {
                        overlay!!.clear()//overlay se limpia
                        if (obj == null){
                            return//No retorna nada si no esta reconociendo una cara
                        }
                        val faceGraphic = LocalFaceGraphic(
                            overlay!!,
                            obj,
                            this@LiveFaceActivityCamera
                        )
                        overlay!!.addGraphic(faceGraphic)
                        //Reconosca nuestra sorisa para tomar la foto
                        val emotion = obj.emotions
                        if (emotion.smilingProbability > smilingPossibility) {
                            safeToTakePicture = true
                        }
                    }

                    override fun objectUpdateCallback(
                        var1: MLAnalyzer.Result<MLFace?>?,
                        obj: MLFace?
                    ) {
                        overlay!!.clear()
                        if (obj == null){
                            return
                        }
                        val faceGraphic = LocalFaceGraphic(
                            overlay!!,
                            obj,
                            this@LiveFaceActivityCamera
                        )
                        overlay!!.addGraphic(faceGraphic)
                        //Reconosca nuestra sorisa para tomar la foto
                        val emotion = obj.emotions
                        if (emotion.smilingProbability > smilingPossibility && safeToTakePicture) {
                            safeToTakePicture = true
                        }
                    }

                    //En caso de que perdamos la coneccion con nuestra camara
                    override fun lostCallback(result: MLAnalyzer.Result<MLFace?>?) {
                        overlay!!.clear()
                    }

                    override fun completeCallback() {
                        overlay!!.clear()
                    }
                }).create()
            analyzer!!.setTransactor(transactor)
        }
        else {
            //validar los rostros grupal
        }
    }

    //Funcion que funciona como Motor de lente
    private fun createLensEngine(){
        //Poder analizar en que posision se encuntra nuestro Rostro
        /*val setting = MLFaceAnalyzerSetting.Factory()
            .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
            .setKeyPointType(MLFaceAnalyzerSetting.TYPE_UNSUPPORT_KEYPOINTS)
            .setMinFaceProportion(0.1F)//MAntiene una proporcion minima
            .setTracingAllowed(true)//Si se encuntra en movimiento siguie el movimiento
            .create()
        analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting)*/
        val context: Context = this.applicationContext
        mLensEngine = LensEngine.Creator(context, analyzer).setLensType(lensType)
            .applyDisplayDimension(640, 480)
            .applyFps(25.0f)
            .enableAutomaticFocus(true)
            .create()
    }

    //Inicializamos el motor de lente
    private fun startLensEngine() {
        restart!!.visibility = View.GONE//Tipo de visualizacion
        if (mLensEngine!=null){
            try {
                //Validamos que nuestro detectMode viene con que codigo
                if (detectMode == 1003){
                    mPreview!!.start(mLensEngine, overlay)//Le decimos a nuestro mPreview que lo inicialize nuevamente mLensEngine y overlay
                } else {
                    //En caso de que venga otro codigo puede ser 1002 le decimos que solo inicialize mLensEngine
                    mPreview!!.start(mLensEngine)
                }

            } catch (e:IOException) {//En dado caso que nuestro lente tenga un error
                //Liberamos el lente
                mLensEngine!!.release()
                //Que lo regrese a nulo
                mLensEngine = null
            }
        }

        //inicializar previsualizacion
        fun startPreview(view: View?){
            mPreview!!.release()//mPreview este liberado
            createFaceAnalizer()
            createLensEngine()//Vamos a crear nuestro lente
            startLensEngine()//Y vamos a inicializar nuestro lente
        }
    }
}