package com.esgomez.hselfiecamera.push

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import java.lang.Exception
import kotlin.concurrent.thread

//NOTA: Para poder obtener el Token de HMS Push kit ejecutamos la aplicacion y lo buscamos en el Logcat como Debug
//Buscar el nombre Huawei push token mismo que contiene una cadena ese es el token, el token expira cada 6 meses

class HuaweiPushService: HmsMessageService() {

    companion object {
        private const val TAG = "HuaweiPushService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        //Logeamos el token
        Log.d(TAG, "Mensaje Recibido")
        remoteMessage?.let {
            //Validamos que nuestro mensaje recive datos
            Log.d(TAG, " - Data ${it.data}")
        }
    }

    //funciona cuando el tocket de Push Notificacion expira, generalmente expira cada 6 meses
    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        //Logeamos el token
        Log.d(TAG, "Huawei push token: $token")
    }
}

//Creamos nueva clase para validar nuestro token
class GetTokenAction() {
    //Este handler se encargara de enviarnos el token en modo sincrono quiere decir que esperara unos segundos antes de enviar el push notificacion
    private val handler: Handler = Handler(Looper.getMainLooper())
    fun getToken(context: Context, callback: (String) -> Unit) {
        thread {//Lo recibimos en un hilo
            try {
                //Para poder recibir el token debemos tener un AppID
                //"client/app_id" es el parametro que se le pasa a la nube de Huawei para saber que nuestra aplicacion exite en AppGalery
                val appID = AGConnectServicesConfig.fromContext(context).getString("client/app_id")
                //Traemos nuestro token desde la nube de Huawei donde "HCM" son las siglas de la nuve de Huawei
                //que se esta conectando pormedio de Push Notificacion
                val token = HmsInstanceId.getInstance(context).getToken(appID, "HCM")
                //Nuestro handler va a publicar en el callback nuestro token
                handler.post { callback(token) }
            } catch (err: Exception) {//no muestra un error en caso de existir
                Log.e("Error:", err.toString())
            }
        }
    }
}