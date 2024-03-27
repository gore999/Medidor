package com.example.medidor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.medidor.objetos.ObjetoDistancia
import com.example.medidor.objetos.ObjetoSuperficie
import com.example.medidor.recycler.ObjetoDistAdapterEst
import com.example.medidor.recycler.ObjetoSupAdapterEst
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsActivity : AppCompatActivity() , OnMapReadyCallback {
    private val PERMISSION_REQUEST_CODE = 123
    private val TOGGLE_MODE_SUPF = 0
    private val TOGGLE_MODE_DIST = 1
    private var toggle_mode = TOGGLE_MODE_SUPF
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    lateinit var texto: TextView
    lateinit var floatingActionButton: FloatingActionButton
    lateinit var botonMyLocation: FloatingActionButton
    lateinit var pin: BitmapDescriptor
    lateinit var togle_mode_button: Button
    lateinit var personalizar_button: Button
    val polygonOptions = PolygonOptions().fillColor(0x800000ff.toInt())
    val polylineOptions = PolylineOptions().zIndex(1.0f)
    var poligono: Polygon? = null
    var polyline: Polyline? = null
    var faseTipoMapa = 0
    var numeroOverlays = 0
    val listaOverlays = mutableListOf<GroundOverlay?>()
    var overlayToRemove: GroundOverlay? =
        null //Almacena el overlay a borrar cuando se detecta coincidencia.
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewEst: RecyclerView
    //private lateinit var adapterSuperficie: FirestoreRecyclerAdapter<ObjetoSuperficie, ObjetoSuperficieViewHolder>
   // private lateinit var adapterDist: FirestoreRecyclerAdapter<ObjetoDistancia, ObjetoDistViewHolder>

    //Objetos temporales en uso
    var objetoSuperficie: ObjetoSuperficie? = null
    var objetoDistancia: ObjetoDistancia? = null

    //Listas estaticas.
    var objSuperfList: List<ObjetoSuperficie> = Medidor.instance.objetoSuperficieList
    var objDistList: List<ObjetoDistancia> = Medidor.instance.objetoDistanciaList
    lateinit var adapterDistEst: ObjetoDistAdapterEst
    lateinit var adapterSupEst: ObjetoSupAdapterEst

    var tipoMapa = GoogleMap.MAP_TYPE_NORMAL
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        personalizar_button=findViewById<Button>(R.id.botonPersonalizar)
        val botonBorrar = findViewById<Button>(R.id.botonborrar)
        val botonActividad = findViewById<MaterialButton>(R.id.datosMatButtonMaps)
        pin = BitmapDescriptorFactory.fromResource(R.drawable.pin)
        texto = findViewById(R.id.datosMedicionTextView)
        floatingActionButton = findViewById(R.id.boton_flotante)
        recyclerViewEst=findViewById(R.id.recyclerStatic)
        togle_mode_button = findViewById<Button>(R.id.toggle_mode_button)
        adapterDistEst= ObjetoDistAdapterEst(objDistList, this)
        adapterSupEst= ObjetoSupAdapterEst(objSuperfList, this)
        recyclerViewEst.adapter=adapterSupEst

// REcicler de objetos estaticos. Se extraen al arrancar de Firebase, se
        recyclerViewEst = findViewById(R.id.recyclerStatic)//El nuevo recycler.
        recyclerViewEst.layoutManager = LinearLayoutManager(this)
        recyclerViewEst.adapter

        //PERMISOS GPS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no tienes permisos, solicítalos al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                }
            }
        }
        // BOTONES----------------------------O-------------------O---------------------------O
        //Cambiar el modo de medicion en mapa (Superficie o distancia)
        togle_mode_button.setOnClickListener {
            if (toggle_mode == TOGGLE_MODE_SUPF) {//Si está en modo superficie, cambiamos el recycler y el boton a distancia
                //recyclerView.adapter = adapterDist
                recyclerViewEst.adapter=adapterDistEst
                togle_mode_button.setText("SUPF.")
                toggle_mode = TOGGLE_MODE_DIST
                borradoPuntosyDatos()
            } else {//viceversa
                // recyclerView.adapter = adapterSuperficie
                recyclerViewEst.adapter=adapterSupEst
                togle_mode_button.setText("DIST.")
                toggle_mode = TOGGLE_MODE_SUPF
                borradoPuntosyDatos()
            }
        }
        //Añadir accion al boton de borrado
        botonBorrar.setOnClickListener {
            borradoPuntosyDatos()
        }
        //Cambiode activity a Introducir datos
        botonActividad.setOnClickListener {
            var intent = Intent(this, DatosActivity::class.java)
            startActivity(intent)
            //Toast.makeText(this,""+Medidor.instance.objetoDistanciaList,Toast.LENGTH_SHORT).show()
        }
        personalizar_button.setOnClickListener{
            var intent=Intent(this, PropiosActivity::class.java)
            startActivity(intent)
        }
        createMapFragment()
    }

    private fun obtenerUbicacionYCentrarMapa() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = tipoMapa
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        } else {
            botonMyLocation = findViewById(R.id.botonMyLocation)
            map.getUiSettings().setMyLocationButtonEnabled(false)
            map.isMyLocationEnabled = true
            botonMyLocation.setOnClickListener {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    }
                }
            }
        }

        //Accion Float button: Cambiar mapa
        floatingActionButton.setOnClickListener {
            faseTipoMapa++
            if (faseTipoMapa == 4) {
                faseTipoMapa = 0
            }
            when (faseTipoMapa) {
                0 -> tipoMapa = GoogleMap.MAP_TYPE_NORMAL
                1 -> tipoMapa = GoogleMap.MAP_TYPE_HYBRID
                2 -> tipoMapa = GoogleMap.MAP_TYPE_TERRAIN
                3 -> tipoMapa = GoogleMap.MAP_TYPE_SATELLITE
            }
            map.mapType = tipoMapa
            map.isBuildingsEnabled = true
            map.isIndoorEnabled = true
        }
        //ACCIONES SOBRE EL MAPA
        //Click largo OVERLAYS
        map.setOnMapLongClickListener {
            if (objetoSuperficie != null) {
                if (listaOverlays.size == 0) {//Si la lista está vacia, simplemente añadimos el overlay.
                    addOverlay(it)
                } else {//Si no está vacia
                    //VER SI YA HAY OVERLAY EN LA ZONA--> BORRARLO
                    for (gOverlay in listaOverlays) {
                        if (gOverlay != null && isPointInOverlayBounds(it,gOverlay)
                        ) {// Verifica si el punto está dentro de las coordenadas del overlay
                            overlayToRemove = gOverlay //Si está, lo dejamos para borrar una vez acabada la iteracion (si no, da error)
                        }
                    }
                    //ACABADA LA ITERACION:
                    //Si hay algo para borrar, borramos, si no, añadimos el punto.
                    if (overlayToRemove != null) {
                        listaOverlays.remove(overlayToRemove)// Quitar de la lista
                        overlayToRemove!!.remove()//eliminar
                        overlayToRemove = null
                    } else {
                        addOverlay(it)
                        overlayToRemove = null
                    }
                }
            } else {
                Toast.makeText(this, "Seleccione una opción", Toast.LENGTH_SHORT).show()
            }
        }
        //CLICK CORTO: dibujo de poligonos o lineas.
        map.setOnMapClickListener {
            if (toggle_mode == TOGGLE_MODE_SUPF) {
                if (objetoSuperficie != null) {
                    map.addMarker(MarkerOptions().draggable(true).position(it).icon(pin))
                    polygonOptions.add(it)
                    if (polygonOptions.points.size > 2) {
                        if (poligono != null) {
                            poligono!!.remove()
                        }

                        poligono = map.addPolygon(polygonOptions)
                        actualizarObjetoSuperficie(objetoSuperficie!!)
                    }
                } else {
                    Toast.makeText(this, "Seleccione una opcion", Toast.LENGTH_SHORT).show()
                }
            }
            if (toggle_mode == TOGGLE_MODE_DIST) {
                if (objetoDistancia != null) {
                    map.addMarker(MarkerOptions().draggable(true).position(it))
                    polylineOptions.add(it)
                    polyline = map.addPolyline(polylineOptions)
                    actualizarObjetoDistancia(objetoDistancia!!)
                    patron(polylineOptions)
                } else {
                    Toast.makeText(this, "Seleccione una opcion", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ///CUANDO PIDA LOS PERMISOS
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, realiza acciones relacionadas con la ubicación
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                map.isMyLocationEnabled = true
            } else {
                // Permiso denegado, toma medidas adecuadas (por ejemplo, informar al usuario)
            }
        }
    }

    //Rellenar el fragment
    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmento) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /*Cuando estamos en modo superficie, Cumprueba si el punto pulsado esta dentro de un overlay (se aplica a los overlays superpuestos
        en el mapa para borrar o para añadir punto
        Devuelve true si el punto está dentro de los bounds del overlay
        */
    private fun isPointInOverlayBounds(point: LatLng, overlay: GroundOverlay): Boolean {
        val overlayBounds = overlay.bounds
        val northeast = overlayBounds.northeast
        val southwest = overlayBounds.southwest
        val overlayLatLngBounds = LatLngBounds(southwest, northeast)
        return overlayLatLngBounds.contains(point)
    }

    //actualizan los datos con el objeto que tengamos seleccionado.
    fun actualizarObjetoSuperficie(obj_sup: ObjetoSuperficie) {
        if (polygonOptions.points.size > 2) {
            var area = SphericalUtil.computeArea(polygonOptions.points)
            texto.setText("%.2f".format(area / (obj_sup.ancho * obj_sup.alto)) + " " + obj_sup.unidad)
        }
    }

    fun actualizarObjetoDistancia(obj_dist: ObjetoDistancia) {
        if (polylineOptions.points.size > 1) {
            var distancia = SphericalUtil.computeLength(polylineOptions.points)
            texto.setText("%.2f".format(distancia / (obj_dist.valor)) + " " + obj_dist.unidad)
        }
    }

    //AÑADIR LOS OVERLAYS
    private fun addOverlay(latLng: LatLng) {
        if (numeroOverlays <= 700) {
            Glide.with(this)
                .load(objetoSuperficie!!.imagenURL)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        // Aquí puedes configurar tu overlay de mapa usando la imagen cargada
                        val overlayImage =
                            BitmapDescriptorFactory.fromBitmap(drawableToBitmap(resource)) //Necesitamos un bitmapdescriptor,no un bitmap.
                        var overlayOptions = GroundOverlayOptions()
                            .image(overlayImage)
                            .bearing(map.cameraPosition.bearing)
                            .position(
                                latLng,
                                objetoSuperficie!!.ancho.toFloat(),
                                objetoSuperficie!!.alto.toFloat()
                            ) // Define la posición y el tamaño del GroundOverlay
                            .transparency(0.2f) // Define la transparencia (0f para completamente opaco, 1f para completamente transparente)
                        val overlay = map.addGroundOverlay(overlayOptions)
                        listaOverlays.add(overlay)
                        numeroOverlays++
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Manejo si la carga es cancelada o eliminada
                    }
                })
        } else {
            Toast.makeText(this, "Numero de imagenes superado", Toast.LENGTH_SHORT).show()
        }
    }

    //Convierte un objeto drawable en bitmap
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun borradoPuntosyDatos() {
        //eliminar puntos
        polygonOptions.points.clear()
        polylineOptions.points.clear()
        //limpiar mapa de imagenes, puntos y lineas.
        map.clear()
        //limpiar lista de objetos que hacen referencia a las imagenes sobre el mapa.
        listaOverlays.clear()
        overlayToRemove == null
        poligono = null;
        polyline = null
        texto.text = ""
        numeroOverlays = 0
    }

    fun patron(polyline: PolylineOptions) {
        var points = polyline.points
        //Dibujamos patron solo si hay mas de dos puntos
        if (points.size > 1) {
            if (numeroOverlays < 1000) {//Si hay menos de 1000 overlays, dibujamos.
                Glide.with(this)
                    .load(objetoDistancia!!.imagenURL)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            // Aquí puedes configurar tu overlay de mapa usando la imagen cargada
                            val bitmap = drawableToBitmap(resource)
                            val overlayImage =
                                BitmapDescriptorFactory.fromBitmap(bitmap) //Necesitamos un bitmapdescriptor,no un bitmap.
                            GlobalScope.launch(Dispatchers.IO) {
                                for (i in points.size - 2 until points.size - 1) {
                                    val startPoint = points[i]
                                    val endPoint = points[i + 1]
                                    val distance =
                                        SphericalUtil.computeDistanceBetween(startPoint, endPoint) //Distancia entre los puntos de inicio y de fin (Para calcular el nº de imagenes
                                    val numImages = (distance / (objetoDistancia?.valor!!)).toInt()
                                    val deltaLat =
                                        (endPoint.latitude - startPoint.latitude) / numImages
                                    val deltaLng =
                                        (endPoint.longitude - startPoint.longitude) / numImages
                                    val bearing = Math.toDegrees(
                                        Math.atan2(
                                            endPoint.longitude - startPoint.longitude,
                                            endPoint.latitude - startPoint.latitude
                                        )
                                    ).toFloat()
                                    for (j in 0 until numImages) {
                                        if (numeroOverlays <= 1000) {
                                            numeroOverlays++

                                            val latLng = LatLng(
                                                startPoint.latitude + j * deltaLat ,
                                                startPoint.longitude + j * deltaLng
                                            )

                                            val overlayOptions = GroundOverlayOptions()
                                                .image(overlayImage)
                                                .position(
                                                    latLng,
                                                    objetoDistancia!!.valor,
                                                    objetoDistancia!!.valor
                                                )
                                                .bearing(bearing)
                                                .anchor(0.5f, 1.0f)
                                                .zIndex(2.0f)

                                            // Agrega el GroundOverlay en el hilo principal
                                            withContext(Dispatchers.Main) {
                                                val overlay = map.addGroundOverlay(overlayOptions)
                                                listaOverlays.add(overlay)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Manejo si la carga es cancelada o eliminada
                        }
                    })
            }
        }
        if (numeroOverlays > 1000) Toast.makeText(this, "Numero excedido", Toast.LENGTH_SHORT)
            .show()
    }
}



