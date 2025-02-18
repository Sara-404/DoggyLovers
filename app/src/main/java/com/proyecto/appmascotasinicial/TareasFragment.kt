package com.proyecto.appmascotasinicial

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.appmascotasinicial.Comun.Companion.db
import com.proyecto.appmascotasinicial.databinding.FragmentTareasBinding
import com.proyecto.appmascotasinicial.tareasclases.TareasAdapter
import com.proyecto.appmascotasinicial.tareasclases.TareasDatos

/**
 * Fragmento que muestra una lista de tareas en relación a las mascotas del usuario, que puede editar o borrar
 * y añadir en caso de que lo desee, y puede marcar como completadas.
 */
class TareasFragment : Fragment() {

    //variable para el binding con la vista
    private lateinit var binding : FragmentTareasBinding
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTareasBinding.inflate(inflater, container, false)

        // Inicializa el adaptador con una lista vacía
        tareasAdapter = TareasAdapter(emptyList(), onClickDelete = {position -> onDeletedItem(position)})

        setupRecyclerView()

        cargarDatosFirebase()

        binding.taFlBtnAnadirTarea.setOnClickListener {
            setupBtnAgregarTarea()
        }

        return binding.root
    }

    /**
     * Se encarga de borrar un elemento de la lista de tareas y notificar al adaptador
     *
     * @param position Posición del elemento a borrar
     */
    private fun onDeletedItem(position: Int){
        listaTareas.removeAt(position)
        tareasAdapter.notifyItemRemoved(position)
    }

    /**
     * Crea un dialog para añadir una tarea y establece su botón positivo y negativo.
     * El botón positivo llama al método anadirTarea, notifica al adaptador y cierra el dialog,
     * el botón negativo cierra el dialog
     */
    private fun setupBtnAgregarTarea() {
        //obtengo la vista del dialog personalizado y sus botones
        val vistaDialog =
            LayoutInflater.from(requireContext()).inflate(R.layout.anadir_tarea_dialog, null)
        val btnAceptar = vistaDialog.findViewById<Button>(R.id.atdBtnAceptar)
        val btnCancelar = vistaDialog.findViewById<Button>(R.id.atdBtnCancelar)

        //creo un AlertDialog con la vista customizada
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(vistaDialog)

        val dialog = builder.create()
        dialog.show()

        btnAceptar.setOnClickListener {
            anadirTarea(vistaDialog)
            tareasAdapter.actualizarTareas(listaTareas)
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }
    }

    /**
     * Método que se encarga de obtener los valores de la tarea (descripción, periodicidad y mascota),
     * comprueba si estos valores no están vacíos, si no lo están llama al método guardarTarea, si lo están
     * avisa al usuario.
     */
    private fun anadirTarea(vistaDialog : View){
        //obtengo los elementos del dialog, con los que voy a trabajar
        val descripcionNuevaTarea = vistaDialog.findViewById<EditText>(R.id.atdEtDescripcionTarea)
        val seleccionTareaRegular = vistaDialog.findViewById<RadioButton>(R.id.atdRdBtnRegular)
        val seleccionTareaHoy = vistaDialog.findViewById<RadioButton>(R.id.atdRdBtnHoy)
        val mascotaSeleccionada = vistaDialog.findViewById<EditText>(R.id.atdEtMascota)

        //obtengo los valores
        val descripcion = descripcionNuevaTarea.text.toString()
        val periodicidad = seleccionTareaRegular.isChecked
        val mascota = mascotaSeleccionada.text.toString()


        if((descripcion.isNotEmpty() && mascota.isNotEmpty()) && (periodicidad || seleccionTareaHoy.isChecked)){
            guardarTarea(mascota, descripcion, periodicidad, true, null)
        }
        else{
            Toast.makeText(requireContext(), "No has rellenado los datos", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Se encarga de cargar los datos de Firebase firestore para el RecyclerView de la lista de tareas
     */
    private fun cargarDatosFirebase(){
        if(user!= null){
            db.collection(Comun.colecUsuarios).document(user.uid).collection(Comun.colecMascotas)
                .get()
                .addOnSuccessListener { result ->
                    for(documentoMascota in result) {
                        val subColeccionTareas = db.collection(Comun.colecUsuarios).document(user.uid)
                            .collection(Comun.colecMascotas).document(documentoMascota.id).collection(Comun.colecTareas)

                        subColeccionTareas.get().addOnSuccessListener {resultadoTareas ->
                            for(documentoTarea in resultadoTareas){
                                val descripcion = documentoTarea.getString(Comun.campoTareasDescripcion) ?: ""
                                val estado = documentoTarea.getBoolean(Comun.campoTareasEstado) ?: false
                                val regular = documentoTarea.getBoolean(Comun.campoTareasPeriodicidad) ?: true

                                val tarea = TareasDatos(documentoTarea.id.toInt(),estado, descripcion, documentoMascota.id, regular)
                                listaTareas.add(tarea)
                            }

                            //ordena la lista de tareas dependiendo de su estado (las completadas abajo)
                            listaTareas.sortByDescending { !it.estado }

                            // Notificar al adaptador sobre los cambios
                            tareasAdapter.actualizarTareas(listaTareas)

                        }
                    }
                }
        }
    }

    /**
     * Se encarga de configurar el RecyclerView con su adapter y su layoutManager
     */
    private fun setupRecyclerView(){
        listaTareas.clear()
        binding.taRcVwTareasPendientes.apply{
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = tareasAdapter
        }

    }

    companion object {

        private lateinit var tareasAdapter: TareasAdapter
        private val listaTareas = mutableListOf<TareasDatos>()

        /**
         * Se encarga de guardar la tarea generada por el usuario en Firebase firestore, o para editar una existente
         *
         * @param mascota Nombre de la mascota a la que le pertenece esa tarea
         * @param descripcion Descripción de la tarea
         * @param periodicidad Indica si la tarea es regular (true) o si no (false)
         * @param esTareaNueva Indica si la tarea es nueva (true) o si no (false)
         * @param tareaOriginal Tarea original si se desea modificar. Puede ser nulo al ser una tarea nueva.
         */
        fun guardarTarea(mascota : String, descripcion : String, periodicidad : Boolean, esTareaNueva : Boolean, tareaOriginal : TareasDatos?){
            val user = FirebaseAuth.getInstance().currentUser
            if(user!= null){

                val mascotaReferencia = db.collection(Comun.colecUsuarios)
                    .document(user.uid)
                    .collection(Comun.colecMascotas)
                    .document(mascota)

                //obtengo el documento del nombre de la mascota introducida por el usuario
                mascotaReferencia.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists() && !documentSnapshot.metadata.isFromCache) {
                        val coleccionTareas = mascotaReferencia.collection(Comun.colecTareas)
                        coleccionTareas.get().addOnSuccessListener { result ->
                            var i = 0
                            for (document in result) {
                                if (document.exists() && !documentSnapshot.metadata.isFromCache) {
                                    if(document.id == i.toString()){
                                        i++
                                    }
                                }

                            }

                            val nuevaTarea = hashMapOf(
                                Comun.campoTareasDescripcion to descripcion,
                                Comun.campoTareasEstado to false,
                                Comun.campoTareasPeriodicidad to periodicidad,
                                Comun.campoTareasId to i,
                                Comun.campoTareasMascota to mascota
                            )

                            val tareaLista = TareasDatos(i,false,descripcion, mascota, periodicidad)

                            //condicional que sirve para indicar si es una tarea nueva o editada, si es nueva actualiza el RecyclerView
                            if(esTareaNueva){
                                listaTareas.add(tareaLista)
                                tareasAdapter.notifyItemInserted(listaTareas.size -1)
                            }
                            else{
                                val index = listaTareas.indexOfFirst { it.idTarea == tareaOriginal!!.idTarea && it.nombreMascota == tareaOriginal!!.nombreMascota}
                                if (index != -1) {
                                    listaTareas[index] = TareasDatos(tareaOriginal!!.idTarea, tareaOriginal.estado, descripcion, tareaOriginal.nombreMascota, periodicidad)
                                    // Notifica al adaptador que los datos han cambiado
                                    tareasAdapter.notifyItemChanged(index)
                                }
                            }

                            coleccionTareas.document(i.toString()).set(nuevaTarea)
                                .addOnSuccessListener { documentReference ->
                                    Log.d(
                                        "ADDTAREA",
                                        "Documento de tarea agregado"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.w("ADDTAREA", "Error al agregar documento de tarea", e)
                                }
                        }

                    }
                    else{
                        Log.d(
                            "ADDTAREA",
                            "La mascota $mascota no existe"
                        )
                    }

                }.addOnFailureListener {e->
                    Log.w("ADDTAREA", "Error al identificar la existencia de $mascota", e)
                }

            }
            else{
                Log.w("ADDTAREA", "user nulo")
            }
        }

    }

}