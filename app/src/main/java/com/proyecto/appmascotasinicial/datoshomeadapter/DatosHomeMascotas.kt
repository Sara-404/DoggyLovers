package com.proyecto.appmascotasinicial.datoshomeadapter

/**
 * DataClass que representa una mascota
 *
 * @param nombre Nombre de la mascota
 * @param fechaNacimiento Fecha de nacimiento de la mascota
 * @param especie Especie de la mascota
 */
data class DatosHomeMascotas(val nombre: String,
                             val fechaNacimiento: String,
                             val especie :String)