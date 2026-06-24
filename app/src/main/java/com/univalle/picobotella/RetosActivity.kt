package com.univalle.picobotella

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Etiqueta necesaria para Dagger Hilt (RA-1)
class RetosActivity : AppCompatActivity() {

    lateinit var db: DatabaseHelper
    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retos)

        db = DatabaseHelper(this)
        listView = findViewById(R.id.lvRetos)
        val btnBack = findViewById<ImageButton>(R.id.btnBackRetos)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddReto)

        actualizarLista()

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // HU 6.0: Al dar clic lanza el diálogo del HU 7.0
        fab.setOnClickListener {
            mostrarDialogoAgregar()
        }
    }

    // LÓGICA HU 7.0: CUADRO DE DIÁLOGO AGREGAR RETO
    private fun mostrarDialogoAgregar() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_reto, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setCancelable(false)

        val mAlertDialog = mBuilder.show()
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etReto = mDialogView.findViewById<EditText>(R.id.etNuevoReto)
        val btnGuardar = mDialogView.findViewById<Button>(R.id.btnGuardarDialog)
        val btnCancelar = mDialogView.findViewById<Button>(R.id.btnCancelarDialog)

        btnGuardar.isEnabled = false
        btnGuardar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CCCCCC"))

        etReto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString().trim()
                if (texto.isNotEmpty()) {
                    btnGuardar.isEnabled = true
                    btnGuardar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF4500"))
                } else {
                    btnGuardar.isEnabled = false
                    btnGuardar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnCancelar.setOnClickListener {
            mAlertDialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val textoReto = etReto.text.toString().trim()
            db.agregarReto(textoReto)
            actualizarLista()
            mAlertDialog.dismiss()
            Toast.makeText(this, "Reto guardado correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    // HU 8.0: CUADRO DE DIÁLOGO EDITAR RETO
    fun mostrarDialogoEditar(reto: RetoModel) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_reto, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setCancelable(false)

        val mAlertDialog = mBuilder.show()
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etReto = mDialogView.findViewById<EditText>(R.id.etEditarReto)
        val btnGuardar = mDialogView.findViewById<Button>(R.id.btnGuardarEdit)
        val btnCancelar = mDialogView.findViewById<Button>(R.id.btnCancelarEdit)

        etReto.setText(reto.descripcion)

        btnCancelar.setOnClickListener {
            mAlertDialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val nuevoTexto = etReto.text.toString().trim()
            if (nuevoTexto.isNotEmpty()) {
                db.editarReto(reto.id, nuevoTexto)
                actualizarLista()
                mAlertDialog.dismiss()
                Toast.makeText(this, "Reto actualizado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // HU 9.0: CUADRO DE DIÁLOGO ELIMINAR RETO
    fun mostrarDialogoEliminar(reto: RetoModel) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_eliminar_reto, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setCancelable(false)

        val mAlertDialog = mBuilder.show()
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtDescripcion = mDialogView.findViewById<TextView>(R.id.txtRetoAEliminar)
        val btnSi = mDialogView.findViewById<Button>(R.id.btnSiEliminar)
        val btnNo = mDialogView.findViewById<Button>(R.id.btnNoEliminar)

        txtDescripcion.text = reto.descripcion

        btnNo.setOnClickListener {
            mAlertDialog.dismiss()
        }

        btnSi.setOnClickListener {
            db.borrarReto(reto.id)
            actualizarLista()
            mAlertDialog.dismiss()
            Toast.makeText(this, "Reto eliminado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarLista() {
        val listaRetos = db.obtenerRetos()
        val adapter = RetoAdapter(this, listaRetos)
        listView.adapter = adapter
    }
}