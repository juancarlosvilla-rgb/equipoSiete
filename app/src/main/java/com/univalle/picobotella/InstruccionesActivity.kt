package com.univalle.picobotella

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class InstruccionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instrucciones)

        // Criterio 8: Animación simple de escala para el triunfo
        val imgTriumph = findViewById<ImageView>(R.id.imgTriumph)
        val anim = AnimationUtils.loadAnimation(this, R.anim.splash_anim) // Reutilizamos la del splash o crea una nueva
        imgTriumph.startAnimation(anim)

        // Criterio 3: Botón volver
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressed() // Esto vuelve al Home
        }
    }
}