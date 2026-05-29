package com.univalle.picobotella

import android.media.MediaPlayer
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

    // Variables globales
    private var mediaPlayer: MediaPlayer? = null
    private var isAudioOn = true
    private var isSpinning = false

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

        // 2. Iniciar música de fondo (Asegúrate que el archivo esté en res/raw)
        mediaPlayer = MediaPlayer.create(this, R.raw.musica_fondo)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        // 3. Iniciar parpadeo del botón principal (HU 2.0)
        iniciarParpadeo(btnPresioname)

        // 4. Lógica de la Botella (Girar y contar)
        btnPresioname.setOnClickListener {
            if (!isSpinning) {
                girarBotella(imgBotella, txtContador)
            }
        }

        // 5. Lógica de la Toolbar con Animaciones (HU 3.0)
        btnStar.setOnClickListener {
            aplicarAnimacionToque(it) {
                Toast.makeText(this, "HU 4.0: Calificar (Próximamente)", Toast.LENGTH_SHORT).show()
            }
        }

        btnVolume.setOnClickListener {
            aplicarAnimacionToque(it) {
                alternarAudio(btnVolume)
            }
        }

        btnInstructions.setOnClickListener {
            aplicarAnimacionToque(it) {
                Toast.makeText(this, "HU 5.0: Instrucciones (Próximamente)", Toast.LENGTH_SHORT).show()
            }
        }

        btnAddChallenge.setOnClickListener {
            aplicarAnimacionToque(it) {
                Toast.makeText(this, "HU 6.0: Agregar Retos (Próximamente)", Toast.LENGTH_SHORT).show()
            }
        }

        btnShare.setOnClickListener {
            aplicarAnimacionToque(it) {
                Toast.makeText(this, "HU 10.0: Compartir (Próximamente)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- FUNCIONES DE LÓGICA DEL JUEGO ---

    private fun girarBotella(botella: ImageView, contador: TextView) {
        isSpinning = true
        contador.visibility = View.GONE

        // Ángulo aleatorio (mínimo 3 vueltas)
        val randomAngle = (360 * 3 + (0..360).random()).toFloat()

        botella.animate()
            .rotationBy(randomAngle)
            .setDuration(3000)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                iniciarCuentaRegresiva(contador)
            }
            .start()
    }

    private fun iniciarCuentaRegresiva(contador: TextView) {
        contador.visibility = View.VISIBLE
        object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val segundos = millisUntilFinished / 1000
                contador.text = segundos.toString()
            }
            override fun onFinish() {
                contador.visibility = View.GONE
                isSpinning = false
                Toast.makeText(this@MainActivity, "¡Reto!", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    // --- FUNCIONES DE LA TOOLBAR Y AUDIO ---

    private fun alternarAudio(boton: ImageButton) {
        if (isAudioOn) {
            mediaPlayer?.pause()
            boton.setImageResource(android.R.drawable.ic_lock_silent_mode)
            isAudioOn = false
        } else {
            mediaPlayer?.start()
            boton.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
            isAudioOn = true
        }
    }

    private fun aplicarAnimacionToque(view: View, accion: () -> Unit) {
        view.animate()
            .scaleX(0.7f)
            .scaleY(0.7f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .withEndAction { accion() }
                    .start()
            }
            .start()
    }

    private fun iniciarParpadeo(button: Button) {
        val anim = AlphaAnimation(1.0f, 0.4f)
        anim.duration = 600
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        button.startAnimation(anim)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}