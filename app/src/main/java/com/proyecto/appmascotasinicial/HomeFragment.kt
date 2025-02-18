package com.proyecto.appmascotasinicial

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.appmascotasinicial.Comun.Companion.db
import com.proyecto.appmascotasinicial.databinding.FragmentHomeBinding
import com.proyecto.appmascotasinicial.datoshomeadapter.DatosHomeAdapter
import com.proyecto.appmascotasinicial.datoshomeadapter.DatosHomeMascotas
import java.util.Random

/**
 * Fragment que hace de "Home" de la aplicación, muestra un textView con el nombre del usuario,
 * un recyclerView con las mascotas de la base de datos de Firebase que tiene el usuario, programa un
 * botón que lleva a la Activity RegistroMascotasActivity, y muestra un cuadro de texto con curiosidades
 * aleatorias de Firestore.
 */
class HomeFragment : Fragment() {

    //variable para el binding
    private lateinit var binding : FragmentHomeBinding
    //variable para el adapter del RecyclerView de las mascotas
    private lateinit var mAdapter: DatosHomeAdapter
    //variable para la lista de mascotas
    private var listaMascotas = mutableListOf<DatosHomeMascotas>()
    private val user = FirebaseAuth.getInstance().currentUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        initRecyclerView()
        setupTitulo()
        setupCuriosidades()

        binding.haBtnRefreshRecyclerVw.setOnClickListener {
            listaMascotas.clear()
            initRecyclerView()
        }

        binding.haBtnAnadirMascota.setOnClickListener {
            Comun.goToActivity(requireActivity(), RegistroMascotaActivity :: class.java)
        }

        return binding.root
    }


    /**
     * Inicia el RecyclerView de las mascotas del usuario obteniéndolas de la base de datos de Firebase
     */
    private fun initRecyclerView(){
        if (user != null) {
            val consultaMascota =
                db.collection(Comun.colecUsuarios).document(user.uid).collection(Comun.colecMascotas).get()
            consultaMascota
                .addOnSuccessListener { result ->

                    for (document in result) {
                        val nombre = document.getString(Comun.campoMascotasNombre) ?: ""
                        val fechaNacimiento = document.getString(Comun.campoMascotasFechaNacimiento) ?: ""
                        val especie = document.getString(Comun.campoMascotasEspecie) ?: ""
                        val mascota = DatosHomeMascotas(nombre, fechaNacimiento, especie)
                        listaMascotas.add(mascota)

                    }

                    // Inicialización del adapter y el layoutManager
                    mAdapter = DatosHomeAdapter(dataList = listaMascotas,
                        onClickDelete = {position -> onDeletedItem(position)})
                    binding.haRvwMascotas.apply {
                        layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        adapter = mAdapter
                    }


                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "Error al obtener mascotas: $exception")
                }
        }
    }

    /**
     * Cuando se elimina un item del RecyclerView de las mascotas, este se borra del listado y se notifica al adapter
     *
     * @param position Posición del item eliminado
     */
    private fun onDeletedItem(position: Int){
        listaMascotas.removeAt(position)
        mAdapter.notifyItemRemoved(position)
    }

    /**
     * Configura un TextView de curiosidades aleatorias de la base de datos de Firestore
     */
    private fun setupCuriosidades(){
        val random = Random()
        val numCuriosidad = random.nextInt(1..12)
        val documento = db.collection(Comun.colecCuriosidades).document("curiosidades_perros")
        documento.get().addOnSuccessListener { document ->
            if (document != null) {
                // Verifica si el documento existe
                if (document.exists()) {
                    // Accede al valor de un campo específico
                    val valorCampo = document.getString(numCuriosidad.toString())
                    // Utiliza el valor del campo
                    if (valorCampo != null) {
                        binding.haTvCuriosidadAleatoria.text = valorCampo
                    }
                }
            }
        }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error al obtener el documento:", exception)
            }
    }

    /**
     * Modifica el TextView de título del fragment que saluda al usuario con su nombre, haciendo una llamada al método obtenerNombre
     */
    private fun setupTitulo(){
        obtenerNombre { nombre ->
            if (nombre.isNotEmpty()) {
                binding.hafTvSaludo.text = "Hola, $nombre"
            } else {
                Log.d("TAG", "Error al obtener el nombre")
            }
        }
    }

    /**
     * Recupera el nombre del usuario de la base de datos de Firestore y lo devuelve vía el callback provisto
     *
     * @param callback función callback invocada pasándole el nombre del usuario tipo String como parámetro
     */
    private fun obtenerNombre(callback: (String) -> Unit){
        if (user != null) {
            val documentoReferencia = db.collection(Comun.colecUsuarios).document(user.uid)
            documentoReferencia.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val nombre = documentSnapshot.getString(Comun.campoUsuariosNombre) ?: ""
                    callback(nombre)
                } else {
                    callback("")
                }
            }.addOnFailureListener {
                callback("")
            }
        } else {
            callback("")
        }
    }

    /**
     * Obtiene un número aleatorio en un rango de números
     *
     * @param range Rango de números del que obtener el número aleatorio
     */
    private fun Random.nextInt(range: IntRange): Int {
        return range.first + nextInt(range.last - range.first)
    }



    companion object {

        /**
         * Borra a una mascota de la base de datos de Firebase
         *
         * @param nombreMascota el nombre de la mascota a borrar
         */
        fun borrarMascota(nombreMascota : String){

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val documentoReferencia =
                    db.collection(Comun.colecUsuarios).document(user.uid).collection(Comun.colecMascotas)
                        .document(nombreMascota)
                documentoReferencia.delete().addOnSuccessListener {
                    Log.d("TAG", "Se borró a $nombreMascota")
                }.addOnFailureListener {
                    Log.d("TAG", "No se pudo borrar la mascota")
                }
            }

        }

    }
}