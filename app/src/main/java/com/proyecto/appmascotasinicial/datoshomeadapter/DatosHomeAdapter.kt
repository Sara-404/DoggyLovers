package com.proyecto.appmascotasinicial.datoshomeadapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.appmascotasinicial.HomeFragment
import com.proyecto.appmascotasinicial.R

/**
 * Adaptador del RecyclerView del fragment HomeFragment que muestra una lista de mascotas del usuario
 *
 * @param dataList Lista de mascotas que va a mostrar
 * @param onClickDelete Función que indica lo que hacer en caso de elemento eliminado
 */
class DatosHomeAdapter(
    private var dataList: MutableList<DatosHomeMascotas>,
    private val onClickDelete: (Int) -> Unit
) : RecyclerView.Adapter<DatosHomeAdapter.ViewHolder>() {

    /**
     * Crea una instancia de ViewHolder e infla el layout de los datos de la mascota
     *
     * @param parent Grupo de vistas donde se va a mostrar el item
     * @param viewType El tipo de vista del item
     * @return Una instancia de ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_datos_mascota , parent, false)
        return ViewHolder(view)
    }

    /**
     * Devuelve el número de items de la lista
     *
     * @return número de items de la vista
     */
    override fun getItemCount(): Int {
        return dataList.size
    }

    /**
     * Vincula el item de la lista con el ViewHolder
     *
     * @param holder ViewHolder que se va a vincular
     * @param position posición del item
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    /**
     * Clase interna para el ViewHolder del adaptador. Configura el comportamiento de los elementos
     * de la lista.
     *
     * @param itemView Item de la vista
     */
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        //variables para los elementos del item de la lista.
        private val nombreMascota = itemView.findViewById<TextView>(R.id.rvhTxtViewNombreMascota)
        private val fechaNacimiento = itemView.findViewById<TextView>(R.id.rvhTxtVwFechaNac)
        private val especieImagen = itemView.findViewById<ImageView>(R.id.rvhImgVwEspecie)
        private val botonBorrar = itemView.findViewById<ImageButton>(R.id.rvhBtnBorrarMascota)
        private val context: Context = itemView.context

        /**
         * Vincula el item (mascota de la lista) con el ViewHolder. Establece el nombre de la mascota, su fecha de nacimiento,
         * su imagen y el botón de borrar mascota.
         *
         * @param mascota mascota de la lista que se va a vincular de tipo DatosHomeMascotas
         */
        fun bind(mascota: DatosHomeMascotas){
            nombreMascota.text = mascota.nombre
            fechaNacimiento.text = mascota.fechaNacimiento.replace('-', '/')

            setImage(mascota)

            botonBorrar.setOnClickListener {
                confirmarBorrarMascotaDialog()

            }

        }

        /**
         * Muestra un dialog para confirmar si el usuario quiere borrar la mascota. En caso afirmativo, llama al método
         * borrarMascota de HomeFragment y al método onClickDelete para notificar al adaptador, y cierra el dialog.
         * En caso negativo cierra el dialog.
         */
        private fun confirmarBorrarMascotaDialog(){
            val builder = AlertDialog.Builder(context)

            builder.setMessage("¿Estás seguro de querer borrar a tu mascota?")
            builder.setPositiveButton("Sí, borrar"){ dialog, _ ->
                Log.d("BORRAR MASCOTA", "dialog yes")
                HomeFragment.borrarMascota(nombreMascota = nombreMascota.text.toString())
                onClickDelete(bindingAdapterPosition)
                dialog.dismiss()

            }.setNegativeButton("Cancelar"){dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }

        /**
         * Establece una imagen de perro si la especie de la mascota es perro, o de gato si es gato
         *
         * @param mascota Elemento de la lista de mascotas que nos interesa
         */
        private fun setImage(mascota : DatosHomeMascotas){
            if(mascota.especie == "perro"){
                especieImagen.setImageResource(R.drawable.perro)
            }
            else if(mascota.especie == "gato"){
                especieImagen.setImageResource(R.drawable.gato)
            }
        }
    }

}