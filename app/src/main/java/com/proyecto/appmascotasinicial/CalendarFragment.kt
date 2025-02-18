package com.proyecto.appmascotasinicial

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.appmascotasinicial.NotificacionesProgramadas.Companion.ID_NOTIFICATION
import com.proyecto.appmascotasinicial.databinding.FragmentCalendarBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar.*
import com.proyecto.appmascotasinicial.Comun.Companion.db

/**
 * Fragmento que va en la MainActivity que se encarga de mostrar un Calendario funcional a través del cual se pueden ver
 * citas y observaciones guardadas en las fechas, recogidas en la base de datos de Firebase. Guarda citas, las muestra, y las puede eliminar,
 * guarda observaciones, las muestra y las puede eliminar. A su vez, establece recordatorios para las citas.
 */
class CalendarFragment : Fragment() {

    //variable para el binding con la vista
    private lateinit var binding: FragmentCalendarBinding

    //fecha del calendario, comienza siendo la fecha de hoy
    private var fechaCalendario = getInstance()
    private val user = FirebaseAuth.getInstance().currentUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        //creo el canal de las notificaciones
        createChannel()

        //se establecen las observaciones nada más aparecer el fragment
        setupDiaObservacionesIniciales()

        binding.caCalendarView.setOnDateChangeListener { _, year, month, dayOfMonth  ->
            setupCalendarioView(year, month + 1, dayOfMonth)
            binding.caTxtVwDia.text = "Día: ${Comun.convertirFechaFormato("$dayOfMonth/${month + 1}/$year")}"
            recuperarCita(year, month + 1, dayOfMonth)
        }

        binding.caBtnGuardarObsv.setOnClickListener {
            guardarObservaciones()
        }

        binding.caBtnBorrarObservaciones.setOnClickListener{
            borrarObservacionesCitas("observaciones")
        }

        agregarCita()

        binding.caBtnBorrarCita.setOnClickListener{
            borrarObservacionesCitas("citas")
        }

        establecerDiasCitas()

        return binding.root
    }


    /**
     * Imprime en un textView los días que contienen una cita en la base de datos de Firebase
     */
    private fun establecerDiasCitas(){
        if(user!= null){
            val coleccionCitas = db.collection(Comun.colecUsuarios).document(user.uid).collection(Comun.colecCitas)

            coleccionCitas.get().addOnSuccessListener { documents ->
                for(document in documents){
                    binding.caTxtVwVerCitas.append("; ${document.id.replace('-', '/')}")
                }
            }
        }
    }

    /**
     * Borra las observaciones o las citas de un día en la base de datos y modifica los elementos precisos del fragment
     *
     * @param coleccionDelDocumentoABorrar Colección a la que pertenece el documento que hay que borrar, pudiendo ser la colección de Observaciones o la de Citas
     */
    private fun borrarObservacionesCitas(coleccionDelDocumentoABorrar : String){
        val dia = fechaCalendario.get(DAY_OF_MONTH)
        val mes = fechaCalendario.get(MONTH) + 1
        val anyo = fechaCalendario.get(YEAR)

        val fecha = Comun.convertirFechaFormato("$dia/$mes/$anyo")

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(
            "¿Estás seguro de que deseas borrar las $coleccionDelDocumentoABorrar del día $fecha?"
        )
        builder.setPositiveButton("Sí") { dialog, which ->
            if (user != null) {
                val documento = db.collection(Comun.colecUsuarios)
                    .document(user.uid)
                    .collection(coleccionDelDocumentoABorrar)
                    .document(fecha.replace('/', '-'))

                documento.get().addOnSuccessListener { document ->
                    if (document.exists() && !document.metadata.isFromCache) {
                        documento.delete().addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "$coleccionDelDocumentoABorrar borradas",
                                Toast.LENGTH_SHORT
                            ).show()

                            if(coleccionDelDocumentoABorrar == Comun.colecObservaciones) binding.caEtObservaciones.setText("")
                            else if(coleccionDelDocumentoABorrar == Comun.colecCitas) {
                                binding.caTxtVwCita.text = ""
                                binding.caTxtVwVerCitas.text = binding.caTxtVwVerCitas.text.toString().replace("; $fecha", "")
                            }

                        }.addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Error al borrar las $coleccionDelDocumentoABorrar",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No existen $coleccionDelDocumentoABorrar guardadas este día",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        builder.setNegativeButton("No", null)
        val dialog = builder.create()
        dialog.show()

    }

    /**
     * Recupera las citas de la base de datos de Firebase según el día seleccionado en el calendario, para mostrarlo en un TextView
     *
     * @param year Año
     * @param month mes
     * @param dayOfMonth día
     */
    private fun recuperarCita(year: Int, month: Int, dayOfMonth: Int){
        val date = Comun.convertirFechaFormato("$dayOfMonth/$month/$year")
        var descripcion: String
        var hora: String

        if(user!= null){
            val documentoCita = db.collection(Comun.colecUsuarios)
                .document(user.uid)
                .collection(Comun.colecCitas)
                .document(date.replace('/', '-'))

            documentoCita.get().addOnSuccessListener { document ->
                if(document.exists() && !document.metadata.isFromCache){
                    descripcion = document.getString(Comun.campoCitasDescripcion).toString()
                    hora = document.getString(Comun.campoCitasHora).toString()
                    binding.caTxtVwCita.text = "$hora -> $descripcion"
                }
                else{
                    binding.caTxtVwCita.text = ""
                }
            }
        }

        Log.d("RECUPERAR CITA" , date)
    }

    /**
     * Crea un dialog con una vista personalizada para añadir una cita y un recordatorio, obtiene sus elementos y establece qué hace el botón de Aceptar que llama al método guardarCita(citaFecha: EditText, citaHora : EditText, descripcion : EditText, switch: Switch, editTextFecha: EditText, editTextHora: EditText)
     */
    private fun agregarCita(){
        binding.caBtnAgregarCita.setOnClickListener {
            //se obtiene la vista del dialog que se tiene que mostrar
            val vistaDialog = LayoutInflater.from(requireContext()).inflate(R.layout.agregar_cita_dialog, null)
            val btnAceptar = vistaDialog.findViewById<Button>(R.id.acdBtnAceptar)
            val btnCancelar = vistaDialog.findViewById<Button>(R.id.acdBtnCancelar)
            val citaFecha = vistaDialog.findViewById<EditText>(R.id.acdEdtTxtSeleccionarFecha)
            val citaHora = vistaDialog.findViewById<EditText>(R.id.acdEdtTxtSeleccionarHora)
            val descripcionCita = vistaDialog.findViewById<EditText>(R.id.acdEdtTxtDescripcionCita)
            val recordatorioSwitch = vistaDialog.findViewById<Switch>(R.id.acdSwtchRecordatorio)
            val recordatorioFecha = vistaDialog.findViewById<EditText>(R.id.acdEdtTxtFechaRecordatorio)
            val recordatorioHora = vistaDialog.findViewById<EditText>(R.id.acdEdtTxtHoraRecordatorio)

            //se crea el dialog con la vista personalizada y se muestra
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(vistaDialog)
            val dialog = builder.create()
            dialog.show()

            establecerSwitch(recordatorioSwitch, recordatorioFecha, recordatorioHora)
            citaFecha.setOnClickListener{
                Comun.mostrarDatePicker(citaFecha, requireContext())
            }
            citaHora.setOnClickListener{
                Comun.mostrarTimePicker(citaHora, requireContext())
            }
            recordatorioFecha.setOnClickListener {
                Comun.mostrarDatePicker(recordatorioFecha, requireContext())
            }
            recordatorioHora.setOnClickListener {
                Comun.mostrarTimePicker(recordatorioHora, requireContext())
            }
            btnAceptar.setOnClickListener {
                if(citaFecha.text.isNotEmpty() && citaHora.text.isNotEmpty() && descripcionCita.text.isNotEmpty()){
                    guardarCita(citaFecha, citaHora, descripcionCita, recordatorioSwitch, recordatorioFecha, recordatorioHora)
                    dialog.dismiss()
                }
                else{
                    Toast.makeText(requireContext(), "Debe elegir una fecha, una hora y escribir una descripción", Toast.LENGTH_SHORT).show()
                }
            }
            btnCancelar.setOnClickListener {
                dialog.dismiss()
            }


        }
    }

    /**
     * Guarda la cita establecida en los editText del dialog en la base de datos y programa en caso de que quiera el usuario un recordatorio
     *
     * @param citaFecha Fecha de la cita
     * @param citaHora Hora de la cita
     * @param descripcion Descripción de la cita
     * @param switch Botón que responde a si el usuario desea recordatorio o no
     * @param editTextFecha Fecha del recordatorio
     * @param editTextHora Hora del recordatorio
     */
    private fun guardarCita(citaFecha: EditText, citaHora : EditText, descripcion : EditText, switch: Switch, editTextFecha: EditText, editTextHora: EditText){
        if(user!=null){
            val documentoCita = db.collection(Comun.colecUsuarios)
                .document(user.uid)
                .collection(Comun.colecCitas)
                .document(citaFecha.text.toString().replace('/', '-'))
            documentoCita.set(hashMapOf(Comun.campoCitasHora to citaHora.text.toString(), Comun.campoCitasDescripcion to descripcion.text.toString()))
                .addOnSuccessListener {
                    binding.caTxtVwVerCitas.append("; ${citaFecha.text.toString()}")
                    Toast.makeText(requireContext(), "Cita guardada correctamente", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "No se pudo guardar la cita", Toast.LENGTH_SHORT).show()
                }
        }

        //si el usuario tiene los permisos activados
        if(ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED){

            //se establece un recordatorio
            if(citaFecha.text.isNotEmpty() && citaHora.text.isNotEmpty() && switch.isChecked) {
                programarNotification(editTextFecha, editTextHora, citaFecha.text.toString())
                Toast.makeText(requireContext(), "Recordatorio activado", Toast.LENGTH_SHORT).show()
            }
        }

        else {

            //pide los permisos al usuario
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NotificacionesProgramadas.MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS
                )
            }

            //avisa al usuario que el recordatorio no se activó
            val dialogbuilder = AlertDialog.Builder(requireContext())
            dialogbuilder.setMessage("Este recordatorio NO se te ha activado, puedes borrar la cita y volver a establecer el recordatorio en caso de que lo desees.")
            dialogbuilder.setTitle("Permisos necesarios")
            dialogbuilder.setNeutralButton("Entendido"){ dialog, which ->
                dialog.dismiss()
            }
            dialogbuilder.create().show()
        }


    }

    /**
     * Modifica la visibilidad de los elementos de fecha y hora del recordatorio dependiendo de lo que dictamine el usuario
     *
     * @param switch Switch que define si el usuario quiere recordatorio o no
     * @param editTextFecha Fecha del recordatorio
     * @param editTextHora Hora del recordatorio
     */
    private fun establecerSwitch(switch : Switch, editTextFecha: EditText, editTextHora: EditText){
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                editTextFecha.visibility = View.VISIBLE
                editTextHora.visibility = View.VISIBLE
            }
            else{
                editTextFecha.visibility = View.INVISIBLE
                editTextHora.visibility = View.INVISIBLE

            }
        }
    }

    /**
     * Programa una notificación en compañía del Broadcast Receiver NotificacionesProgramadas y AlarmManager
     *
     * @param editTextFecha Fecha del recordatorio
     * @param editTextHora Hora del recordatorio
     * @param fechaCita Fecha de la cita que tiene que recordar
     */
    @SuppressLint("ScheduleExactAlarm")
    private fun programarNotification(editTextFecha: EditText, editTextHora: EditText, fechaCita : String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val intent = Intent(activity?.applicationContext, NotificacionesProgramadas::class.java)
            intent.putExtra("fechaCita", fechaCita)
            val pendingIntent = PendingIntent.getBroadcast(
                activity?.applicationContext,
                ID_NOTIFICATION,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            //////////////////////////////////////
            val fechaFormato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val fechaString = "${editTextFecha.text.toString()} ${editTextHora.text.toString()}"

            val fecha = LocalDateTime.parse(fechaString, fechaFormato)
            val fechaEnMilis = fecha.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ////////////////////////////////////

            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,fechaEnMilis , pendingIntent)

        }
        else{
            Toast.makeText(requireContext(), "Para activar las notificaciones necesitas Android 8 o superior", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Crea un canal para las notificaciones en caso de que el usuario tenga Android 8 o superior
     */
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificacionesProgramadas.MI_CANAL_ID_NOTIFICACIONES,
                "MiCanal",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal de notificaciones"
            }

            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Muestra las observaciones asociadas a una fecha del Calendario
     *
     * @param year Año
     * @param month Mes
     * @param dayOfMonth día
     */
    private fun setupCalendarioView(year: Int, month: Int, dayOfMonth: Int) {
        //cada vez que el usuario seleccione una fecha se modificarán las observaciones acorde a la bbdd

        val date = Comun.convertirFechaFormato("$dayOfMonth/$month/$year")

        //referencia al documento en firebase

        if(user!= null){
            val coleccionId = db.collection(Comun.colecUsuarios).document(user.uid)
            val documentRef = coleccionId.collection(Comun.colecObservaciones).document(date.replace('/','-'))

            //se obtiene los datos del documento en firebase
            documentRef.get().addOnSuccessListener { document ->
                //si el documento no es nulo
                if (document != null) {

                    //si el documento existe
                    if (document.exists() && !document.metadata.isFromCache) {
                        //el editText de observaciones mostrará el campo texto del documento de la bbdd
                        val texto = document.getString(Comun.campoObservacionesTexto)
                        binding.caEtObservaciones.setText(texto)
                    }
                    //si el documento no existe, vacía el cuadro de observaciones
                    else {
                        binding.caEtObservaciones.setText("")
                    }

                } else {
                    //si el documento fuera nulo
                    Toast.makeText(requireContext(), "documento nulo", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                // Error al intentar acceder a Firestore
                Toast.makeText(requireContext(), "No accede a firebase", Toast.LENGTH_SHORT).show()
            }
        }

        //si se selecciona una fecha, el cuadro de observaciones pierde el foco y se esconde el teclado
        binding.caEtObservaciones.clearFocus()
        Comun.hideKeyboard(requireContext(), binding.caEtObservaciones)

        //cambiamos la variable fechaCalendario
        val calendar = getInstance()
        calendar.set(year, month - 1 , dayOfMonth)
        fechaCalendario = calendar

    }


    /**
     * Guarda en la base de datos observaciones que ponga el usuario con su fecha correspondiente
     */
    private fun guardarObservaciones() {
        val dia = fechaCalendario.get(DAY_OF_MONTH)
        val mes = fechaCalendario.get(MONTH) + 1
        val anyo = fechaCalendario.get(YEAR)

        val fechaString = Comun.convertirFechaFormato("$dia/$mes/$anyo")

        //obtengo el texto de las observaciones
        val texto = binding.caEtObservaciones.text.toString()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            db.collection(Comun.colecUsuarios).document(currentUser.uid)
                .collection(Comun.colecObservaciones).document(fechaString.replace('/', '-'))
                .set(hashMapOf(Comun.campoObservacionesTexto to texto))
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Observaciones guardadas correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Error al guardar observaciones",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    /**
     * Configura el cuadro de observaciones al inicio en caso de que el día de hoy tenga alguna observación para mostrar
     */
    private fun setupDiaObservacionesIniciales() {
        //creo una instancia de calendario con el día del sistema
        val diaInicial = fechaCalendario.get(DAY_OF_MONTH)
        val mesInicial = fechaCalendario.get(MONTH) + 1
        val anioInicial = fechaCalendario.get(YEAR)

        val fechaString = Comun.convertirFechaFormato("$diaInicial/$mesInicial/$anioInicial")

        binding.caTxtVwDia.text = "Día: $fechaString"

        //accedo a la bbdd, si existe documento con la fecha del día, muestra el texto en el editText de las observaciones
        if(user!= null){
            db.collection(Comun.colecUsuarios).document(user.uid).collection(Comun.colecObservaciones).document(fechaString.replace('/', '-'))
                .get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.caEtObservaciones.setText(document.getString(Comun.campoObservacionesTexto))
                    }
                }
        }

        recuperarCita(anioInicial, mesInicial, diaInicial)
    }


}