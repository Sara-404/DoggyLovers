package com.proyecto.appmascotasinicial.tareasclases

/**
 * Define una tarea
 *
 * @param idTarea Id de la tarea
 * @param estado Estado de la tarea (true = completada, false = no completada)
 * @param descripcion Descripción de la tarea
 * @param nombreMascota Nombre de la mascota a la que pertenece esa tarea
 * @param regular Valor que indica si la tarea es periódica o no (true = periódica, false = no periódica)
 */
data class TareasDatos (var idTarea : Int, var estado : Boolean, var descripcion : String, var nombreMascota : String, var regular : Boolean)
