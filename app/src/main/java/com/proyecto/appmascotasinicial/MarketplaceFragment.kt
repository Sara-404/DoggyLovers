package com.proyecto.appmascotasinicial

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.proyecto.appmascotasinicial.Comun.Companion.db
import com.proyecto.appmascotasinicial.databinding.FragmentMarketplaceBinding
import com.proyecto.appmascotasinicial.marketplaceclases.MarketplaceAdapter
import com.proyecto.appmascotasinicial.marketplaceclases.Producto

/**
 * Establece un catálogo de productos sobre el cuidado de mascotas con un RecyclerView a los que acceder dando clic sobre ellos.
 */
class MarketplaceFragment : Fragment() {

    //variable para el binding
    private lateinit var binding: FragmentMarketplaceBinding

    //variable para el adapter del RecyclerView
    private lateinit var marketplaceAdapter: MarketplaceAdapter
    //lista de productos para el RecyclerView
    private val listaProducto = mutableListOf<Producto>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMarketplaceBinding.inflate(inflater, container, false)

        //establece de adapter una lista vacía
        marketplaceAdapter = MarketplaceAdapter(emptyList())

        cargarDatos()

        return binding.root
    }

    /**
     * Carga los datos de la lista de productos desde Firebase firestore, y si sale bien llama al método setupRecyclerView
     */
    private fun cargarDatos(){
        db.collection(Comun.colecProductos).get().addOnSuccessListener { resultadoProductos ->
            for (producto in resultadoProductos) {
                val titulo = producto.getString(Comun.campoProductosTitulo) ?: ""
                val descripcion = producto.getString(Comun.campoProductosDescripcion) ?: ""
                val enlace = producto.getString(Comun.campoProductosEnlace) ?: ""
                val imagen = producto.getString(Comun.campoProductosImagen) ?: ""

                val producto = Producto(titulo, descripcion, enlace, imagen)
                listaProducto.add(producto)
            }
            setupRecyclerView()
        }

    }

    /**
     * Se encarga de inicializar el RecyclerView de la lista de productos
     */
    private fun setupRecyclerView(){
        marketplaceAdapter = MarketplaceAdapter(listaProducto)
        binding.maRecyclerViewProductos.apply{
            layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = marketplaceAdapter
        }
    }

}