package com.example.medidor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.medidor.objetos.ObjetoDistancia
import com.example.medidor.objetos.ObjetoSuperficie
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DatosActivity : AppCompatActivity() {

    val db = Firebase.firestore
    lateinit var nombre: EditText
    lateinit var ud: EditText
    lateinit var alto: EditText
    lateinit var ancho: EditText
    lateinit var nombreDis: EditText
    lateinit var udDis: EditText
    lateinit var distanciaDis: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos)
        nombre=findViewById(R.id.nombreEditTextText)
        ud=findViewById(R.id.udEditText)
        alto=findViewById(R.id.altoEditTextNumber)
        ancho=findViewById(R.id.anchoEditTextNumber)
        nombreDis=findViewById(R.id.nombreDistanciaEditTextText)
        udDis=findViewById(R.id.unidadDistanciaEditText)
        distanciaDis=findViewById(R.id.valorDistanciaEditText)
        findViewById<Button>(R.id.buttonSave).setOnClickListener {
            var objeto= ObjetoSuperficie(nombre.text.toString(),
                ud.text.toString(),
                alto.text.toString().toFloat(),
                ancho.text.toString().toFloat(),
                "",
                "")
            db.collection("obj_superficie")
                .add(objeto).addOnSuccessListener {
                    nombre.setText("")
                    ud.setText("")
                    alto.setText("")
                    ancho.setText("")
                }.addOnFailureListener {
                    Toast.makeText(this,"Fallo",Toast.LENGTH_SHORT).show()
                }
        }
        findViewById<Button>(R.id.saveDist).setOnClickListener {
           // Creamos el objeto con los datos del formulario
            var objeto= ObjetoDistancia(
                nombreDis.text.toString(),
                udDis.text.toString(),
                distanciaDis.text.toString().toFloat(),
                "",
                "")
            //lo añadimos a la coleccion
            db.collection("obj_distancia")
                .add(objeto).addOnSuccessListener {
                    nombreDis.setText("")
                    udDis.setText("")
                    distanciaDis.setText("")
                }.addOnFailureListener {
                    Toast.makeText(this,"Fallo",Toast.LENGTH_SHORT).show()
                }
        }
    }

}