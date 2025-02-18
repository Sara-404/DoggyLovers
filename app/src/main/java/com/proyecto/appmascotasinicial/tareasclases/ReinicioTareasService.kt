package com.proyecto.appmascotasinicial.tareasclases

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.appmascotasinicial.Comun
import com.proyecto.appmascotasinicial.Comun.Companion.db

/**
 * Clase que se encarga de reiniciar las tareas del usuario, que se vuelvan a marcar como no completadas
 *
 * @param context Contexto de la aplicación
 * @param params parámetros de la clase WorkerParameters
 */
class ReinicioTareasService (context: Context, params: WorkerParameters) : Worker(context, params){

    private val user = FirebaseAuth.getInstance().currentUser

    /**
     * Lógica del reinicio de tareas, recoge las tareas de la base de datos de Firebase firestore y marca
     * su estado como no completado. Además borra las tareas que no son periódicas.
     *
     * @return devuelve el resultado exitoso
     */
    override fun doWork(): Result {
        if(user!= null){
            val mascotaRef = db.collection(Comun.colecUsuarios)
                .document(user.uid)
                .collection(Comun.colecMascotas)
            mascotaRef.get()
                .addOnSuccessListener { result ->
                    for (documentoMascota in result) {
                        val subcoleccionTareas =
                            mascotaRef.document(documentoMascota.id).collection(Comun.colecTareas)
                        subcoleccionTareas.get().addOnSuccessListener { resultTarea ->
                            for (documentoTarea in resultTarea) {
                                if (documentoTarea.exists() && !documentoTarea.metadata.isFromCache) {
                                    //si la tarea no es regular, se borra por la noche
                                    if(documentoTarea.getBoolean(Comun.campoTareasPeriodicidad) == false){
                                        subcoleccionTareas.document(documentoTarea.id).delete().addOnSuccessListener {
                                            Log.d("ACT TAREA", "Tarea borrada porque no era regular")
                                        }
                                    }

                                    subcoleccionTareas.document(documentoTarea.id)
                                        .update(Comun.campoTareasEstado, false).addOnSuccessListener {
                                        Log.d("ACT TAREA", "Tareas actualizadas con éxito")
                                    }.addOnFailureListener {
                                        Log.d("ACT TAREA", "no se pudo actualizar tareas")
                                    }
                                } else {
                                    Log.d("ACT TAREA", "documento no existe o borrado")
                                }
                            }
                        }.addOnFailureListener {
                            Log.d("ACT TAREA", "no se encuentra colección tareas")
                        }
                    }
                }.addOnFailureListener { Log.d("ACT TAREA", "no se encuentra colección mascota") }
        } else {
            Log.d("ACT TAREA", "USER NULL")
        }
        return Result.success()
    }

}