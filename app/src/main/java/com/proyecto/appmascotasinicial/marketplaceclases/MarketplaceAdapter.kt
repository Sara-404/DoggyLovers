package com.proyecto.appmascotasinicial.marketplaceclases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.proyecto.appmascotasinicial.Comun
import com.proyecto.appmascotasinicial.R

/**
 * Adaptador del RecyclerView de la clase MarketplaceFragment que muestra una lista de productos
 *
 * @param dataList Lista de productos de tipo Producto
 */
class MarketplaceAdapter(private var dataList : List<Producto>) : RecyclerView.Adapter<MarketplaceAdapter.ViewHolder> (){

    /**
     * Crea una instancia de ViewHolder e infla el layout de los datos de los productos
     *
     * @param parent Grupo de vistas donde se va a mostrar el item
     * @param viewType El tipo de vista del item
     * @return Una instancia de ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_marketplace_productos, parent, false)
        return ViewHolder(view)
    }

    /**
     * Devuelve el número de items de la lista
     *
     * @return número de items de la vista
     */
    override fun getItemCount(): Int {
        return dataList.size
    }

    /**
     * Vincula el item de la lista con el ViewHolder
     *
     * @param holder ViewHolder que se va a vincular
     * @param position posición del item
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    /**
     * Clase interna para el ViewHolder del adaptador. Configura el comportamiento de los elementos
     * de la lista.
     *
     * @param itemView Item de la vista
     */
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        //variables para los elementos de la vista del item
        private val context = itemView.context
        private val tituloProducto = itemView.findViewById<TextView>(R.id.itemMpPtituloProducto)
        private val descripcionProducto = itemView.findViewById<TextView>(R.id.itemMpPdescripcion)
        private val imagenProducto = itemView.findViewById<ImageView>(R.id.itemMpPimagenProducto)
        private val layout = itemView.findViewById<LinearLayout>(R.id.itemMpLayout)

        /**
         * Vincula el item (producto de la lista) con el ViewHolder. Establece el titulo del producto, su descripción,
         * su imagen y la acción de clicarlo que abre el sitio web correspondiente al enlace del Producto.
         *
         * @param producto producto de la lista que se va a vincular de tipo Producto
         */
        fun bind(producto: Producto){

            tituloProducto.text = producto.titulo
            descripcionProducto.text = producto.descripcion
            Glide.with(itemView).load(producto.imagen).fitCenter().override(300,123).into(imagenProducto)

            layout.setOnClickListener {
                Comun.abrirSitioWeb(producto.enlace, context)
            }

        }

    }

}