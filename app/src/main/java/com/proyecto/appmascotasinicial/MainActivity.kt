package com.proyecto.appmascotasinicial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.add
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.proyecto.appmascotasinicial.databinding.ActivityMainBinding
import com.proyecto.appmascotasinicial.tareasclases.ReinicioTareasService
import java.util.concurrent.TimeUnit

/**
 * Clase principal de la aplicación, Consiste en una barra de menú y un fragmento.
 * Configura un servicio de reinicio de tareas diario.
 */
class MainActivity : AppCompatActivity(){

    //variable para el binding
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomBar()

        iniciarServiciodeReinicioTareas()

        //este bloque se crea una vez al crearse la aplicación, sirve para que no cause problemas la rotación de pantalla y demás
        if(savedInstanceState == null){
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                //se añade fragment Home
                add<HomeFragment>(R.id.mainFragmentContainer)
            }
        }
    }

    /**
     * Programa un servicio de reinicio de tareas, a medianoche cada 24 horas
     */
    private fun iniciarServiciodeReinicioTareas(){
        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            ReinicioTareasService::class.java,
            24, // a media noche cada 24 horas
            TimeUnit.HOURS
        )
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS) // Delay inicial para que comience a la medianoche
            .build()

        WorkManager.getInstance(this).enqueue(periodicWorkRequest)

    }

    /**
     * Calcula un el tiempo hasta la medianoche en milisegundos
     *
     * @return Milisegundos que quedan para la medianoche
     */
    private fun calculateInitialDelay(): Long {
        val now = System.currentTimeMillis()
        val midnight = (now / 86400000L + 1) * 86400000L
        return midnight - now
    }

    /**
     * Infla el layout de la barra de menú.
     *
     * @param menu El layout del menú que se pretende inflar
     * @return true si el menú es creado correctamente, false si no.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bottom_nav_bar, menu)
        return true
    }

    /**
     * Establece el fragment que se ve, dependiendo del item que se clique en el menú
     */
    private fun setupBottomBar(){
        binding.mainMenuBottomBar.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.menuItemHome -> {
                    val bundle = bundleOf()
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace(R.id.mainFragmentContainer, HomeFragment::class.java, bundle)
                    }
                    true
                }
                R.id.menuItemCalendar ->{
                    val bundle = bundleOf()
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace(R.id.mainFragmentContainer, CalendarFragment::class.java, bundle)
                    }
                    true
                }
                R.id.menuItemMarketplace ->{
                    val bundle = bundleOf()
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace(R.id.mainFragmentContainer, MarketplaceFragment::class.java, bundle)
                    }
                    true
                }
                R.id.menuItemTareas ->{
                    val bundle = bundleOf()
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace(R.id.mainFragmentContainer, TareasFragment::class.java, bundle)
                    }
                    true
                }
                R.id.menuItemOtros -> {
                    Comun.goToActivity(this, SettingsActivity::class.java)
                    true
                }
                else -> false
            }
        }
    }

}