package com.example.medidor.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medidor.Medidor
import com.example.medidor.R
import com.example.medidor.objetos.ObjetoDistancia
import com.example.medidor.objetos.ObjetoSuperficie

class ObjetoSupAdapterFragment (private val objetos: List<ObjetoSuperficie>) :
    RecyclerView.Adapter<ObjetoSupAdapterFragment.ObjetoSuperfViewHolder>() {
    var selected=-1
    var objetoSuperficie: ObjetoSuperficie? =null;
    inner class ObjetoSuperfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.tituloCard)
        private val descripcionTextView: TextView = itemView.findViewById(R.id.descripcionCard)
        private val imageView: ImageView = itemView.findViewById(R.id.iconoCard)

        fun bind(data: ObjetoSuperficie) {
            nombreTextView.text = data.nombre
            descripcionTextView.text = data.unidad
            Glide.with(imageView.context).load(data.icono).into(imageView)
        }
    }
    //Crear la vista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjetoSuperfViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.objeto_superficie, parent, false)
        return ObjetoSuperfViewHolder(view)

    }
    // Vincular datos a la vista.
    override fun onBindViewHolder(holder: ObjetoSuperfViewHolder, position: Int){
        holder.bind(objetos[position]) //
        //pintar el check
        if(position==selected){
            holder.itemView.findViewById<ImageView>(R.id.selectedimage).setImageDrawable(
                Medidor.instance.getDrawable(
                    R.drawable.check_mark_on
                ))
        }else{
            holder.itemView.findViewById<ImageView>(R.id.selectedimage).setImageDrawable(
                Medidor.instance.getDrawable(
                    R.drawable.check_mark_off
                ))
        }

        //Establecer accion de pulsar.
        holder.itemView.setOnClickListener{
            //objetoDistancia=objetos[position]
            //mapsActivity.objetoDistancia=objetos[position]

            it.findViewById<ImageView>(R.id.selectedimage).setImageDrawable(
                Medidor.instance.getDrawable(
                    R.drawable.check_mark_on
                ))
            notifyItemChanged(selected)
            selected=position//hacemos cambio
            notifyItemChanged(position)
            // Toast.makeText(mapsActivity.applicationContext,""+position+ mapsActivity.objetoDistancia!!.nombre+objetos.size,
            //  Toast.LENGTH_SHORT).show()
        }


    }

    override fun getItemCount(): Int = objetos.size
}