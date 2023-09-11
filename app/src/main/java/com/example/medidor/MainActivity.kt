package com.example.medidor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.SphericalUtil

class MainActivity : AppCompatActivity() , OnMapReadyCallback {
    private val PERMISSION_REQUEST_CODE = 123
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    lateinit var spinner: Spinner
    lateinit var texto: TextView
    lateinit var floatingActionButton: FloatingActionButton
    val polygonOptions = PolygonOptions().fillColor(0x800000ff.toInt())
    var poligono: Polygon?=null
    var faseTipoMapa=0
    var faseMedida=0 // Alterna en los arrays de unidades y cantidades, que deben de ser correlativos.
    val uds=arrayOf(" Bernabeus"," Piscinas olímpicas"," m2", " Km2")
    val cantidades= arrayOf(44414,1050,1,1000000)
    val listaOverlays= mutableListOf<GroundOverlay?>()
    var overlayToRemove: GroundOverlay?=null //Almacena el overlay a borrar cuando se detecta coincidencia.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent=Intent(this,MapsActivity::class.java)
        startActivity(intent)
        val boton= findViewById<Button>(R.id.button)
        val botonActividad= findViewById<MaterialButton>(R.id.datosMatButton)
        spinner = findViewById(R.id.spinner)
        texto=findViewById(R.id.textView)
//Iniciar recicler

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Si no tienes permisos, solicítalos al usuario
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE)
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                }
            }

        }
        // Cambiar cosas cuando  se selecciona
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
           override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
               faseMedida=pos
               if(polygonOptions.points.size>2){
                   var area = SphericalUtil.computeArea(polygonOptions.points)
                   texto.setText("%.2f".format(area/cantidades[faseMedida])+uds[faseMedida])
               }
           }
           override fun onNothingSelected(p0: AdapterView<*>?) {
           }
       }
        //Accion boton flotante.
        floatingActionButton=findViewById(R.id.floatingActionButton)
        //Añadir accion al boton
        boton.setOnClickListener {
            polygonOptions.points.clear()
            map.clear()
            listaOverlays.clear()
            overlayToRemove==null
            poligono=null;
        }
        //Cambiode activity:
        botonActividad.setOnClickListener {
            var intent= Intent(this,DatosActivity::class.java)
            startActivity(intent)
        }
        createMapFragment()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType=GoogleMap.MAP_TYPE_NORMAL
        //Cambiar mapa
        floatingActionButton.setOnClickListener{
            faseTipoMapa++
            if(faseTipoMapa==4){faseTipoMapa=0 }
            when(faseTipoMapa) {
                0->map.mapType = GoogleMap.MAP_TYPE_NORMAL
                1->map.mapType = GoogleMap.MAP_TYPE_HYBRID
                2->map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                3->map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
        }
        //Click largo OVERLAYS
        map.setOnMapLongClickListener {
            if(listaOverlays.size==0){//Si la lista está vacia, simplemente añadimos el overlay.
                addOverlay(it)}
            else{//Si no está vacia
                //VVER SI YA HAY OVERLAY EN LA ZONA--> BORRARLO
                for (gOverlay in listaOverlays) {
                    if (gOverlay != null && isPointInOverlayBounds(it, gOverlay!!)) {// Verifica si el punto está dentro de las coordenadas del overlay
                        overlayToRemove=gOverlay //Si está, lo dejamos para borrar una vez acabada la iteracion (si no, da error)
                    }
                }
                //ACABADA LA ITERACION:
                //Si hay algo para borrar, borramos, si no, añadimos el punto.
                if(overlayToRemove!=null){
                    listaOverlays.remove(overlayToRemove)// Quitar de la lista
                    overlayToRemove!!.remove()//eliminar
                    overlayToRemove=null
                }else {
                    addOverlay(it)
                    overlayToRemove=null
                }
            }

        }
        //CLICK CORTO: dibujo de poligonos
        map.setOnMapClickListener {
           map.addMarker(MarkerOptions().draggable(true).position(it))
            polygonOptions.add(it)
            if(polygonOptions.points.size>2) {
                if(poligono!=null) {
                    poligono!!.remove()
                }
                poligono = map.addPolygon(polygonOptions)
                var area = SphericalUtil.computeArea(polygonOptions.points)
                texto.setText("%.2f".format(area/cantidades[faseMedida])+uds[faseMedida])
            }
        }

    }
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
    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //OVERLAYS
    private fun addOverlay(location: LatLng){
        val image = BitmapDescriptorFactory.fromResource(R.drawable.bernabeu) // Santiago bernabeu
        val overlayOptions = imagenSegunSeleccion(faseMedida,location)
        val overlay = map.addGroundOverlay(overlayOptions)
        listaOverlays.add(overlay)
    }
    private fun isPointInOverlayBounds(point: LatLng, overlay: GroundOverlay): Boolean {
        val overlayBounds = overlay.bounds
        val northeast = overlayBounds.northeast
        val southwest = overlayBounds.southwest
        val overlayLatLngBounds = LatLngBounds(southwest, northeast)
        return overlayLatLngBounds.contains(point)
    }
    private fun imagenSegunSeleccion(fase:Int, location:LatLng):GroundOverlayOptions{
        var image=BitmapDescriptorFactory.fromResource(R.drawable.bernabeu)
        var overlayOptions=GroundOverlayOptions()
            .image(image)
            .position(location, 204F, 217.7F) // Define la posición y el tamaño del GroundOverlay
            .transparency(0.2f) // Define la transparencia (0f para completamente opaco, 1f para completamente transparente)
        return overlayOptions
    }

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.iconoDistCard)
        private val tituloView: TextView = itemView.findViewById(R.id.tituloDistCard)
        private val dimensionesTextView: TextView = itemView.findViewById(R.id.descripcionDistCard)
        fun bind(data: ObjetoSuperficie) {
            tituloView.text = data.nombre
            dimensionesTextView.text = data.alto.toString()+" x "+data.ancho.toString()
            Glide.with(itemView.context)// Carga la imagen de icono utilizando Glide (o Picasso)
                .load(data.imagenURL)
                .into(iconImageView)

        }
    }

}