package com.proyecto.appmascotasinicial

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.appmascotasinicial.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Configura la Activity de Inicio de sesión del usuario. Permitiendo iniciar sesión con email/contraseña en la misma activity, o
 * mediante google llevando al user a otra activity. Configura un botón de contraseña olvidada, de registro de usuario, y de inicio de sesión.
 */
class LoginActivity : AppCompatActivity(){

    //variable para el binding
    private lateinit var binding : ActivityLoginBinding
    //dataStore para guardar el estado de inicio de sesión del usuario
    val Context.dataStore by preferencesDataStore(name = "USER_PREFERENCES_ESTADO_LOGIN")
    //variable para la autenticación con google
    private lateinit var authWithGoogle: AuthWithGoogle

    /**
     * Override de AppCompatActivity. Se carga cuando la actividad empieza.
     * Se encarga de inicializar el binding con la vista, los métodos y establecer los listener de los botones
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //inicializamos authWithGoogle
        authWithGoogle = AuthWithGoogle(this)

        //botón de registrarse lleva a la activity signUp
        binding.liaBtnRegistrarse.setOnClickListener {
            Comun.goToActivity(this,  SignUpActivity::class.java)
        }

        //se comprueba si el usuario está logueado, y si es así lo lleva a la main activity
        if (FirebaseAuth.getInstance().currentUser != null) {
            Comun.goToActivity(this,  MainActivity::class.java)
        }

        //configura el botón de Iniciar sesión
        binding.liaBtnLogIn.setOnClickListener {
            setup()
        }

        //configura el botón de acceso con google
        binding.liaBtnLiGoogle.setOnClickListener {
            authWithGoogle.setupGoogle()
        }

        //configura el botón de contraseña olvidada
        binding.liaTvForgottenPassword.setOnClickListener{
            restablecerContra()
        }

    }

    /**
     * Muestra un dialog para que el usuario escriba su email, y envía mediante Firebase Authentication
     * un correo para el restablecimiento de la contraseña
     */
    private fun restablecerContra(){
        //variable para el email que introduzca el usuario
        val input : EditText = EditText(this)

        //creamos el alertDialogBuilder
        val builder = AlertDialog.Builder(this)

        //establecemos su título y su vista del input
        builder.setTitle("Escribe tu email")
        builder.setView(input)

        builder.setPositiveButton("Aceptar") { dialog, which ->
            val emailParaReestablecer = input.text.toString().trim()

            //se verifica si el correo es válido
            if (isValidEmail(emailParaReestablecer)) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailParaReestablecer)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Correo enviado para restablecer su contraseña",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                    .addOnFailureListener {
                        // Error al enviar el correo de restablecimiento de contraseña
                        Toast.makeText(
                            this,
                            "Error al enviar el correo de restablecimiento de contraseña",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            else {
                // El correo electrónico no es válido
                Toast.makeText(
                    this,
                    "Por favor, ingrese un correo electrónico válido",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)

        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

    /**
     * Comprueba que un email pasado por parámetro sea un email válido respondiendo a un patrón
     *
     * @param email Email que comprueba
     * @return True si el email es válido. False si no lo es.
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Inicia sesión mediante FirebaseAuth con el email y contraseña dados por el usuario. En caso de éxito,
     * va a la activity principal. En caso de fallo, muestra una alerta
     */
    private fun setup(){

        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            binding.liaEtEmailAddress.text.toString(),
            binding.liaEtPassword.text.toString()
        ).addOnCompleteListener {
            if (it.isSuccessful) {
                lifecycleScope.launch(Dispatchers.IO) {
                    Comun.saveValues(true, dataStore)
                }
                Comun.goToActivity(this, MainActivity :: class.java)

            } else {
                Comun.showAlert("Se ha producido un error al iniciar sesión, vuelve a intentarlo", this)
            }
        }
    }

}
