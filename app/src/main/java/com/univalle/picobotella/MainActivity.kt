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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Variables de Audio
    private var mediaPlayerFondo: MediaPlayer? = null
    private var soundSpin: MediaPlayer? = null

    // Variables de Estado
    private var isAudioOn = true
    private var isSpinning = false
    private var ultimoAngulo = 0f

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
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)

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

        btnLogout.setOnClickListener {
            aplicarAnimacionToque(it) {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            }
        }

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

        btnVolume.setOnClickListener {
            aplicarAnimacionToque(it) {
                alternarAudio(btnVolume)
            }
        }

        btnInstructions.setOnClickListener {
            aplicarAnimacionToque(it) {
                if (isAudioOn) mediaPlayerFondo?.pause()
                startActivity(Intent(this, InstruccionesActivity::class.java))
            }
        }

        btnAddChallenge.setOnClickListener {
            aplicarAnimacionToque(it) {
                if (isAudioOn) mediaPlayerFondo?.pause()
                startActivity(Intent(this, RetosActivity::class.java))
            }
        }

        btnShare.setOnClickListener {
            aplicarAnimacionToque(it) {
                val eslogan = "App pico botella. Solo los valientes lo juegan !!\nhttps://play.google.com/store/apps/details?id=com.nequi.MobileApp"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, eslogan)
                }
                startActivity(Intent.createChooser(intent, "Compartir con:"))
            }
        }
    }

    // --- LÓGICA DE GIRO ---

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
                contador.text = (ms / 1000).toString()
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

    // --- HU 12.0 ACTUALIZADA: RETO PRIVADO DESDE FIRESTORE + POKEMON API ---
    private fun mostrarDialogoRetoAleatorio() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reto_aleatorio, null)
        val builder = AlertDialog.Builder(this).setView(dialogView).setCancelable(false)
        val dialog = builder.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtReto = dialogView.findViewById<TextView>(R.id.txtRetoElegido)
        val imgPoke = dialogView.findViewById<ImageView>(R.id.imgPokemon)
        val btnCerrar = dialogView.findViewById<Button>(R.id.btnCerrarReto)

        // 1. CARGAR RETO ALEATORIO DESDE LA CARPETA PRIVADA DEL USUARIO (UID)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val dbFirestore = FirebaseFirestore.getInstance()
            dbFirestore.collection("usuarios").document(uid).collection("mis_retos")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val listaDocumentos = snapshot.documents
                        val documentoAzar = listaDocumentos.random()
                        txtReto.text = documentoAzar.getString("descripcion") ?: "Reto sin descripción"
                    } else {
                        txtReto.text = "¡No tienes retos guardados en tu cuenta!"
                    }
                }
                .addOnFailureListener {
                    txtReto.text = "Error al conectar con la nube"
                }
        } else {
            txtReto.text = "Error: Usuario no identificado"
        }

        // 2. CARGAR POKEMON DE LA API (HILO SECUNDARIO)
        thread {
            try {
                val apiResponse = URL("https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json").readText()
                val json = JSONObject(apiResponse)
                val array = json.getJSONArray("pokemon")
                val randomPoke = array.getJSONObject((0 until array.length()).random())
                val imgUrl = randomPoke.getString("img").replace("http://", "https://")

                runOnUiThread {
                    Glide.with(this@MainActivity)
                        .load(imgUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(imgPoke)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        btnCerrar.setOnClickListener {
            dialog.dismiss()
            if (isAudioOn) mediaPlayerFondo?.start()
        }
    }

    // --- FUNCIONES AUXILIARES ---

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
        mediaPlayerFondo = null
        soundSpin?.release()
        soundSpin = null
    }
}