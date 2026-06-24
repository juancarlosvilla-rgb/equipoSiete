package com.univalle.picobotella

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.univalle.picobotella.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPass = findViewById<TextInputEditText>(R.id.etPass)
        val tilPass = findViewById<TextInputLayout>(R.id.tilPass)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<TextView>(R.id.btnRegister)

        // --- VALIDACIÓN EN TIEMPO REAL ---
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = etEmail.text.toString().trim()
                val pass = etPass.text.toString().trim()

                // Criterio 5: Error visual
                if (pass.isNotEmpty() && pass.length < 6) {
                    tilPass.error = "Mínimo 6 dígitos"
                } else {
                    tilPass.error = null
                }

                // Habilitar si ambos campos están llenos
                val isReady = email.isNotEmpty() && pass.length >= 6

                btnLogin.isEnabled = isReady
                btnRegister.isEnabled = isReady

                if (isReady) {
                    // Estilo cuando está habilitado (Blanco Bold para Registrarse)
                    btnLogin.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF3D00"))
                    btnLogin.setTextColor(Color.WHITE)
                    btnLogin.typeface = Typeface.DEFAULT_BOLD

                    btnRegister.setTextColor(Color.WHITE) //
                    btnRegister.typeface = Typeface.DEFAULT_BOLD // Criterio 12: Bold
                } else {
                    // Estilo inactivo (Gris)
                    btnLogin.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
                    btnRegister.setTextColor(Color.parseColor("#9EA1A1")) // Criterio 11: Color gris
                    btnRegister.typeface = Typeface.DEFAULT
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etEmail.addTextChangedListener(watcher)
        etPass.addTextChangedListener(watcher)

        // --- ACCIONES DE LOS BOTONES ---

        btnLogin.setOnClickListener {
            viewModel.login(etEmail.text.toString(), etPass.text.toString())
        }

        btnRegister.setOnClickListener {
            viewModel.register(etEmail.text.toString(), etPass.text.toString())
        }

        // --- OBSERVADORES DE TOASTS

        // Observar Login
        viewModel.loginStatus.observe(this) { success ->
            if (success == true) {
                irAlHome()
            } else if (success == false) {
                Toast.makeText(this, "Login incorrecto", Toast.LENGTH_SHORT).show()
                viewModel.resetStatus()
            }
        }

        // Observar Registro
        viewModel.registerStatus.observe(this) { success ->
            if (success == true) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                irAlHome()
            } else if (success == false) {
                // Criterio 13: Toast Error en el registro
                Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show()
                viewModel.resetStatus()
            }
        }
    }

    private fun irAlHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}