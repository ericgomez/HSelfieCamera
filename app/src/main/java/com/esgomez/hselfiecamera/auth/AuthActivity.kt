package com.esgomez.hselfiecamera.auth

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.esgomez.hselfiecamera.R
import com.esgomez.hselfiecamera.main.MainActivity
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        //Establecemos un listener, de forma que cuando el botón sea presionado dé inicio la autenticación
        btnLogin.setOnClickListener {
            //Auntenticar con HuaweiID
            loginHuaweiIdAuth()
        }
    }

    private fun loginHuaweiIdAuth(){
        val mAuthParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)//capturamos los parámetros
            .setEmail() //correo del usuario
            .setAccessToken()
            .setProfile()//capturamos todas las configuraciones del Huawei ID, nickname, foto de perfil, avatar ...
            .setIdToken()//Se utilizará mas adelante para el push notification
            .setUid()//Identificador único de cada usuario de Huawei
            .setId()
            .createParams()//Se asignan todos los parámetros anteriores en un objeto, para luego ser accedidos

        /**
         * Variable para acceder al servicio de autenticación
         * recibe como parámetros el context del activity y los parámetros
         */
        val mAuthManager = HuaweiIdAuthManager.getService(this, mAuthParams)
        startActivityForResult(mAuthManager.signInIntent, 1000) //es el numero de identificacion
    }
    // Permite saber como me voy a logear y de que forma me voy a logear
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000){
            /**
             * Este if, cubre si al momento de acceder, el usuario se queda sin
             * internet o si presiona
             * alguna tecla que haga cerrar o detener el activity
             */
            if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Login Cancelado!", Toast.LENGTH_LONG).show()
            } else if (resultCode == Activity.RESULT_OK){
                //verificamos que el servicio esté funcionando correctamente
                val authHuaweiIdTasks = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
                //Si el resultado es exitoso, procederemos a autenticar
                if (authHuaweiIdTasks.isSuccessful){
                    //Toast.makeText(this, "Login Exitoso!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)//Que nos lleve a la nueva actividad creada
                    startActivity(intent)//Que no lleve aya
                    finish()//Finalizamos la actividad anterior
                }else {
                    //En caso de que no sea exitoso por que la cuenta ya no está activa,
                    // incompleta o por una falla de conexión
                    Toast.makeText(this,"Login Fallo!", Toast.LENGTH_LONG).show()

                }
            }
        }
    }
}