package com.example.medidor
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medidor.databinding.ActivityMainBinding
import com.example.medidor.objetos.ObjetoDistancia
import com.example.medidor.objetos.ObjetoMedidasPersonales
import com.example.medidor.objetos.ObjetoSuperficie
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity()  {
    lateinit var binding:ActivityMainBinding
    private val `PERMISSION-REQUEST-CODE` = 123
    private val GOOGLE_SING_IN=111
    val mAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ///    I N T E N T S
        val intentMapa=Intent(this,MapsActivity::class.java)
        val intentConvert=Intent(this,ConvertActivity::class.java)
        val intentInfo=Intent(this,InstruccionesActivity::class.java)
        // INSTANCIA BASE DE DATOS DE APP
        Medidor.instance.db=Firebase.firestore //Instanciar db
        //LISTENER DEL USUARIO LOGEADO. Cuando hay login, se producen estas acciones.
        mAuth.addAuthStateListener {firebaseAuth->
            if(mAuth.currentUser!=null){
                binding.googleButton.setText(getString(R.string.cerrar_sesion)+ (firebaseAuth.currentUser?.email.toString()))
                cargarObjetosPersonalesEnMedidorAPP()
            }else{
                binding.googleButton.setText(getString(R.string.iniciar_sesion))
            }
        }

        /*Boton iniciar sesion-
        * Varia estado segun este o no iniciada sesion. Cuando se inicia, hay que cargar los datos personales.
        * */
        binding.googleButton.setOnClickListener{
            if(mAuth.currentUser==null){ //Sesion sin iniciar.
                val googleConf=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleClient:GoogleSignInClient= GoogleSignIn.getClient(this,googleConf)
                startActivityForResult(googleClient.signInIntent,GOOGLE_SING_IN)
            }else{
                mAuth.signOut()
                cleanMedidorApp()
                binding.googleButton.setText(getString(R.string.iniciar_sesion))
            }
        }
        //CARGAR LOS DATOS DE UNIDADES DE LA APP
        cargarDatosGeneralesAPP()
        binding.botonMapa.setOnClickListener { startActivity(intentMapa) }
        binding.botonConvert.setOnClickListener { startActivity(intentConvert) }
        //Accion boton flotante.
        binding.floatingActionButtonInfo.setOnClickListener{ startActivity(intentInfo)}
        //Añadir accion al boton
    }
    private fun cargarDatosGeneralesAPP() {
        FirebaseFirestore.getInstance().collection("obj_distancia").get()
            .addOnSuccessListener { result ->
                Medidor.instance.objetoMedidasApp.listaDistancia = result.toObjects(ObjetoDistancia::class.java)
                FirebaseFirestore.getInstance().collection("obj_superficie").get()
                    .addOnSuccessListener { objSupfList ->
                        Medidor.instance.objetoMedidasApp.listaSuperficie =
                            objSupfList.toObjects(ObjetoSuperficie::class.java)
                    }.addOnFailureListener { exception ->
                        println("fallo al cargar"+exception.message.toString())
                    }
            }.addOnFailureListener { exception ->
                println("fallo al cargar")
            }
    }

    fun cargarObjetosPersonalesEnMedidorAPP() {
        Medidor.instance.email = mAuth.currentUser?.email.toString()
        //Cargamos los objetos personales.
        FirebaseFirestore.getInstance().collection("objetos_personales")
            .document(mAuth.currentUser?.email.toString()).get()
            .addOnSuccessListener { result ->
                if (result.exists()) {
                    Medidor.instance.medidasPersonales =
                        result.toObject(ObjetoMedidasPersonales::class.java)!!
                } else {
                    //Toast.makeText(this,Medidor.instance.medidasPersonales.listaDistancia.size,Toast.LENGTH_SHORT).show()
                }

            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Fallo al cargar objetos personales", Toast.LENGTH_SHORT)                    .show()
            }
    }

    fun cleanMedidorApp() {
        Medidor.instance.medidasPersonales.listaDistancia.clear()
        Medidor.instance.medidasPersonales.listaSuperficie.clear()
        Medidor.instance.email = ""
    }
//PERMISOS
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == `PERMISSION-REQUEST-CODE`) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, realiza acciones relacionadas con la ubicación
            } else {
                // Permiso denegado, toma medidas adecuadas (por ejemplo, informar al usuario)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==GOOGLE_SING_IN){
           val task=GoogleSignIn.getSignedInAccountFromIntent(data)
           try{
               val account=task.getResult(ApiException::class.java)//Obtener cuenta de google
               if(account!=null){
                   val credential=GoogleAuthProvider.getCredential(account.idToken,null)//obtener credencial de la cuenta de google recuperada.
                   mAuth.signInWithCredential(credential)
               }
           }catch (ex:ApiException){
               Toast.makeText(this,""+ex.message,Toast.LENGTH_SHORT).show()
              // Toast.makeText(this,"fallo",Toast.LENGTH_SHORT).show()
           }
        }
    }
}