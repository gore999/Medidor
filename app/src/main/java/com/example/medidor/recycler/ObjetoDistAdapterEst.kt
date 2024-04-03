package com.example.medidor.recycler

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medidor.MapsActivity
import com.example.medidor.Medidor
import com.example.medidor.R
import com.example.medidor.objetos.ObjetoDistancia
import com.example.medidor.objetos.ObjetoSuperficie

class ObjetoDistAdapterEst(private val objetos: MutableList<ObjetoDistancia>, var mapsActivity: MapsActivity) :
    RecyclerView.Adapter<ObjetoDistAdapterEst.ObjetoDistViewHolder>() {
    var selected=-1
    val objetosPrivate= mutableListOf<ObjetoDistancia>()
    init{
        objetosPrivate.addAll(objetos)
    }
    inner class ObjetoDistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.tituloDistCard)
        private val descripcionTextView: TextView = itemView.findViewById(R.id.descripcionDistCard)
        private val imageView: ImageView = itemView.findViewById(R.id.iconoDistCard)
        fun bind(data: ObjetoDistancia) {
            nombreTextView.text = data.nombre
            descripcionTextView.text = data.unidad
            Glide.with(imageView.context).load(data.imagenIcono).into(imageView)
        }
    }
    //Crear la vista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjetoDistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.objeto_distancia, parent, false)
        return ObjetoDistViewHolder(view)

    }
    // Vincular datos a la vista.
    override fun onBindViewHolder(holder: ObjetoDistViewHolder, position: Int){
        holder.bind(objetos[position]) //
        //pintar el check
        if(position==selected){
            holder.itemView.findViewById<ImageView>(R.id.selecteddistimage).setImageDrawable(
                Medidor.instance.getDrawable(
                    R.drawable.check_mark_on
                ))
        }else{
            holder.itemView.findViewById<ImageView>(R.id.selecteddistimage).setImageDrawable(
                Medidor.instance.getDrawable(
                    R.drawable.check_mark_off
                ))
        }
        //Establecer accion de pulsar.
        holder.itemView.setOnClickListener{
            //objetoDistancia=objetos[position]
            mapsActivity.objetoDistancia=objetos[position]
            mapsActivity.actualizarObjetoDistancia(objetos[position])
            it.findViewById<ImageView>(R.id.selecteddistimage).setImageDrawable(
                Medidor.instance.getDrawable(
                    R.drawable.check_mark_on
                ))
            notifyItemChanged(selected)
            selected=position//hacemos cambio
            notifyItemChanged(position) }
    }

    override fun getItemCount(): Int = objetos.size
    fun updateObjetos(objetos_nuevos: MutableList<ObjetoDistancia>){
        objetos.addAll(objetos_nuevos)
        notifyDataSetChanged()

    }
    fun clearObjetos(){
        objetos.clear()
    }
}

