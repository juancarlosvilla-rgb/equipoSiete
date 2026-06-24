package com.univalle.picobotella

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView

class RetoAdapter(val context: Context, var lista: MutableList<RetoModel>) : BaseAdapter() {

    override fun getCount(): Int = lista.size
    override fun getItem(p0: Int): RetoModel = lista[p0]
    override fun getItemId(p0: Int): Long = p0.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_reto, parent, false)

        val reto = lista[position]
        val txtDesc = view.findViewById<TextView>(R.id.itemTxtDescripcion)
        val btnEdit = view.findViewById<ImageButton>(R.id.itemBtnEdit)
        val btnDelete = view.findViewById<ImageButton>(R.id.itemBtnDelete)

        // Bindeamos la descripción del reto que viene de Firestore
        txtDesc.text = reto.descripcion

        // HU 4.0 Criterio 8: Animación de touch antes de editar
        btnEdit.setOnClickListener {
            animarToque(it) {
                // Llamamos a la función de la actividad para abrir el diálogo de edición
                (context as RetosActivity).mostrarDialogoEditar(reto)
            }
        }

        // HU 4.0 Criterio 8: Animación de touch antes de eliminar
        btnDelete.setOnClickListener {
            animarToque(it) {
                // Llamamos a la función de la actividad para abrir el diálogo de confirmación de borrado
                (context as RetosActivity).mostrarDialogoEliminar(reto)
            }
        }

        return view
    }

    // Criterio de animación sutil para todos los botones de la lista
    private fun animarToque(v: View, fin: () -> Unit) {
        v.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction {
                v.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .withEndAction {
                        fin()
                    }
                    .start()
            }
            .start()
    }
}