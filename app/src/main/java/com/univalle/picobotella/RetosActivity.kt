package com.univalle.picobotella

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.univalle.picobotella.viewmodel.RetosViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RetosActivity : AppCompatActivity() {
    
    private val viewModel: RetosViewModel by viewModels()
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retos)

        listView = findViewById(R.id.lvRetos)
        val btnBack = findViewById<ImageButton>(R.id.btnBackRetos)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddReto)

        // 1. Observar los retos de Firebase Firestore
        viewModel.retos.observe(this) { lista ->
            val adapter = RetoAdapter(this, lista.toMutableList())
            listView.adapter = adapter
        }

        // 2. Cargar los datos desde internet al entrar
        viewModel.cargar()

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Abrir diálogo para agregar nuevo reto a la nube
        fab.setOnClickListener {
            mostrarDialogoAgregar()
        }
    }

    // --- LÓGICA HU 7.0: DIÁLOGO AGREGAR (FIRESTORE) ---
    private fun mostrarDialogoAgregar() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_reto, null)
        val mAlertDialog = AlertDialog.Builder(this).setView(mDialogView).setCancelable(false).show()
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etReto = mDialogView.findViewById<EditText>(R.id.etNuevoReto)
        val btnGuardar = mDialogView.findViewById<Button>(R.id.btnGuardarDialog)
        val btnCancelar = mDialogView.findViewById<Button>(R.id.btnCancelarDialog)

        btnGuardar.isEnabled = false
        etReto.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString().trim()
                btnGuardar.isEnabled = texto.isNotEmpty()
                btnGuardar.backgroundTintList = ColorStateList.valueOf(
                    if (texto.isNotEmpty()) Color.parseColor("#FF4500") else Color.parseColor("#CCCCCC")
                )
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, after: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        btnGuardar.setOnClickListener {
            val texto = etReto.text.toString().trim()
            viewModel.agregar(texto) // Se envía a Google Firestore
            mAlertDialog.dismiss()
            Toast.makeText(this, "Reto guardado", Toast.LENGTH_SHORT).show()
        }
        btnCancelar.setOnClickListener { mAlertDialog.dismiss() }
    }

    // --- HU 8.0: DIÁLOGO EDITAR (FIRESTORE) ---
    fun mostrarDialogoEditar(reto: RetoModel) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_reto, null)
        val mAlertDialog = AlertDialog.Builder(this).setView(mDialogView).setCancelable(false).show()
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etReto = mDialogView.findViewById<EditText>(R.id.etEditarReto)
        val btnGuardar = mDialogView.findViewById<Button>(R.id.btnGuardarEdit)
        val btnCancelar = mDialogView.findViewById<Button>(R.id.btnCancelarEdit)

        etReto.setText(reto.descripcion)

        btnGuardar.setOnClickListener {
            val nuevoTexto = etReto.text.toString().trim()
            if (nuevoTexto.isNotEmpty()) {
                viewModel.editar(reto.id, nuevoTexto) // Actualiza en la nube usando el ID
                mAlertDialog.dismiss()
                Toast.makeText(this, "Reto actualizado", Toast.LENGTH_SHORT).show()
            }
        }
        btnCancelar.setOnClickListener { mAlertDialog.dismiss() }
    }

    // --- HU 9.0: DIÁLOGO ELIMINAR (FIRESTORE) ---
    fun mostrarDialogoEliminar(reto: RetoModel) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_eliminar_reto, null)
        val mAlertDialog = AlertDialog.Builder(this).setView(mDialogView).setCancelable(false).show()
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtDesc = mDialogView.findViewById<TextView>(R.id.txtRetoAEliminar)
        val btnSi = mDialogView.findViewById<Button>(R.id.btnSiEliminar)
        val btnNo = mDialogView.findViewById<Button>(R.id.btnNoEliminar)

        txtDesc.text = reto.descripcion

        btnSi.setOnClickListener {
            viewModel.eliminar(reto.id) // Se borra permanentemente de Firestore
            mAlertDialog.dismiss()
            Toast.makeText(this, "Reto eliminado", Toast.LENGTH_SHORT).show()
        }
        btnNo.setOnClickListener { mAlertDialog.dismiss() }
    }
}