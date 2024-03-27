package com.example.medidor.objetos

class ObjetoMedidasPersonales {
    var listaDistancia: MutableList<ObjetoDistancia>
    var listaSuperficie: MutableList<ObjetoSuperficie>

    init{
        listaDistancia= mutableListOf()
        listaSuperficie= mutableListOf()
    }
    fun addLista(objDis: List<ObjetoDistancia>,objSup:List<ObjetoSuperficie>){
        listaDistancia.addAll(objDis)
        listaSuperficie.addAll(objSup)
    }
    fun addDist(objDis:ObjetoDistancia){
        listaDistancia.add(objDis)
    }
    fun addSup(objSup:ObjetoSuperficie){
        listaSuperficie.add(objSup)
    }
    fun removeDist(objDis:ObjetoDistancia){
        listaDistancia.remove(objDis)
    }
    fun removeSup(objSup:ObjetoSuperficie){
        listaSuperficie.remove(objSup)
    }
}