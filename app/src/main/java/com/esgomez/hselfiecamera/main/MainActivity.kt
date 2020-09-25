package com.esgomez.hselfiecamera.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.esgomez.hselfiecamera.R
import com.esgomez.hselfiecamera.auth.AuthActivity
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import kotlinx.android.synthetic.main.activity_home.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnLogout.setOnClickListener {
            //Logout Huawei ID
            logoutHuaweiId()
        }
    }

    //Con este metodo nos aseguramos para que el boton de hacia atras del telefono no haga nada
    //La unica forma de hacer Logout sea con el boton btnLogout
    override fun onBackPressed() {
        //No haga nada
    }

    //Funcion para hacer el Logout
    private fun logoutHuaweiId(){
        val mAuthParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .createParams()//nos crea los parametros
        //Creamos el servicio para deslogearnos de nuestra aplicacion
        val mAuthManager = HuaweiIdAuthManager.getService(this, mAuthParams)
        //Nos permitira desconectar
        val logoutTask = mAuthManager.signOut()
        logoutTask.addOnSuccessListener{
            //Le indicamos exactamente que estamos en MainActivity con this@MainActivity
            startActivity(Intent(this@MainActivity,AuthActivity::class.java))
            finish()//Finaliza el MainActivity
        }
        logoutTask.addOnFailureListener{
            Toast.makeText(this, "Logout Fallo!", Toast.LENGTH_LONG).show()
        }

    }

}