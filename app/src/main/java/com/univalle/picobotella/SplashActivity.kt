package com.univalle.picobotella

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 1. FORMA MODERNA DE OCULTAR BARRAS (Quita los Warnings de systemUiVisibility)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // 2. CONFIGURAR ANIMACIÓN
        val botella = findViewById<ImageView>(R.id.imgBotellaSplash)
        val animacion = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
        botella.startAnimation(animacion)

        // 3. FORMA SEGURA DE ESPERAR 5 SEGUNDOS (Quita el Warning de GlobalScope)
        lifecycleScope.launch {
            delay(5000) // 5 segundos exactos (Criterio 4)
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish() // Criterio 5: Finalizar para no volver atrás
        }
    }
}