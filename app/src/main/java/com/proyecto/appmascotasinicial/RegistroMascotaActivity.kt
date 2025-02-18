package com.proyecto.appmascotasinicial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.appmascotasinicial.Comun.Companion.db
import com.proyecto.appmascotasinicial.databinding.ActivityRegistroMascotaBinding
import java.text.DecimalFormat

/**
 * Registra una mascota en la base de datos de Firebase Firestore y genera sus correspondientes tareas
 */
class RegistroMascotaActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegistroMascotaBinding
    private val user = FirebaseAuth.getInstance().currentUser

    //variables para los datos de la mascota
    private lateinit var nombreMascota : String
    private lateinit var fechaNacMascota : String
    private lateinit var especie : String
    private var medicacion : Boolean = false
    private var numComidas : Int = 0
    private var dosisDiaria : Int = 0
    private lateinit var dosisRacion : String
    private var paseos : Int = 0
    private var limpiezaArena : Int = 0
    private var tareas = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroMascotaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //se configura el spinner del número de comidas
        Comun.setupSpinners(this, binding.rmfSpinnerNumeroComidas, R.array.rmfSpinnerOptions)

        //se configura el editText de la fecha de nacimiento
        binding.rmfEtFechaNacimiento.setOnClickListener {
            Comun.mostrarDatePicker(binding.rmfEtFechaNacimiento, this)
        }

        addPreguntaPaseos()

        addPreguntaArena()

        enviarDatos()

        //si el usuario pulsa el botón de omitir, lleva a la activity principal
        binding.rmaBtnOmitir.setOnClickListener {
            Comun.goToActivity(this,  MainActivity::class.java)
        }

    }

    /**
     * Si el usuario selecciona la especie perro, añade una pregunta de paseos adicional, e
     * invisibiliza la de la limpieza de arena en caso de que estuviera visible
     */
    private fun addPreguntaPaseos(){
        binding.rmfRdBtnPerro.setOnCheckedChangeListener{ buttonView , isChecked ->
            if(isChecked){
                //visibilizar pregunta paseo
                binding.rmfTxtViewTituloPaseos.visibility = View.VISIBLE
                binding.rmfSpinnerPaseos.visibility = View.VISIBLE
                Comun.setupSpinners(this, binding.rmfSpinnerPaseos, R.array.rmfSpinnerOptions)

                //invisibilizar (si estuviera antes seleccionado) la pregunta de la limpieza de arena
                binding.rmfTxtVwTituloLimpiezaArena.visibility = View.GONE
                binding.rmfSpinnerArena.visibility = View.GONE

            }
        }
    }

    /**
     * Si el usuario selecciona la especie gato, añade una pregunta de limpieza de arena adicional, e
     * invisibiliza la de los paseos en caso de que estuviera visible
     */
    private fun addPreguntaArena(){
        binding.rmfRdBtnGato.setOnCheckedChangeListener{buttonView, isChecked ->
            if(isChecked){
                //visibilizar pregunta de limpieza de arena
                binding.rmfTxtVwTituloLimpiezaArena.visibility = View.VISIBLE
                binding.rmfSpinnerArena.visibility = View.VISIBLE
                Comun.setupSpinners(this, binding.rmfSpinnerArena, R.array.rmfSpinnerOptions)

                //invisibilizar (si estuviera antes seleccionada) la pregunta de los paseos
                binding.rmfTxtViewTituloPaseos.visibility = View.GONE
                binding.rmfSpinnerPaseos.visibility = View.GONE
            }
        }
    }

    /**
     * Cuando el usuario pulsa el botón de registrar mascota, llama al método getDatos,
     * si comprobarDatosRellenos es true, guarda los datos con el método guardarDatos y lleva al user a
     * la pantalla principal. Si los datos no están rellenos, avisa al usuario con un Toast
     */
    private fun enviarDatos(){
        binding.rmfBtnRegistrarMascota.setOnClickListener {
            getDatos()
            if(comprobarDatosRellenos()){
                guardarDatos()
                Comun.goToActivity(this,  MainActivity::class.java)
            }
            else{
                Toast.makeText(this, "Debes rellenar los datos para guardar a tu mascota", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Comprueba si el usuario ha rellenado el nombre de la mascota, la fecha de nacimiento y la especie.
     *
     * @return True si los datos están rellenos, False si no.
     */
    private fun comprobarDatosRellenos() : Boolean {
        return (nombreMascota.isNotEmpty()
                && fechaNacMascota.isNotEmpty()
                && especie.isNotEmpty())
    }

    /**
     * Método que guarda los datos de la mascota en Firebase firestore, y genera sus tareas correspondientes
     */
    private fun guardarDatos(){
        //si el usuario no es nulo
        if(user!= null){
            //compruebo que el usuario no haya introducido el nombre de una mascota que ya exista
            val documento = db.collection(Comun.colecUsuarios).document(user.uid).collection(Comun.colecMascotas).document(nombreMascota)
            val coleccionTareas = documento.collection(Comun.colecTareas)

            documento.get().addOnSuccessListener {documentSnapshot ->
                //si el documento existe, y no ha sido un documento borrado previamente que se quede en el cache de Firebase, no guardo datos
                if(documentSnapshot.exists() && !documentSnapshot.metadata.isFromCache){
                    Toast.makeText(this, "No puedes introducir dos mascotas con el mismo nombre", Toast.LENGTH_SHORT).show()
                }

                //si el documento no existe (o es un documento borrado previamente, y en el cache de Firebase), los guardo
                else{
                    if(especie == "perro"){
                        documento.set(
                            hashMapOf(
                                Comun.campoMascotasNombre to nombreMascota,
                                Comun.campoMascotasFechaNacimiento to fechaNacMascota,
                                Comun.campoMascotasEspecie to especie,
                                Comun.campoMascotasMedicacion to medicacion,
                                Comun.campoMascotasNumComidas to numComidas,
                                Comun.campoMascotasDosis to dosisDiaria,
                                Comun.campoMascotasRacion to dosisRacion,
                                Comun.campoMascotasPaseos to paseos
                            )
                        )
                    }
                    else{
                        documento.set(
                            hashMapOf(
                                Comun.campoMascotasNombre to nombreMascota,
                                Comun.campoMascotasFechaNacimiento to fechaNacMascota,
                                Comun.campoMascotasEspecie to especie,
                                Comun.campoMascotasMedicacion to medicacion,
                                Comun.campoMascotasNumComidas to numComidas,
                                Comun.campoMascotasDosis to dosisDiaria,
                                Comun.campoMascotasRacion to dosisRacion,
                                Comun.campoMascotasArena to limpiezaArena
                            )
                        )
                    }

                    Toast.makeText(this, "Mascota guardada con éxito :)", Toast.LENGTH_SHORT).show()

                    coleccionTareas.get().addOnSuccessListener {resultadoTareas ->

                        //si la mascota fue borrada previamente, se queda en el cache y las tareas no se borran, así que hay que borrarlas
                        if(!resultadoTareas.isEmpty){
                            for(tarea in resultadoTareas){
                                coleccionTareas.document(tarea.id).delete()
                            }
                        }

                        var i = 0
                        for (tarea in tareas) {
                            val tareaMap = hashMapOf(
                                Comun.campoTareasId to i,
                                Comun.campoTareasMascota to nombreMascota,
                                Comun.campoTareasDescripcion to tarea,
                                Comun.campoTareasEstado to false,
                                Comun.campoTareasPeriodicidad to true
                            )

                            coleccionTareas.document(i.toString()).set(tareaMap)
                                .addOnSuccessListener {
                                    Log.d("GUARDANDO EN FIREBASE", "Tarea $i guardada correctamente")
                                }
                                .addOnFailureListener { e ->
                                    Log.w("GUARDANDO EN FIREBASE", "Error al guardar la tarea $i", e)
                                }

                            i++
                        }
                        Toast.makeText(this, "Tareas generadas", Toast.LENGTH_SHORT).show()
                    }

                }
            }.addOnFailureListener {
                Toast.makeText(this, "Fallo al guardar los datos", Toast.LENGTH_SHORT).show()
            }

        }
        else{
            Toast.makeText(this, "Error, tienes que logearte de nuevo para guardar los datos", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Obtiene los datos de la mascota introducidos en la activity por el usuario:
     * nombreMascota, fechaNacMascota, especie, medicacion, numComidas, dosisDiaria, dosisRacion y especie;
     * y además llama al método obtenerTareas
     */
    private fun getDatos(){
        //obtener nombre mascota
        nombreMascota = binding.rmfEtNombreMascota.text.toString()

        //obtener fecha de nacimiento
        fechaNacMascota = binding.rmfEtFechaNacimiento.text.toString().replace('/', '-')

        //obtener especie
        especie = establecerEspecie()

        //obtener medicacion
        medicacion = binding.rmfChBxMedicacion.isChecked

        //obtener numero de comidas
        numComidas = binding.rmfSpinnerNumeroComidas.selectedItem.toString().toInt()

        //obtener dosis diaria
        if(binding.rmfEtDosisDiaria.text.isNotEmpty()){
            dosisDiaria = binding.rmfEtDosisDiaria.text.toString().toInt()
        }

        //calcular dosis por toma
        val decimalFormat = DecimalFormat("#.#")
        decimalFormat.isDecimalSeparatorAlwaysShown = false

        dosisRacion = decimalFormat.format(dosisDiaria.toDouble()/numComidas.toDouble())

        //obtener paseos
        if(especie == "perro"){
            paseos = binding.rmfSpinnerPaseos.selectedItem.toString().toInt()
        }
        //obtener limpieza de arena
        else if(especie == "gato"){
            limpiezaArena = binding.rmfSpinnerArena.selectedItem.toString().toInt()
        }

        obtenerTareas()

    }

    /**
     * Obtiene la lista de tareas de la mascota
     */
    private fun obtenerTareas(){
        tareas.add("Limpiar cuenco del agua de: $nombreMascota")
        tareas.add("Limpiar cuenco de comida de: $nombreMascota")
        if(medicacion) tareas.add("Dar medicación a $nombreMascota")

        var i = 1
        while(i<=numComidas){
            tareas.add("Alimentar a $nombreMascota $dosisRacion gramos ($i/$numComidas)")
            i++
        }

        if(especie == "perro"){
            i = 1
            while(i<=paseos){
                tareas.add("Pasear a $nombreMascota ($i/$paseos)")
                i++
            }
        }
        else if (especie == "gato"){
            i = 1
            while(i<=limpiezaArena){
                tareas.add("Limpiar la caja de arena a $nombreMascota ($i/$limpiezaArena)")
                i++
            }
        }

        i = 0
        while(i < tareas.size){
            Log.d("OBTENIENDO TAREAS", tareas[i])
            i++
        }
    }

    /**
     * Determina la especie de la mascota del usuario
     *
     * @return "perro" si la especie es perro o "gato" si es gato
     */
    private fun establecerEspecie() : String{
        val id = binding.rmfRdGrpEspecie.checkedRadioButtonId

        if(id != -1){
            if(id == binding.rmfRdBtnPerro.id){
                return "perro"
            }
            else{
                return "gato"
            }
        }
        return ""
    }

}
