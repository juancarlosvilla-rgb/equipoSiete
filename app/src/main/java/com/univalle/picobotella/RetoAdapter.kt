package com.univalle.picobotella

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

class RetoAdapter(val context: Context, var lista: MutableList<RetoModel>) : BaseAdapter() {

    override fun getCount(): Int = lista.size
    override fun getItem(p0: Int) = lista[p0]
    override fun getItemId(p0: Int) = p0.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_reto, parent, false)

        val reto = lista[position]
        val txtDesc = view.findViewById<TextView>(R.id.itemTxtDescripcion)
        val btnEdit = view.findViewById<ImageButton>(R.id.itemBtnEdit)
        val btnDelete = view.findViewById<ImageButton>(R.id.itemBtnDelete)

        txtDesc.text = reto.descripcion

        // Animación de toque Criterio 7 y acciones
        // Dentro de getView en RetoAdapter.kt
        btnEdit.setOnClickListener {
            animarToque(it) {
                // Llamamos a la función de la actividad
                (context as RetosActivity).mostrarDialogoEditar(reto)
            }
        }

        btnDelete.setOnClickListener {
            animarToque(it) {
                Toast.makeText(context, "HU 9.0: Eliminar ID ${reto.id}", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun animarToque(v: View, fin: () -> Unit) {
        v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction {
            v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).withEndAction { fin() }.start()
        }.start()
    }
}