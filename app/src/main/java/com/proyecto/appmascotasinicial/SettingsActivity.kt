package com.proyecto.appmascotasinicial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/**
 * Soporte del fragment de preferencias SettingsFragment
 */
class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="Preferencias"
    }
}