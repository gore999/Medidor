package com.example.medidor

import android.app.Application
import android.content.Context
import com.example.medidor.objetos.ObjetoDistancia
import com.example.medidor.objetos.ObjetoMedidasApp
import com.example.medidor.objetos.ObjetoMedidasPersonales
import com.example.medidor.objetos.ObjetoSuperficie
import com.google.firebase.firestore.FirebaseFirestore
class Medidor : Application() {
    //var objetoSuperficieList: MutableList<ObjetoSuperficie>
    //var objetoDistanciaList: MutableList<ObjetoDistancia>
    var objetoMedidasApp=ObjetoMedidasApp()
    var medidasPersonales= ObjetoMedidasPersonales()
    var email=""
    lateinit var db: FirebaseFirestore

    init {
        instance=this
       // objetoSuperficieList= mutableListOf()
        //objetoDistanciaList= mutableListOf()
    }
    companion object {
        lateinit var instance: Medidor
    }

    fun getAppContext(): Context = instance.applicationContext
}