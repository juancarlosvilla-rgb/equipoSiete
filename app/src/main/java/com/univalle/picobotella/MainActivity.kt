package com.univalle.picobotella

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Variables de Audio
    private var mediaPlayerFondo: MediaPlayer? = null // Música ambiente
    private var soundSpin: MediaPlayer? = null       // Sonido de giro

    // Variables de Estado
    private var isAudioOn = true
    private var isSpinning = false
    private var ultimoAngulo = 0f // Guardar posición previa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Referencias de la UI
        val imgBotella = findViewById<ImageView>(R.id.imgBotellaMain)
        val txtContador = findViewById<TextView>(R.id.txtContador)
        val btnPresioname = findViewById<Button>(R.id.btnPresioname)

        // Referencias de la Toolbar
        val btnStar = findViewById<ImageButton>(R.id.btnStar)
        val btnVolume = findViewById<ImageButton>(R.id.btnVolume)
        val btnInstructions = findViewById<ImageButton>(R.id.btnInstructions)
        val btnAddChallenge = findViewById<ImageButton>(R.id.btnAddChallenge)
        val btnShare = findViewById<ImageButton>(R.id.btnShare)

        // 2. Iniciar música de fondo
        mediaPlayerFondo = MediaPlayer.create(this, R.raw.musica_fondo)
        mediaPlayerFondo?.isLooping = true
        if (isAudioOn) mediaPlayerFondo?.start()

        // 3. Iniciar parpadeo del botón
        iniciarParpadeo(btnPresioname)

        // 4. Lógica del Giro de Botella
        btnPresioname.setOnClickListener {
            if (!isSpinning) {
                lanzarGiroBotella(imgBotella, txtContador, btnPresioname)
            }
        }

        // --- LÓGICA DE LA TOOLBAR ---

        // HU 4.0: Calificar
        btnStar.setOnClickListener {
            aplicarAnimacionToque(it) {
                val urlNequi = "https://play.google.com/store/apps/details?id=com.nequi.MobileApp"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlNequi))
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "No se pudo abrir la tienda", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Control de Volumen (HU 3.0)
        btnVolume.setOnClickListener {
            aplicarAnimacionToque(it) {
                alternarAudio(btnVolume)
            }
        }

        // HU 5.0: Instrucciones
        btnInstructions.setOnClickListener {
            aplicarAnimacionToque(it) {
                if (isAudioOn) mediaPlayerFondo?.pause()
                val intent = Intent(this, InstruccionesActivity::class.java)
                startActivity(intent)
            }
        }

        // HU 6.0: Ver Retos
        btnAddChallenge.setOnClickListener {
            aplicarAnimacionToque(it) {
                if (isAudioOn) mediaPlayerFondo?.pause()
                val intent = Intent(this, RetosActivity::class.java)
                startActivity(intent)
            }
        }

        // HU 10.0: Compartir
        btnShare.setOnClickListener {
            aplicarAnimacionToque(it) {
                val tituloApp = "App pico botella."
                val eslogan = "Solo los valientes lo juegan !!"
                val urlApp = "https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"
                val mensajeFinal = "$tituloApp\n$eslogan\n$urlApp"

                val intentCompartir = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, mensajeFinal)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intentCompartir, "Compartir con:"))
            }
        }
    }

    // --- LÓGICA HU 11.0 (BOTELLA Y CUENTA REGRESIVA) ---

    private fun lanzarGiroBotella(botella: ImageView, contador: TextView, boton: Button) {
        isSpinning = true

        // El botón desaparece al iniciar
        boton.visibility = View.GONE
        boton.clearAnimation() // Detener parpadeo mientras no está

        // Pausar música de fondo durante el giro
        if (isAudioOn) mediaPlayerFondo?.pause()

        // Iniciar sonido de giro
        soundSpin = MediaPlayer.create(this, R.raw.sonido_giro)
        soundSpin?.start()

        // Cálculo de giro aleatorio
        val tiempoGiro = (3000..5000).random().toLong() // Entre 3 y 5 seg
        val vueltasExtra = (5..10).random()
        val gradosAleatorios = (vueltasExtra * 360) + (0..360).random()

        // Sumamos al ángulo actual para que no salte
        val anguloObjetivo = ultimoAngulo + gradosAleatorios

        botella.animate()
            .rotation(anguloObjetivo)
            .setDuration(tiempoGiro)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                // Detener sonido de giro
                soundSpin?.stop()
                soundSpin?.release()
                soundSpin = null

                // Actualizar último ángulo
                ultimoAngulo = anguloObjetivo

                // Iniciar cuenta regresiva
                iniciarCuentaRegresivaFinal(contador, boton)
            }
            .start()
    }

    private fun iniciarCuentaRegresivaFinal(contador: TextView, boton: Button) {
        contador.visibility = View.VISIBLE
        // Cuenta de 3 a 0
        object : CountDownTimer(4000, 1000) {
            override fun onTick(ms: Long) {
                val segundos = ms / 1000
                contador.text = segundos.toString()
            }
            override fun onFinish() {
                contador.visibility = View.GONE

                // Lanzar HU 12
                Toast.makeText(this@MainActivity, "¡RETO SELECCIONADO!", Toast.LENGTH_SHORT).show()

                // El botón vuelve a aparecer
                boton.visibility = View.VISIBLE
                iniciarParpadeo(boton) // Reiniciar parpadeo

                // El audio vuelve si estaba en ON
                if (isAudioOn) mediaPlayerFondo?.start()

                isSpinning = false
            }
        }.start()
    }

    // --- FUNCIONES DE APOYO Y CICLO DE VIDA ---

    private fun alternarAudio(boton: ImageButton) {
        if (isAudioOn) {
            mediaPlayerFondo?.pause()
            boton.setImageResource(android.R.drawable.ic_lock_silent_mode)
            isAudioOn = false
        } else {
            mediaPlayerFondo?.start()
            boton.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
            isAudioOn = true
        }
    }

    private fun aplicarAnimacionToque(view: View, accion: () -> Unit) {
        view.animate().scaleX(0.7f).scaleY(0.7f).setDuration(100).withEndAction {
            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).withEndAction { accion() }.start()
        }.start()
    }

    private fun iniciarParpadeo(button: Button) {
        val anim = AlphaAnimation(1.0f, 0.4f)
        anim.duration = 600
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        button.startAnimation(anim)
    }

    override fun onRestart() {
        super.onRestart()
        if (isAudioOn) mediaPlayerFondo?.start()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayerFondo?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerFondo?.release()
        soundSpin?.release()
    }
}