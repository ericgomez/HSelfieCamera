package com.esgomez.hselfiecamera.main

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esgomez.hselfiecamera.R
import com.esgomez.hselfiecamera.auth.AuthActivity
import com.huawei.hms.support.api.entity.common.CommonConstant
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import kotlinx.android.synthetic.main.activity_home.*
import java.lang.Exception
import java.lang.RuntimeException
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    //Permisos de la camara
    companion object {
        //Saber si tenemos el permiso de la camara o lo tenemos denegado
        private const val PERMISSION_REQUEST = 1
        //Funcion que nos indica si nuestro permiso esta accedido
        private fun isPermissionGranted(
            context: Context,
            permission: String?
        ): Boolean {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission!!
                ) == PackageManager.PERMISSION_GRANTED){//Si el permiso esta accedido
                return true //Retornamos True
            }
            return false //Retornamos false
        }
    }

    //Metodo para saber los permisos que requerimos en nuestra aplicacion
    private val requiredPermission: Array<String?>
    get() = try {
        val info = this.packageManager
            .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
        val ps = info.requestedPermissions
        if (ps != null && ps.isNotEmpty()){
            //Nos retornara los permisos
            ps
        }else {
            //En caso de que los permisos sean nulos
            //Nos retorna un arreglo de nulls
            arrayOfNulls(0)
        }
    } catch (e: RuntimeException){//Error en tiempo de ejecucion
        throw e
    } catch (e: Exception){//En dado caso que no pueda acceder a los permisos de la camara
        arrayOfNulls(0)
    }

    //Funcion que nos dice si todos los permisos fueron concedidos
    private fun allPermissionGranted(): Boolean {
        //For que recorre todos lo permisos y valida
        for (permission in requiredPermission){
            if (!isPermissionGranted(this, permission)){
                return false
            }
        }
        //Si todos los permisos fueron consedidos retorna verdadero
        return true
    }

    //Variable con los permisos en tiempo den ejecucion
    private val runtimePermission: Unit
    get() {
        //Validamos que todos los permisos concedidos en ejecucion
        val allPermissions: MutableList<String?> = ArrayList()
        for (permission in requiredPermission) {
            //validamos si todos los permisos no esten autorizados
            if (!isPermissionGranted(this, permission)){
                allPermissions.add(permission)
            }
            //Validar que efectivamente no sean vacios
            if (allPermissions.isNotEmpty()){
                //Solicitamos los permisos
                ActivityCompat.requestPermissions(
                    this,
                    allPermissions.toTypedArray(),
                    PERMISSION_REQUEST
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //Validar el resultado de la peticion de los permisos
        if (requestCode != PERMISSION_REQUEST){
            //Retorna null
            return
        }
        //Para saber si necesitamos o no un dialogo
        var isNeedShowDialog = false
        for (i in permissions.indices){
            if (permissions[i] == android.Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[i]
            != PackageManager.PERMISSION_GRANTED){
                isNeedShowDialog = true
            }
        }

        //Validamos si el dialogo esta en true
        //shouldShowRequestPermissionRationale Permite que si el usuario denega los permisos mostramos un nuevo dialogo diciendole esta seguro qie quiere denegar estos permisos
        if (isNeedShowDialog && !ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ){
            //Validamos que se nos muestre nuestro dialogo
            //Builder es para construir nuestro dialog en tiempo real
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setMessage("Esta aplicacion requiere acceso a tu carpeta de medios y a tu camara para poder funcionar")
                .setPositiveButton("Configuracion"){_, _ ->
                    val intent = Intent (Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResult(intent,200)
                    startActivity(intent)
                }
                    //En caso que la respuesta sea negativo
                .setNegativeButton("Cancel") {
                    //Creamos una landa y finalizamos el proceso
                    _,_ -> finish()
                }.create()
            //Mostramos nuestro dialogo
            dialog.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Validar cuando la actividad se crea si todos los permisos ya fueron concedidos
        if (!allPermissionGranted()){
            runtimePermission
        }

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