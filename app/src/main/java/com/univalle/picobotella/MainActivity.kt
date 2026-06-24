package com.univalle.picobotella

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

@AndroidEntryPoint // Etiqueta necesaria para Dagger Hilt (RA-1)
class MainActivity : AppCompatActivity() {

    // Variables de Audio
    private var mediaPlayerFondo: MediaPlayer? = null // Música ambiente
    private var soundSpin: MediaPlayer? = null       // Sonido de giro

    // Variables de Estado
    private var isAudioOn = true
    private var isSpinning = false
    private var ultimoAngulo = 0f // Criterio 4 HU 11: Guardar posición previa

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

        // 2. Iniciar música de fondo (HU 2.0 )
        mediaPlayerFondo = MediaPlayer.create(this, R.raw.musica_fondo)
        mediaPlayerFondo?.isLooping = true
        if (isAudioOn) mediaPlayerFondo?.start()

        // 3. Iniciar parpadeo del botón (HU 2.0 )
        iniciarParpadeo(btnPresioname)

        // 4. Lógica del Giro de Botella (HU 11.0)
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
                if (isAudioOn) {
                    mediaPlayerFondo?.pause()
                }
                val intent = Intent(this, InstruccionesActivity::class.java)
                startActivity(intent)
            }
        }

        // HU 6.0: Ver Retos
        btnAddChallenge.setOnClickListener {
            aplicarAnimacionToque(it) {
                if (isAudioOn) mediaPlayerFondo?.pause() // Pausa audio
                val intent = Intent(this, RetosActivity::class.java)
                startActivity(intent)
            }
        }

        // HU 10.0: Lógica compartir la aplicación
        btnShare.setOnClickListener {
            aplicarAnimacionToque(it) {
                // Criterio 2: Definir el mensaje exacto solicitado
                val tituloApp = "App pico botella."
                val eslogan = "Solo los valientes lo juegan !!"
                val urlApp = "https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"

                val mensajeFinal = "$tituloApp\n$eslogan\n$urlApp"

                // Intent para mostrar el Bottom Sheet del sistema
                val intentCompartir = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, mensajeFinal)
                    type = "text/plain"
                }

                // Mostrar el selector de aplicaciones (Chooser)
                val chooser = Intent.createChooser(intentCompartir, "Compartir con:")
                startActivity(chooser)
            }
        }
    }

    // --- FUNCIONES DE CICLO DE VIDA (FUERA DE ONCREATE) ---

    // Criterio 3 HU 5.0: Restablecer audio al volver de las instrucciones
    override fun onRestart() {
        super.onRestart()
        if (isAudioOn) {
            mediaPlayerFondo?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerFondo?.release()
        mediaPlayerFondo = null
    }

    // --- LÓGICA HU 11.0 (GIRO Y CUENTA REGRESIVA) ---

    private fun lanzarGiroBotella(botella: ImageView, contador: TextView, boton: Button) {
        isSpinning = true
        boton.visibility = View.GONE
        boton.clearAnimation()

        if (isAudioOn) mediaPlayerFondo?.pause()

        soundSpin = MediaPlayer.create(this, R.raw.sonido_giro)
        soundSpin?.start()

        val tiempoGiro = (3000..5000).random().toLong()
        val vueltasExtra = (5..10).random()
        val gradosAleatorios = (vueltasExtra * 360) + (0..360).random()
        val anguloObjetivo = ultimoAngulo + gradosAleatorios

        botella.animate()
            .rotation(anguloObjetivo)
            .setDuration(tiempoGiro)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                soundSpin?.stop()
                soundSpin?.release()
                soundSpin = null

                ultimoAngulo = anguloObjetivo
                iniciarCuentaRegresivaFinal(contador, boton)
            }
            .start()
    }

    private fun iniciarCuentaRegresivaFinal(contador: TextView, boton: Button) {
        contador.visibility = View.VISIBLE
        object : CountDownTimer(4000, 1000) {
            override fun onTick(ms: Long) {
                val segundos = ms / 1000
                contador.text = segundos.toString()
            }
            override fun onFinish() {
                contador.visibility = View.GONE
                mostrarDialogoRetoAleatorio()
                boton.visibility = View.VISIBLE
                iniciarParpadeo(boton)
                isSpinning = false
            }
        }.start()
    }

    private fun mostrarDialogoRetoAleatorio() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reto_aleatorio, null)
        val builder = AlertDialog.Builder(this).setView(dialogView).setCancelable(false)
        val dialog = builder.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtReto = dialogView.findViewById<TextView>(R.id.txtRetoElegido)
        val imgPoke = dialogView.findViewById<ImageView>(R.id.imgPokemon)
        val btnCerrar = dialogView.findViewById<Button>(R.id.btnCerrarReto)

        val db = DatabaseHelper(this)
        txtReto.text = db.obtenerRetoAleatorio()

        thread {
            try {
                val apiResponse = URL("https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json").readText()
                val json = JSONObject(apiResponse)
                val array = json.getJSONArray("pokemon")
                val randomPoke = array.getJSONObject((0 until array.length()).random())
                val imgUrl = randomPoke.getString("img").replace("http://", "https://")

                runOnUiThread {
                    Glide.with(this).load(imgUrl).placeholder(android.R.drawable.ic_menu_gallery).into(imgPoke)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        btnCerrar.setOnClickListener {
            dialog.dismiss()
            if (isAudioOn) mediaPlayerFondo?.start()
        }
    }

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

    override fun onPause() {
        super.onPause()
        mediaPlayerFondo?.pause()
    }
}