package com.proyecto.appmascotasinicial

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.regex.Pattern

/**
 * Clase que define métodos y variables comunes en la aplicación
 */
class Comun {

    companion object {
        @SuppressLint("StaticFieldLeak")
        //variables para la base de datos de firebase y para el usuario autenticado
        val db = FirebaseFirestore.getInstance()

        //valores de firebase
        //Colecciones:
        const val colecUsuarios = "usuarios"
        const val colecCuriosidades = "curiosidades"
        const val colecProductos = "productos"
        const val colecCitas = "citas"
        const val colecMascotas = "mascotas"
        const val colecTareas = "tareas"
        const val colecObservaciones = "observaciones"

        //Campos colección usuarios:
        const val campoUsuariosNombre = "nombre"
        const val campoUsuariosEmail = "email"
        const val campoUsuariosProvider = "provider"

        //Campos colección productos:
        const val campoProductosTitulo = "titulo"
        const val campoProductosDescripcion = "descripcion"
        const val campoProductosEnlace = "enlace"
        const val campoProductosImagen = "imagen"

        //Campos colección citas:
        const val campoCitasDescripcion = "descripcion"
        const val campoCitasHora = "hora"

        //Campos colección mascotas:
        const val campoMascotasDosis = "dosis_diaria"
        const val campoMascotasEspecie = "especie"
        const val campoMascotasFechaNacimiento = "fecha_nacimiento"
        const val campoMascotasArena = "limpieza_arena"
        const val campoMascotasPaseos = "paseos"
        const val campoMascotasMedicacion = "medicacion"
        const val campoMascotasNombre = "nombre"
        const val campoMascotasNumComidas = "num_comidas"
        const val campoMascotasRacion = "racion"

        //Campos colección tareas
        const val campoTareasDescripcion = "descripcion"
        const val campoTareasEstado = "estado"
        const val campoTareasId = "id"
        const val campoTareasMascota = "mascota"
        const val campoTareasPeriodicidad = "regular"

        //Campos colección Observaciones:
        const val campoObservacionesTexto = "texto"

        /**
         * Se encarga de crear un dialog con un mensaje de error
         *
         * @param mensaje El mensaje que debe mostrar el dialog
         */
        fun showAlert(mensaje: String, context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(mensaje)
            builder.setTitle("Error")
            builder.setPositiveButton("Aceptar", null)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        /**
         * Guarda el estado de inicio de sesión
         *
         * @param estadoLogin El estado de inicio de sesión que se pretende guardar
         */
        suspend fun saveValues(estadoLogin : Boolean, dataStore : DataStore<Preferences>){
            dataStore.edit{preferences ->
                preferences[booleanPreferencesKey("status")] = estadoLogin
            }
        }

        /**
         * Abre un sitio web
         *
         * @param url Url del sitio web que se va a abrir
         * @param context Contexto de la activity que lanza el método
         */
        fun abrirSitioWeb(url : String, context: Context){
            if(url.isNotEmpty()){
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        }

        /**
         * Muesta un DatePicker al hacer clic en el EditText que se pasa por parámetro. Al seleccionar un día se imprime en el editText.
         *
         * @param editTextFecha EditText al que se le va a colocar el DatePicker
         */
        fun mostrarDatePicker(editTextFecha : EditText, context: Context){
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context,
                R.style.dialogFecha,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    editTextFecha.setText(convertirFechaFormato("$dayOfMonth/${month + 1}/$year"))
                },
                year,
                month,
                dayOfMonth)

            datePickerDialog.show()
        }

        /**
         * Muestra un TimePicker al hacer clic en el EditText que se pasa por parámetro, al seleccionar una hora se imprime en el editText
         *
         * @param editTextHora EditText al que se le desea colocar un TimePicker
         */
        fun mostrarTimePicker(editTextHora : EditText, context: Context){
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                context,
                TimePickerDialog.OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int ->
                    val selectedTime = Calendar.getInstance()
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedTime.set(Calendar.MINUTE, minute)
                    editTextHora.setText(
                        DateFormat.format("HH:mm", selectedTime)
                    )
                },
                hour,
                minute,
                DateFormat.is24HourFormat(context)
            )
            timePickerDialog.show()
        }

        /**
         * Esconde el teclado
         */
        fun hideKeyboard(context: Context, editText : EditText) {
            val imm =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        }

        /**
         * Lleva a otra activity mediante un Intent
         *
         * @param context Contexto de la activity origen
         * @param activiyDestino Activity a la que se desea ir
         */
        fun goToActivity(context: Context, activityDestino : Class<out Activity>){
            Log.d("Comun", "Starting activity: ${activityDestino.simpleName}")
            val intentMain = Intent(context, activityDestino)
            context.startActivity(intentMain)
        }

        /**
         * Configura un Spinner con las opciones que se elijan
         *
         * @param context El contexto de la aplicación
         * @param spinner El Spinner que se quiere configurar
         * @param options El ID del array de Strings que contiene las opciones elegidas
         */
        fun setupSpinners(context: Context, spinner: Spinner, options: Int) {
            val options = context.resources.getStringArray(options)
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, options)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        /**
         * Convierte una fecha en formato String dd-MM-yyyy, dd/MM/yyyy, d-MM-yyyy, dd-M-yyyy, d/MM/yyyy, dd/M/yyyy, d-M-yyyy y d/M/yyyy al formato dd/MM/yyyy
         *
         * @param fecha Fecha que se pretende formatear
         * @return La fecha formateada
         */
        fun convertirFechaFormato(fecha: String): String {
            val partes = fecha.replace('-', '/').split("/")
            val dia = partes[0].toInt()
            val mes = partes[1].toInt()
            val anyo = partes[2].toInt()

            val diaString = if (dia < 10) "0$dia" else "$dia"
            val mesString = if (mes < 10) "0$mes" else "$mes"

            return "$diaString/$mesString/$anyo"
        }

        /**
         * Comprueba si una contraseña es segura según 5 criterios:
         *  - Que contenga 8 caracteres o más
         *  - Que contenga minúsculas
         *  - Que contenga mayúsculas
         *  - Que contenga números
         *  - Que contenga caracteres especiales
         *
         *  @param password La contraseña que se quiere comprobar
         *  @return True si cumple con los criterios, False si no los cumple
         */
        fun contraSegura(password: String): Boolean {
            //si la contraseña tiene más de 8 caracteres
            if (password.length >= 8) {

                //variables para comprobar que las tenga mayúsculas, minúsculas y números
                var mayus = false
                var number = false
                var minus = false

                //expresión regular para comprobar cualquier carácter especial
                val caracteresEspeciales = Pattern.compile("\\p{P}*")
                val comprobarEspeciales = caracteresEspeciales.matcher(password)

                //bucle para recorrer todos los caracteres de la contraseña
                var i = 0
                while (i < password.length) {

                    //si el caracter es un número
                    if (Character.isDigit(password[i])) number = true
                    //si el caracter es mayúscula
                    if (Character.isUpperCase(password[i])) mayus = true
                    //si el caracter es minúscula
                    if (Character.isLowerCase(password[i])) minus = true
                    i++
                }

                //si se cumple lo anterior y encuentra un caracter especial, devuelve true
                return mayus && number && minus && comprobarEspeciales.find()
            } else {
                return false
            }
        }

    }

}