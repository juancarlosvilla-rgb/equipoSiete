package com.univalle.picobotella

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RetosActivity : AppCompatActivity() {

    lateinit var db: DatabaseHelper
    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retos)

        db = DatabaseHelper(this)
        listView = findViewById(R.id.lvRetos)
        val btnBack = findViewById<ImageButton>(R.id.btnBackRetos)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddReto)

        actualizarLista()

        btnBack.setOnClickListener { onBackPressed() }

        fab.setOnClickListener {
            // Aquí lanzaremos el diálogo de la HU 7.0 pronto
            db.agregarReto("Nuevo reto manual")
            actualizarLista()
            Toast.makeText(this, "Reto agregado a la lista", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarLista() {
        val listaRetos = db.obtenerRetos() // Ahora obtenemos la lista de modelos, no solo texto
        val adapter = RetoAdapter(this, listaRetos)
        listView.adapter = adapter
    }
}