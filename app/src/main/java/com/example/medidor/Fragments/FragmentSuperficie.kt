package com.example.medidor.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medidor.Medidor
import com.example.medidor.R
import com.example.medidor.objetos.ObjetoSuperficie
import com.example.medidor.recycler.ObjetoSupAdapterFragment

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentSuperficie.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentSuperficie : Fragment() {
    lateinit var rootView:View
    lateinit var nombre: EditText
    lateinit var unidades: EditText
    lateinit var valorH: EditText
    lateinit var valorV: EditText
    lateinit var urlico: EditText
    lateinit var url: EditText
    lateinit var botonGuardar:Button
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: ObjetoSupAdapterFragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_area, container, false)
        nombre=rootView.findViewById(R.id.unidadArea)
        unidades=rootView.findViewById(R.id.unidadesArea)
        valorH=rootView.findViewById(R.id.valorHorArea)
        valorV=rootView.findViewById(R.id.valorVertArea)
        urlico=rootView.findViewById(R.id.UrlIcoArea)
        url=rootView.findViewById(R.id.UrlImageArea)

        botonGuardar=rootView.findViewById(R.id.guardar_areaud_button)
        recyclerView=rootView.findViewById(R.id.recyclerViewAreaUds)
        adapter= ObjetoSupAdapterFragment(Medidor.instance.medidasPersonales.listaSuperficie)//Le pasamos la lista de medidas personales.
        recyclerView.layoutManager = LinearLayoutManager(Medidor.instance.getAppContext())
        recyclerView.adapter=adapter





        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                // No necesitamos mover elementos en este caso
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val alertDialog = AlertDialog.Builder(rootView.context)
                    .setTitle("Confirmación")
                    .setMessage("¿Estás seguro de continuar?")
                    .setPositiveButton("Confirmar") { dialog, which ->
                        //Confirmar borrado.
                        val position = viewHolder.layoutPosition
                        // Elimina el elemento de la lista de datos
                        // Luego notifica al adaptador para que actualice la vista
                        Medidor.instance.medidasPersonales.listaSuperficie.removeAt(position)
                        //Actualizar
                        Medidor.instance.db.collection("objetos_personales").document(Medidor.instance.email).set(Medidor.instance.medidasPersonales)
                        adapter.notifyItemRemoved(position)
                    }
                    .setNegativeButton("Cancelar") {dialog, which ->
                        adapter.notifyDataSetChanged()
                    }
                // Muestra el diálogo
                alertDialog.show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        botonGuardar.setOnClickListener{
            grabar()
        }
        return rootView
    }
    private fun grabar() {
        val nom=nombre.text.toString()
        val ud=unidades.text.toString()
        val vaV=valorV.text.toString()

        val vaH=valorH.text.toString()
        if(nom.isNotEmpty()&& ud.isNotEmpty() && vaV.isNotEmpty()&& vaH.isNotEmpty()){
            val vaVF=vaV.toFloat()
            val vaHF=vaH.toFloat()
            val obsupef = ObjetoSuperficie()
            obsupef.nombre=nom
            obsupef.unidad=ud
            obsupef.alto=vaVF
            obsupef.ancho=vaHF
            obsupef.icono=urlico.text.toString()+""
            obsupef.imagenURL=url.text.toString()+""
            Medidor.instance.medidasPersonales.listaSuperficie.add(obsupef)
            Medidor.instance.db.collection("objetos_personales").document(Medidor.instance.email).set(Medidor.instance.medidasPersonales)
                .addOnSuccessListener {
                    Toast.makeText(rootView.context,"Grabado", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(rootView.context,"Error al grabar", Toast.LENGTH_SHORT).show()
                }
            vaciarEditText()
            recyclerView.adapter=adapter
            recyclerView.refreshDrawableState()
        }else{
            Toast.makeText(rootView.context,"Cubre los campos necesarios", Toast.LENGTH_SHORT).show()
        }
    }
    private fun vaciarEditText(){
        nombre.setText("")
        unidades.setText("")
        valorH.setText("")
        valorV.setText("")
        urlico.setText("")
        url.setText("")
    }
}