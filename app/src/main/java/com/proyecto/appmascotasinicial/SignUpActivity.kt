package com.proyecto.appmascotasinicial

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.appmascotasinicial.Comun.Companion.db
import com.proyecto.appmascotasinicial.databinding.ActivitySignUpBinding

/**
 * Se encarga del registro del usuario en la aplicación mediante email y contraseña o mediante google
 */
class SignUpActivity : AppCompatActivity(){

    //variable para el binding
    private lateinit var binding: ActivitySignUpBinding
    //variable para el registro mediante google
    private lateinit var authWithGoogle: AuthWithGoogle

    /**
     * Override de AppCompatActivity. Se carga cuando la actividad empieza.
     * Se encarga de inicializar el binding con la vista, los métodos y establecer los listener de los botones
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.suaBtnSignUp.setOnClickListener {
            setup()
        }

        //inicializamos la variable para la autenticación con google
        authWithGoogle = AuthWithGoogle(this)

        //inicia o registra al usuario con google con la variable de tipo AuthWithGoogle authWithGoogle
        binding.suaBtnSignUpGoogle.setOnClickListener {
            authWithGoogle.setupGoogle()
        }

        //vuelve a la pantalla de inicio de sesión
        binding.suaBtnVolver.setOnClickListener {
            Comun.goToActivity(this,  LoginActivity :: class.java)
        }

    }

    /**
     * Se encarga de registrar al usuario con Email y contraseña y guardar sus datos en firestore
     */
    private fun setup() {
        //si el nombre y el email no están vacíos
        if (binding.suaEtNombre.text.isNotEmpty() && binding.suaEtEmailAddress.text.isNotEmpty()) {

            //si la contraseña es segura
            if (Comun.contraSegura(binding.suaEtPassword.text.toString())) {

                //se registra al usuario en la app
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.suaEtEmailAddress.text.toString(),
                    binding.suaEtPassword.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {

                        //inicia sesión
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                            binding.suaEtEmailAddress.text.toString(),
                            binding.suaEtPassword.text.toString()
                        )
                            .addOnSuccessListener {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user != null) {
                                    //se guarda en la colección usuarios un documento con id, proveedor, y nombre
                                    db.collection(Comun.colecUsuarios).document(user.uid)
                                        .set(
                                            hashMapOf(
                                                Comun.campoUsuariosEmail to binding.suaEtEmailAddress.text.toString(),
                                                Comun.campoUsuariosProvider to "BASIC",
                                                Comun.campoUsuariosNombre to binding.suaEtNombre.text.toString()
                                            )
                                        )
                                    //lleva a la activity main
                                    Comun.goToActivity(this, MainActivity::class.java)
                                } else {
                                    Comun.showAlert("Error en el guardado de datos", this)
                                }
                            }.addOnFailureListener {
                                //si falla al iniciar sesión
                                Comun.showAlert("Error al iniciar sesión", this)
                                val intentLogin = Intent(this, LoginActivity::class.java)
                                startActivity(intentLogin)
                            }
                    } else {
                        Comun.showAlert("Error al registrarte, vuelve a intentarlo", this)
                    }
                }

            } else {
                //si la contraseña no es segura, salta un mensaje
                Toast.makeText(
                    this,
                    "Su contraseña debe tener mínimo 8 caracteres, minúsculas, mayúsculas, números y carácteres especiales",
                    Toast.LENGTH_LONG
                )
                    .show()
            }

        } else {
            //si no ha escrito su nombre o email, salta un mensaje
            Toast.makeText(
                this,
                "Debe escribir los datos",
                Toast.LENGTH_SHORT
            )
                .show()
        }

    }


}