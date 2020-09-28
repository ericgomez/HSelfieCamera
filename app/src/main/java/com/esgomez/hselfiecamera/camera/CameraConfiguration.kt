package com.esgomez.hselfiecamera.camera

open class CameraConfiguration {
    var fps = 20.0f
    var previewHeight: Int? = null
    var isAutoFocus = true

    //Funcion Synchronized necesitamos que sea sincronizada porque esta funcion sera en tiempo real
    @Synchronized
    fun setCameraFacing (facing: Int){
        //En caso de que la caamara no este aciva en la camara trasera o frontal quiere decir que
        //la camara esta trabada y es una camara invalida
        require(!(facing != CAMERA_FACING_BACK && facing != CAMERA_FACING_FRONT)){
            "Invald Camera: $facing"
        }

        cameraFacing = facing
    }

    companion object {
        //Podemos accesar por medio de nuestros companion object y podemos eliminar los imports
        val CAMERA_FACING_BACK: Int = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
        val CAMERA_FACING_FRONT: Int = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT

        //Crear Variable sincornizada para que solo pueda ser accedida en tiempo de ejecucion y no en cualquier momento
        //Para evitar null pointer exception
        @get:Synchronized
        var cameraFacing = CAMERA_FACING_BACK
            protected set
    }


}