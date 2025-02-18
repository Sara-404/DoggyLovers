package com.proyecto.appmascotasinicial

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.appmascotasinicial.Comun.Companion.db

/**
 * Crea una notificación que avisa sobre una cita próxima
 */
class NotificacionesProgramadas : BroadcastReceiver() {

    private val user = FirebaseAuth.getInstance().currentUser

    companion object{
        //identificador de las notificaciones
        const val ID_NOTIFICATION = 1
        const val MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS = 100
        //canal para las notificaciones
        const val MI_CANAL_ID_NOTIFICACIONES = "DoggyLoversCanal"
    }

    /**
     * Se encarga de recuperar los datos de los intent que llamen a la clase para crear la notificación
     *
     * @param context Contexto de la aplicación
     * @param intent Intent de la actividad
     */
    override fun onReceive(context: Context, intent: Intent?) {
        if(context!= null && intent != null){
            val fechaCita = intent.getStringExtra("fechaCita")

            crearNotificacion(context, fechaCita)
        }
    }

    /**
     * Función encargada de crear una notificación para avisar de la fecha y hora de una cita de la base de datos
     *
     * @param context Contexto de la aplicación
     * @param fechaCita Fecha de la cita a recordar
     */
    private fun crearNotificacion(context: Context, fechaCita : String?){
        var textoDescripcion: String
        var fecha: String
        var hora: String

        if(user!= null && !fechaCita.isNullOrEmpty()){

            db.collection(Comun.colecUsuarios)
                .document(user.uid)
                .collection(Comun.colecCitas)
                .document(fechaCita.replace('/', '-'))
                .get().addOnSuccessListener { document ->
                    fecha = document.id.replace('-', '/')
                    hora = document.getString("hora").toString()
                    textoDescripcion = document.getString("descripcion").toString()

                    //para que si el usuario pulsa la app, le abra la propia app. Abre Login para que compruebe si está logueado, si es así abrirá Main (código Login)
                    val intentLogin = Intent(context, LoginActivity::class.java).apply {
                        //esto es para que si está abierta la app abra esa pero sino abra otra y no de errores y cosas raras
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent : PendingIntent = PendingIntent.getActivity(context, 0,intentLogin , PendingIntent.FLAG_IMMUTABLE)

                    //Se crea la notificación
                    val builderNotification = NotificationCompat.Builder(context, MI_CANAL_ID_NOTIFICACIONES)
                        .setSmallIcon(R.drawable.icono_doggylovers)
                        .setContentTitle("DoggyLovers: cita próxima")
                        .setContentText("Detalles de tu cita ↓")
                        .setStyle( NotificationCompat.BigTextStyle()
                            .bigText("Cita -> $textoDescripcion el día $fecha a las $hora"))
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()


                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


                    notificationManager.notify(ID_NOTIFICATION, builderNotification)

                }


        }

    }
}