package com.univalle.picobotella

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Etiqueta necesaria para Dagger Hilt (RA-1)
class InstruccionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instrucciones)

        // Animación simple de escala para el triunfo
        val imgTriumph = findViewById<ImageView>(R.id.imgTriumph)
        val anim = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
        imgTriumph.startAnimation(anim)

        // Botón volver
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            // Usamos el dispatcher moderno para volver atrás
            onBackPressedDispatcher.onBackPressed()
        }
    }
}