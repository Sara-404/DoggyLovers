package com.proyecto.appmascotasinicial.tareasclases

import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.appmascotasinicial.Comun
import com.proyecto.appmascotasinicial.Comun.Companion.db
import com.proyecto.appmascotasinicial.R
import com.proyecto.appmascotasinicial.TareasFragment

/**
 * Adaptador del RecyclerView del fragment TareasFragment que muestra una lista de tareas del usuario
 *
 * @param dataList Lista de tareas que va a mostrar
 * @param onClickDelete Función que indica lo que hacer en caso de elemento eliminado
 */
class TareasAdapter(private var dataList : List<TareasDatos>,
                    private val onClickDelete: (Int) -> Unit) :
    RecyclerView.Adapter<TareasAdapter.ViewHolder> (){

    /**
     * Función que se encarga de pasar un elemento al final de la lista
     *
     * @param position posición del elemento que se desea pasar al final
     */
    fun pasarElementoAlFinal(position : Int){
        val mutableList = dataList.toMutableList()
        val elementoMovido = mutableList.removeAt(position)
        mutableList.add(elementoMovido)
        dataList = mutableList
        notifyItemMoved(position, dataList.size -1)
    }

    /**
     * Sirve para actualizar el recyclerView y que aparezcan las tareas en el RecyclerView
     *
     * @param tareas lista de tareas a mostrar en el recyclerView
     */
    fun actualizarTareas(tareas: List<TareasDatos>) {
        dataList = tareas
        notifyDataSetChanged()
    }

    /**
     * Crea una instancia de ViewHolder e infla el layout de los datos de las tareas
     *
     * @param parent Grupo de vistas donde se va a mostrar el item
     * @param viewType El tipo de vista del item
     * @return Una instancia de ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tareas , parent, false)
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
        holder.bind(dataList[position], onClickDelete, this)
    }

    /**
     * Clase interna para el ViewHolder del adaptador. Configura el comportamiento de los elementos
     * de la lista.
     *
     * @param itemView Item de la vista
     */
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        private val user = FirebaseAuth.getInstance().currentUser

        //variables para los elementos del item de la vista
        private val botonTarea = itemView.findViewById<CheckBox>(R.id.itemTareaCheckBox)
        private val botonEditar = itemView.findViewById<ImageButton>(R.id.itemTareaBtnEditar)
        private val botonBorrar = itemView.findViewById<ImageButton>(R.id.itemTareaBtnBorrar)
        private val contexto = itemView.context

        /**
         * Vincula el item (tarea de la lista) con el ViewHolder. Establece el botón de borrar tarea, el de
         * editar tarea, y el comportamiento cuando se marca una tarea como completada (CheckBox selected)
         *
         * @param tarea tarea de la lista que se va a vincular de tipo TareasDatos
         * @param onClickDelete función para establecer qué hacer en caso de eliminación del elemento
         */
        fun bind(tarea: TareasDatos, onClickDelete: (Int) -> Unit, tareasAdapter: TareasAdapter){

            //limpio cualquier estado anterior. Al ser un recyclerView se reciclan los elementos y se marcan sin intención, así se limpia
            botonTarea.isChecked = false

            botonTarea.isChecked = tarea.estado

            if(tarea.estado) botonTarea.text = tacharTexto(tarea.descripcion)
            else botonTarea.text = tarea.descripcion


            //esto es igual para que se limpie, ya que al ser recyclerView pasan cosas rarillas
            botonTarea.setOnClickListener(null)

            //cuando se clica una tarea, se cambia su estado y se pasa al final
            botonTarea.setOnClickListener{
                establecerEstado(tarea, botonTarea.isChecked)
                tareasAdapter.pasarElementoAlFinal(bindingAdapterPosition)
            }

            botonEditar.setOnClickListener {
                editarTareaDialog(tarea, tareasAdapter)
            }

            //muestra un dialog para confirmar y en caso afirmativo llama a borrarTarea y onClickDelete
            botonBorrar.setOnClickListener {
                val builder = AlertDialog.Builder(contexto)
                builder.setMessage("¿Estás seguro de que quieres borrar esta tarea? Esta acción no se puede deshacer")
                builder.setPositiveButton("Sí, borrar"){dialog, which ->
                    borrarTarea(tarea)
                    onClickDelete(bindingAdapterPosition)
                }
                builder.setNegativeButton("Cancelar"){dialog, which ->
                    dialog.dismiss()
                }

                builder.create().show()
            }


        }

        /**
         * Muestra un dialog que permite editar la tarea
         *
         * @param tarea Item de la lista que se pretende editar
         * @param tareasAdapter Adapter del RecyclerView
         */
        private fun editarTareaDialog(tarea: TareasDatos, tareasAdapter: TareasAdapter){
            val vistaDialog = LayoutInflater.from(contexto).inflate(R.layout.editar_tarea_dialog, null)
            val descripcionTarea = vistaDialog.findViewById<EditText>(R.id.etdEtDescripcionTarea)
            val mascotaSpinner = vistaDialog.findViewById<Spinner>(R.id.etdSpinnerMascota)
            val eleccionRegular = vistaDialog.findViewById<RadioButton>(R.id.etdRdBtnRegular)
            val eleccionHoy = vistaDialog.findViewById<RadioButton>(R.id.etdRdBtnHoy)
            val btnAceptar = vistaDialog.findViewById<Button>(R.id.etdBtnAceptar)
            val btnCancelar = vistaDialog.findViewById<Button>(R.id.etdBtnCancelar)

            val builder = AlertDialog.Builder(contexto)
            builder.setView(vistaDialog)

            //opciones del Spinner: cualquiera de las mascotas del usuario
            val listaOpciones = mutableListOf<String>()


            if(user!=null){
                db.collection(Comun.colecUsuarios)
                    .document(user.uid)
                    .collection(Comun.colecMascotas)
                    .get().addOnSuccessListener { resultado ->
                        for(documento in resultado){
                            listaOpciones.add(documento.getString(Comun.campoMascotasNombre).toString())
                        }

                        //adapter para el Spinner
                        val adapter = ArrayAdapter<String>(vistaDialog.context, android.R.layout.simple_spinner_item, listaOpciones)
                        //diseño del desplegable
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        //establecemos el adaptador del spinner
                        mascotaSpinner.adapter = adapter

                        //mascota del Spinner por defecto
                        val posicionMascota = listaOpciones.indexOf(tarea.nombreMascota)
                        mascotaSpinner.setSelection(posicionMascota)

                }
            }

            //adapter para el Spinner
            val adapter = ArrayAdapter<String>(vistaDialog.context, android.R.layout.simple_spinner_item, listaOpciones)
            //diseño del desplegable
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            //establecemos el adaptador del spinner
            mascotaSpinner.adapter = adapter

            //VALORES POR DEFECTO:
            //descripcion por defecto la que viene
            descripcionTarea.text = Editable.Factory.getInstance().newEditable(tarea.descripcion)

            //radiobutton marcado por defecto en periodicidad
            if(tarea.regular) eleccionRegular.isChecked = true
            else eleccionHoy.isChecked = true

            val dialog = builder.create()
            dialog.show()

            btnAceptar.setOnClickListener{
                TareasFragment.guardarTarea(mascotaSpinner.selectedItem.toString(), descripcionTarea.text.toString(), eleccionRegular.isChecked, false, tarea)
                borrarTarea(tarea)
                // Obtiene la posición de la tarea editada en la lista
                val position = bindingAdapterPosition
                // Notifica al adaptador que el elemento en la posición especificada ha cambiado
                if (position != RecyclerView.NO_POSITION) {
                    tareasAdapter.notifyItemChanged(position)
                }
                dialog.dismiss()
            }

            btnCancelar.setOnClickListener{
                dialog.dismiss()
            }


        }

        /**
         * Borra la tarea pasada por parámetro de la base de datos de Firebase firestore
         *
         * @param tarea Tarea que se desea eliminar
         */
        private fun borrarTarea(tarea : TareasDatos){
            if(user != null){
                db.collection(Comun.colecUsuarios)
                    .document(user.uid)
                    .collection(Comun.colecMascotas)
                    .document(tarea.nombreMascota)
                    .collection(Comun.colecTareas)
                    .document(tarea.idTarea.toString())
                    .delete().addOnSuccessListener {
                        Log.d("BORRAR TAREA", "Tarea de ${tarea.nombreMascota} con id ${tarea.idTarea} borrada")
                    }.addOnFailureListener {
                        Log.d("BORRAR TAREA", "ERROR borrando ${tarea.nombreMascota} con id ${tarea.idTarea}")
                    }
            }
        }

        /**
         * Establece el estado(true = completada, false = no completada) de la tarea
         *
         * @param tarea Tarea a la que se pretende cambiar el estado
         * @param estado estado de la tarea que se desea poner
         */
        private fun establecerEstado(tarea: TareasDatos, estado : Boolean){
            tarea.estado = estado

            if (user != null) {
                val documentoTarea = db.collection(Comun.colecUsuarios).document(user.uid)
                    .collection(Comun.colecMascotas).document(tarea.nombreMascota)
                    .collection(Comun.colecTareas).document(tarea.idTarea.toString())

                documentoTarea.update(Comun.campoTareasEstado, estado)

            }

            establecerTexto(tarea, botonTarea)

        }

        /**
         * Función marcar el CheckBox de una tarea con un tick o no dependiendo de su estado
         *
         * @param tarea Tarea a la que se quiere tachar el texto
         * @param botonTarea CheckBox de la tarea
         */
        private fun establecerTexto(tarea: TareasDatos, botonTarea: CheckBox){
            //si la tarea está completada
            if (tarea.estado) {
                // Asignamos el texto tachado al CheckBox
                botonTarea.text = tacharTexto(botonTarea.text.toString())
                // Marcamos el CheckBox como seleccionado
                botonTarea.isChecked = true
            } else {
                // Si la tarea no está completada, simplemente asignamos la descripción al CheckBox
                botonTarea.text = tarea.descripcion
                // Marcamos el CheckBox como no seleccionado
                botonTarea.isChecked = false
            }
        }

        /**
         * Tacha el texto de una tarea
         *
         * @param texto Texto a tachar
         * @return Texto tachado de tipo SpannableString
         */
        private fun tacharTexto(texto : String) : SpannableString{
            // Creamos un SpannableString con el texto de la tarea
            val textoATachar = SpannableString(texto)
            // Agregamos un StrikethroughSpan para tachar el texto
            textoATachar.setSpan(StrikethroughSpan(), 0, texto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return textoATachar
        }
    }
}

