package com.univalle.picobotella

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isSpinning = false // Para evitar que giren mientras ya está girando

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPresioname = findViewById<Button>(R.id.btnPresioname)
        val imgBotella = findViewById<ImageView>(R.id.imgBotellaMain)
        val txtContador = findViewById<TextView>(R.id.txtContador)

        // Iniciar música de fondo
        mediaPlayer = MediaPlayer.create(this, R.raw.musica_fondo)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        // Animación de parpadeo para el botón
        iniciarParpadeo(btnPresioname)

        // Acción al presionar el botón
        btnPresioname.setOnClickListener {
            if (!isSpinning) {
                girarBotella(imgBotella, txtContador)
            }
        }
    }

    private fun girarBotella(botella: ImageView, contador: TextView) {
        isSpinning = true
        contador.visibility = View.GONE // Ocultar contador si estaba visible

        // Generar un ángulo aleatorio (mínimo 3 vueltas completas + ángulo azar)
        val randomAngle = (360 * 3 + (0..360).random()).toFloat()

        // Animación de giro
        botella.animate()
            .rotationBy(randomAngle)
            .setDuration(3000) // Duración de 3 segundos (HU 11.0)
            .setInterpolator(DecelerateInterpolator()) // Empieza rápido y frena suave
            .withEndAction {
                // Cuando termina de girar, inicia el contador
                iniciarCuentaRegresiva(contador)
            }
            .start()
    }

    private fun iniciarCuentaRegresiva(contador: TextView) {
        contador.visibility = View.VISIBLE

        object : CountDownTimer(4000, 1000) { // 4 segundos para que muestre 3, 2, 1, 0
            override fun onTick(millisUntilFinished: Long) {
                val segundosRestantes = millisUntilFinished / 1000
                if (segundosRestantes > 0) {
                    contador.text = segundosRestantes.toString()
                } else {
                    contador.text = "0"
                }
            }

            override fun onFinish() {
                contador.visibility = View.GONE
                isSpinning = false
                // Aquí podrías lanzar el diálogo del reto (HU 12.0)
            }
        }.start()
    }

    private fun iniciarParpadeo(button: Button) {
        val animacion = AlphaAnimation(1.0f, 0.4f)
        animacion.duration = 600
        animacion.repeatMode = Animation.REVERSE
        animacion.repeatCount = Animation.INFINITE
        button.startAnimation(animacion)
    }
}