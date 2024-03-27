package com.example.medidor
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medidor.objetos.ObjetoDistancia
import com.example.medidor.objetos.ObjetoMedidasPersonales
import com.example.medidor.objetos.ObjetoSuperficie
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity()  {
    private val PERMISSION_REQUEST_CODE = 123
    private val GOOGLE_SING_IN=111
    lateinit var botonGoogle: Button
    lateinit var floatingActionButton: FloatingActionButton
    val mAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ///    I N T E N T S
        val intentMapa=Intent(this,MapsActivity::class.java)
        val intentConvert=Intent(this,ConvertActivity::class.java)
        val intentInfo=Intent(this,InstruccionesActivity::class.java)
        // INSTANCIA BASE DE DATOS DE APP
        Medidor.instance.db=Firebase.firestore //Instanciar db
        //INTERFACE
        val botonMapa= findViewById<Button>(R.id.botonMapa)
        val botonConvert= findViewById<Button>(R.id.botonConvert)
        botonGoogle=findViewById(R.id.googleButton)
        //LISTENER DEL USUARIO LOGEADO. Cuando hay login, se producen estas acciones.
        mAuth.addAuthStateListener {firebaseAuth->
            if(mAuth.currentUser!=null){
                botonGoogle.setText("Cerrar sesión de "+ (firebaseAuth.currentUser?.email.toString()))
                cargarObjetosPersonalesEnMedidorAPP()
            }else{
                botonGoogle.setText(getString(R.string.iniciar_sesion))
            }
        }

        /*Boton iniciar sesion-
        * Varia estado segun este o no iniciada sesion. Cuando se inicia, hay que cargar los datos personales.
        * */
        botonGoogle.setOnClickListener{
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
                botonGoogle.setText(getString(R.string.iniciar_sesion))
            }
        }
        //CARGAR LOS DATOS DE UNIDADES DE LA APP
        cargarDatosGeneralesAPP()
        botonMapa.setOnClickListener { startActivity(intentMapa) }
        botonConvert.setOnClickListener { startActivity(intentConvert) }
        //Accion boton flotante.
        floatingActionButton=findViewById(R.id.floatingActionButtonInfo)
        floatingActionButton.setOnClickListener{ startActivity(intentInfo)}
        //Añadir accion al boton
    }

    private fun cargarDatosGeneralesAPP() {
        FirebaseFirestore.getInstance().collection("obj_distancia").get()
            .addOnSuccessListener { result ->
                Medidor.instance.objetoDistanciaList = result.toObjects(ObjetoDistancia::class.java)
                FirebaseFirestore.getInstance().collection("obj_superficie").get()
                    .addOnSuccessListener { result ->
                        Medidor.instance.objetoSuperficieList =
                            result.toObjects(ObjetoSuperficie::class.java)
                        //Medidor.instance.db.Dao().getAllDist()
                    }.addOnFailureListener { exception ->
                        println("fallo al cargar")
                        // Log.w(TAG, "Error getting documents.", exception)
                    }
            }.addOnFailureListener { exception ->
                println("fallo al cargar")
                // Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun cargarObjetosPersonalesEnMedidorAPP() {
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

    private fun cleanMedidorApp() {
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
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, realiza acciones relacionadas con la ubicación
            } else {
                // Permiso denegado, toma medidas adecuadas (por ejemplo, informar al usuario)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==GOOGLE_SING_IN){
           val task=GoogleSignIn.getSignedInAccountFromIntent(data)
           try{
               val account=task.getResult(ApiException::class.java)//Obtener cuenta de google
               if(account!=null){
                   val credential=GoogleAuthProvider.getCredential(account.idToken,null)//obtener credencial de la cuenta de google recuperada.
                   mAuth.signInWithCredential(credential)

                   //cargamos objetos personales si se inicia correctamente

               }
           }catch (ex:ApiException){
               //Toast.makeText(this,""+ex.cause,Toast.LENGTH_SHORT).show()
               Toast.makeText(this,"fallo",Toast.LENGTH_SHORT).show()
           }

        }
    }
}