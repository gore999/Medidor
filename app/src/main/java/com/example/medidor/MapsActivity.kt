package com.example.medidor

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build.VERSION_CODES.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil
import com.google.maps.android.ui.IconGenerator
import java.util.Arrays
import kotlinx.coroutines.*
class MapsActivity : AppCompatActivity() , OnMapReadyCallback {
    private val PERMISSION_REQUEST_CODE = 123
    private val TOGGLE_MODE_SUPF=0
    private val TOGGLE_MODE_DIST=1
    private var toggle_mode=TOGGLE_MODE_SUPF
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap

    lateinit var texto: TextView
    lateinit var floatingActionButton: FloatingActionButton
    lateinit var togle_mode_button: Button
    val polygonOptions = PolygonOptions().fillColor(0x800000ff.toInt())
    val polylineOptions = PolylineOptions().zIndex(1.0f)
    var poligono: Polygon?=null
    var polyline: Polyline?=null
    var faseTipoMapa=0
    val listaOverlays= mutableListOf<GroundOverlay?>()
    var overlayToRemove: GroundOverlay?=null //Almacena el overlay a borrar cuando se detecta coincidencia.
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterSuperficie: FirestoreRecyclerAdapter<ObjetoSuperficie, ObjetoSuperficieViewHolder>
    private lateinit var adapterDist: FirestoreRecyclerAdapter<ObjetoDistancia, ObjetoDistViewHolder>
    var objetoSuperficie: ObjetoSuperficie?=null
    var objetoDistancia: ObjetoDistancia?=null
    var tipoMapa=GoogleMap.MAP_TYPE_NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val boton= findViewById<Button>(R.id.botonborrar)
        val botonActividad= findViewById<MaterialButton>(R.id.datosMatButtonMaps)
        texto=findViewById(R.id.datosMedicionTextView)
        floatingActionButton=findViewById(R.id.boton_flotante)
        togle_mode_button=findViewById<Button>(R.id.toggle_mode_button)
        togle_mode_button.setOnClickListener {
            if(toggle_mode==TOGGLE_MODE_SUPF){//Si está en modo superficie, cambiamos el recycler y el boton a distancia
                recyclerView.adapter=adapterDist
                togle_mode_button.setText("Dist")
                toggle_mode=TOGGLE_MODE_DIST
                borradoPuntosyDatos()
            }
           else{//viceversa

                recyclerView.adapter=adapterSuperficie
                togle_mode_button.setText("Supf")
                toggle_mode=TOGGLE_MODE_SUPF
                borradoPuntosyDatos()
            }
        }

        //Iniciar recicler
        recyclerView = findViewById(R.id.recyclrMaps)
        recyclerView.layoutManager = LinearLayoutManager(this)
        //Establecer el modelo de superficie por defecto al Recycler.
        changeAdapterObjetoDistancia()//iniciar los adapters
        changeAdapterObjetoSuperficie()

        //PERMISOS GPS
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
        //Añadir accion al boton de borrado
        boton.setOnClickListener {
            borradoPuntosyDatos()
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
        map.mapType=tipoMapa
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

        map.isMyLocationEnabled=true

        //Accion Float button: Cambiar mapa
        floatingActionButton.setOnClickListener{
            faseTipoMapa++
            if(faseTipoMapa==4){faseTipoMapa=0 }
            when(faseTipoMapa) {
                0->tipoMapa = GoogleMap.MAP_TYPE_NORMAL
                1->tipoMapa = GoogleMap.MAP_TYPE_HYBRID
                2->tipoMapa= GoogleMap.MAP_TYPE_TERRAIN
                3->tipoMapa= GoogleMap.MAP_TYPE_SATELLITE
            }
            map.mapType=tipoMapa
            map.isBuildingsEnabled=true
            map.isIndoorEnabled=true
        }
        //ACCIONES SOBRE EL MAPA
        //Click largo OVERLAYS
        map.setOnMapLongClickListener {
            if(listaOverlays.size==0){//Si la lista está vacia, simplemente añadimos el overlay.
                addOverlay(it)}
            else{//Si no está vacia
                //VER SI YA HAY OVERLAY EN LA ZONA--> BORRARLO
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
        //CLICK CORTO: dibujo de poligonos o lineas.
        map.setOnMapClickListener {
            if (toggle_mode == TOGGLE_MODE_SUPF) {
                if(objetoSuperficie!=null){
                    map.addMarker(MarkerOptions().draggable(true).position(it))
                    polygonOptions.add(it)
                    if (polygonOptions.points.size > 2) {
                        if (poligono != null) {
                            poligono!!.remove()
                        }
                        poligono = map.addPolygon(polygonOptions)
                        actualizarObjetoSuperficie(objetoSuperficie!!)
                    }
                }else{
                    Toast.makeText(this,"Seleccione una opcion",Toast.LENGTH_SHORT).show()
                }
            }
            if(toggle_mode==TOGGLE_MODE_DIST){
                if(objetoDistancia!=null){
                    map.addMarker(MarkerOptions().draggable(true).position(it))
                    polylineOptions.add(it)
                    polyline=map.addPolyline(polylineOptions)
                    actualizarObjetoDistancia(objetoDistancia!!)
                    patron(polylineOptions)
                }
               else{
                   Toast.makeText(this,"Seleccione una opcion",Toast.LENGTH_SHORT).show()
               }
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
                map.isMyLocationEnabled=true
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

    private fun isPointInOverlayBounds(point: LatLng, overlay: GroundOverlay): Boolean {
        val overlayBounds = overlay.bounds
        val northeast = overlayBounds.northeast
        val southwest = overlayBounds.southwest
        val overlayLatLngBounds = LatLngBounds(southwest, northeast)
        return overlayLatLngBounds.contains(point)
    }

    //actualiza los datos con el objeto que tengamos seleccionado.
    private fun actualizarObjetoSuperficie (obj_sup:ObjetoSuperficie){
        if(polygonOptions.points.size>2){
            var area = SphericalUtil.computeArea(polygonOptions.points)
            texto.setText("%.2f".format(area/(obj_sup.ancho*obj_sup.alto))+" "+obj_sup.unidad)
        }
    }
    private fun actualizarObjetoDistancia (obj_dist:ObjetoDistancia){
        if(polylineOptions.points.size>1){
            var distancia = SphericalUtil.computeLength(polylineOptions.points)
            texto.setText("%.2f".format(distancia/(obj_dist.valor))+" "+obj_dist.unidad)
        }
    }
    private fun addOverlay(latLng: LatLng){
        Glide.with(this)
            .load(objetoSuperficie!!.imagenURL)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    // Aquí puedes configurar tu overlay de mapa usando la imagen cargada
                    val overlayImage = BitmapDescriptorFactory.fromBitmap(drawableToBitmap(resource)) //Necesitamos un bitmapdescriptor,no un bitmap.
                    var overlayOptions= GroundOverlayOptions()
                        .image(overlayImage)
                        .bearing(map.cameraPosition.bearing)
                        .position(latLng, objetoSuperficie!!.ancho.toFloat(), objetoSuperficie!!.alto.toFloat()) // Define la posición y el tamaño del GroundOverlay
                        .transparency(0.2f) // Define la transparencia (0f para completamente opaco, 1f para completamente transparente)
                    val overlay = map.addGroundOverlay(overlayOptions)
                    listaOverlays.add(overlay)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Manejo si la carga es cancelada o eliminada
                }
            })
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

    private fun changeAdapterObjetoSuperficie(){
        val query = FirebaseFirestore.getInstance().collection("obj_superficie")
        val options = FirestoreRecyclerOptions.Builder<ObjetoSuperficie>()
            .setQuery(query, ObjetoSuperficie::class.java)
            .build()
        adapterSuperficie = object : FirestoreRecyclerAdapter<ObjetoSuperficie, ObjetoSuperficieViewHolder>(options) {
            var selected=-1
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ObjetoSuperficieViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.objeto_superficie, parent, false)
                return ObjetoSuperficieViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: ObjetoSuperficieViewHolder,
                position: Int,
                model: ObjetoSuperficie
            ) {
                holder.bind(model)
                if(position==selected){
                    holder.itemView.findViewById<ImageView>(R.id.selectedimage).setImageDrawable(resources.getDrawable(R.drawable.check_mark_on))
                }else{
                    holder.itemView.findViewById<ImageView>(R.id.selectedimage).setImageDrawable(resources.getDrawable(R.drawable.check_mark_off))
                }
                holder.itemView.setOnClickListener {
                    objetoSuperficie = model
                    actualizarObjetoSuperficie(objetoSuperficie!!)
                    it.findViewById<ImageView>(R.id.selectedimage).setImageDrawable(resources.getDrawable(R.drawable.check_mark_on))
                    selected=position
                    notifyDataSetChanged()
                }

            }
        }
        recyclerView.adapter = adapterSuperficie
    }
    inner class ObjetoSuperficieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.tituloCard)
        private val descripcionTextView: TextView = itemView.findViewById(R.id.descripcionCard)
        private val imageView: ImageView = itemView.findViewById(R.id.iconoCard)

        fun bind(data: ObjetoSuperficie) {
            nombreTextView.text = data.nombre
            descripcionTextView.text = data.unidad
            Glide.with(imageView.context).load(data.icono).into(imageView)
        }
    }
    private fun changeAdapterObjetoDistancia(){
        val query = FirebaseFirestore.getInstance().collection("obj_distancia")
        val options = FirestoreRecyclerOptions.Builder<ObjetoDistancia>()
            .setQuery(query, ObjetoDistancia::class.java)
            .build()

        adapterDist = object : FirestoreRecyclerAdapter<ObjetoDistancia, ObjetoDistViewHolder>(options) {
            var selected=-1
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ObjetoDistViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.objeto_distancia, parent, false)
                return ObjetoDistViewHolder(view)
            }

            override fun onBindViewHolder(holder: ObjetoDistViewHolder, position: Int, model: ObjetoDistancia) {
                holder.bind(model)
                if(position==selected){
                    holder.itemView.findViewById<ImageView>(R.id.selecteddistimage).setImageDrawable(resources.getDrawable(R.drawable.check_mark_on))
                }else{
                    holder.itemView.findViewById<ImageView>(R.id.selecteddistimage).setImageDrawable(resources.getDrawable(R.drawable.check_mark_off))
                }
                holder.itemView.setOnClickListener{
                    val item=snapshots.getSnapshot(position).toObject(ObjetoSuperficie::class.java)
                    objetoDistancia=model
                    actualizarObjetoDistancia(objetoDistancia!!)
                    it.findViewById<ImageView>(R.id.selecteddistimage).setImageDrawable(resources.getDrawable(R.drawable.check_mark_on))
                    selected=position
                    notifyDataSetChanged()
                }
            }
        }
        recyclerView.adapter = adapterDist
    }

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
    }

    override fun onStart() {
        super.onStart()
        adapterSuperficie.startListening()
        adapterDist.startListening()
    }
    override fun onStop() {
        super.onStop()
        adapterSuperficie.stopListening()
        adapterDist.stopListening()
    }

    fun patron(polyline: PolylineOptions){
        var points= polyline.points
        if(points.size>1) {
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
                                    SphericalUtil.computeDistanceBetween(startPoint, endPoint)
                                val numImages = (distance / (objetoDistancia?.valor!!)).toInt()
                                val deltaLat = (endPoint.latitude - startPoint.latitude) / numImages
                                val deltaLng =
                                    (endPoint.longitude - startPoint.longitude) / numImages

                                for (j in 0 until numImages) {
                                    val latLng = LatLng(
                                        startPoint.latitude + j * deltaLat,
                                        startPoint.longitude + j * deltaLng
                                    )

                                    val bearing = Math.toDegrees(
                                        Math.atan2(
                                            endPoint.longitude - startPoint.longitude,
                                            endPoint.latitude - startPoint.latitude
                                        )
                                    ).toFloat()

                                    val overlayOptions = GroundOverlayOptions()
                                        .image(overlayImage)
                                        .position(
                                            latLng,
                                            objetoDistancia!!.valor,
                                            objetoDistancia!!.valor
                                        )
                                        .bearing(bearing)
                                        .anchor(0.5f, 0.5f)
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
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Manejo si la carga es cancelada o eliminada
                    }
                })
        }
    }
}

