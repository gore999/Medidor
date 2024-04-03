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

class ObjetoDistanciaAdapterFragment (private val objetos: List<ObjetoDistancia>) :
    RecyclerView.Adapter<ObjetoDistanciaAdapterFragment.ObjetoDistViewHolder>() {
    var selected=-1
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
            //mapsActivity.objetoDistancia=objetos[position]

            it.findViewById<ImageView>(R.id.selecteddistimage).setImageDrawable(
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



