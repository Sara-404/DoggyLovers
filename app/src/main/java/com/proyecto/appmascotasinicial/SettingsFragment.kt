package com.proyecto.appmascotasinicial

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.appmascotasinicial.Comun.Companion.campoUsuariosProvider
import com.proyecto.appmascotasinicial.Comun.Companion.db


/**
 * Fragment que se encarga de las configuraciones y preferencias que el usuario quiera cambiar, como
 * cambiar el nombre, la contraseña, enviar comentarios, borrar cuenta o cerrar sesión.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    //inputText para los dialogs que lo requieren
    private lateinit var input : EditText
    private val user = FirebaseAuth.getInstance().currentUser


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        //se obtienen los objetos
        val cambioNombre : Preference? = findPreference("nameChange")
        val cambioPassword : Preference? = findPreference("passwordChange")
        val feedback : Preference? = findPreference("feedback")
        val cerrarSession: Preference? = findPreference("logOut")
        val borrarCuenta : Preference? = findPreference("borrarCuenta")
        val volver : Preference? = findPreference("volver")

        //sitio web del formulario donde se recojen los comentarios
        val urlFormulario = "https://docs.google.com/forms/d/e/1FAIpQLScHtj8Vz1l3WvoGXUvi70ADTmR8qoeesvDzhxkNlIAWKEC9Ng/viewform?usp=sf_link"

        volver?.setOnPreferenceClickListener {
            Comun.goToActivity(requireActivity(), MainActivity::class.java)
            true
        }


        feedback?.setOnPreferenceClickListener {
            Comun.abrirSitioWeb(urlFormulario, requireContext())
            true
        }


        cambioPassword?.setOnPreferenceClickListener {
            cambiarContra()
            true
        }

        cerrarSession?.setOnPreferenceClickListener {
            cerrarSesion()
            true
        }

        borrarCuenta?.setOnPreferenceClickListener {
            borrarCuenta()
            true
        }

        cambioNombre?.setOnPreferenceClickListener {
            cambiarNombreUser()
            true
        }

    }

    /**
     * En caso de que el proveedor sea email y contraseña, permite al usuario cambiarla
     */
    private fun cambiarContra(){
        var provider : String
        if(user!= null){
            db.collection(Comun.colecUsuarios).document(user.uid).get().addOnSuccessListener {documentoUser ->
                provider = documentoUser.getString(campoUsuariosProvider).toString()
                if(provider == "GOOGLE") {
                    Toast.makeText(
                        requireContext(),
                        "No puede cambiar la contraseña puesto que accede mediante google",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else{
                    val alertDialogBuilder = AlertDialog.Builder(requireContext())

                    alertDialogBuilder.setTitle("Cambiar contraseña")
                    alertDialogBuilder.setMessage("¿Estás seguro de querer cambiar tu contraseña?")
                    alertDialogBuilder.setPositiveButton("Aceptar"){dialog, which ->
                        //se crea otro dialog
                        val alertDialogBuilderContra = AlertDialog.Builder(requireContext())
                        input = EditText(requireContext())

                        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                        alertDialogBuilderContra.setTitle("Escribe tu nueva contraseña")
                        alertDialogBuilderContra.setView(input)

                        alertDialogBuilderContra.setPositiveButton("Cambiar contraseña"){ _, _ ->

                            if (Comun.contraSegura(input.text.toString())) {
                                user.updatePassword(input.text.toString()).addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Contraseña actualizada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Por seguridad, antes de actualizar su contraseña es necesario iniciar sesión de nuevo",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    cerrarSesion()
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Su contaseña tiene que tener mínimo 8 caracteres, mayúsculas, minúsculas y caracteres especiales",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }

                        alertDialogBuilderContra.setNegativeButton("Cancelar", null)
                        val alertDialogContra = alertDialogBuilderContra.create()
                        alertDialogContra.show()

                    }

                    alertDialogBuilder.setNegativeButton("Cancelar", null)

                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }
            }
        }
        else{
            Toast.makeText(requireContext(), "No se pudo encontrar al usuario autenticado, prueba a iniciar sesión de nuevo", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Mediante un dialog, el usuario puede cambiar su nombre, el cual será actualizado en Firebase firestore
     */
    private fun cambiarNombreUser(){

        input = EditText(requireContext())
        //creamos un dialog
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Escribe tu nombre")

        //Establecemos el inputText para el dialog
        alertDialogBuilder.setView(input)

        //establecemos lo que hace el botón aceptar
        alertDialogBuilder.setPositiveButton("Aceptar"){dialog, which ->
            //si el usuario ha escrito un nombre
            val nombre = input.text.toString().trim()
            if(nombre.isNotEmpty()){

                if(user != null){
                    //accede al documento con el email del user y modifica el campo nombre
                    db.collection(Comun.colecUsuarios).document(user.uid)
                        .update(Comun.campoUsuariosNombre, input.text.toString())
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Nombre cambiado con éxito",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Error al cambiar el nombre",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                }

            }else {
                Toast.makeText(requireContext(), "Por favor, ingresa un nombre válido", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialogBuilder.setNegativeButton("Cancelar"){dialog, which ->

        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }

    /**
     * Cierra la sesión del usuario y lo lleva la activity de inicio de sesión LoginActivity
     */
    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        Comun.goToActivity(requireContext(), LoginActivity :: class.java)
    }

    /**
     * Crea un dialog para confirmar que el usuario quiere borrar su cuenta, y si es así borra su cuenta de
     * Firebase Authentication y los datos guardados asociados a su cuenta en Firebase firestore
     */
    private fun borrarCuenta() {
        //comprueba que haya habido un inicio de sesión reciente para borrar la cuenta
        if (user != null ) {

            //dialog para confirmar que el usuario quiere borrar su cuenta
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle("Eliminar cuenta")
            alertDialogBuilder.setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Se borrarán todos los datos asociados a ella")
            alertDialogBuilder.setPositiveButton("Sí") { dialog, which ->

                db.collection(Comun.colecUsuarios).document(user.uid).delete()
                    .addOnSuccessListener {
                        Log.d("BORRAR_CUENTA", "Documento de usuario eliminado con éxito")

                        // Elimina la cuenta del usuario
                        user.delete().addOnSuccessListener {
                            Log.d("BORRAR_CUENTA", "Cuenta de usuario eliminada con éxito")

                            Toast.makeText(
                                requireContext(),
                                "Cuenta borrada con éxito",
                                Toast.LENGTH_SHORT
                            ).show()
                            cerrarSesion()

                        }?.addOnFailureListener {
                            Log.e(
                                "BORRAR_CUENTA",
                                "Error al eliminar la cuenta de usuario: $it"
                            )
                            Toast.makeText(
                                requireContext(),
                                "Ha habido un problema al borrar su cuenta de usuario, prueba a iniciar sesión de nuevo y volverlo a intentar",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }.addOnFailureListener {
                    Log.e("BORRAR_CUENTA", "Error al eliminar el documento de usuario: $it")
                }

            }
            alertDialogBuilder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            // Crear y mostrar el diálogo
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()

        }
        else{
            Toast.makeText(
                requireContext(),
                "Necesita iniciar sesión de nuevo antes de borrar la cuenta",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}

