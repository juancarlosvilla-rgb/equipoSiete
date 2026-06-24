package com.univalle.picobotella

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "PicoBotella.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE retos (id INTEGER PRIMARY KEY AUTOINCREMENT, descripcion TEXT)")
        // Retos iniciales
        db?.execSQL("INSERT INTO retos (descripcion) VALUES ('Canta una canción')")
        db?.execSQL("INSERT INTO retos (descripcion) VALUES ('Haz 10 flexiones')")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS retos")
        onCreate(db)
    }

    // Guardar (HU 7.0)
    fun agregarReto(texto: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put("descripcion", texto) }
        db.insert("retos", null, values)
        db.close()
    }

    // Editar (HU 8.0)
    fun editarReto(id: Int, nuevoTexto: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put("descripcion", nuevoTexto) }
        db.update("retos", values, "id=?", arrayOf(id.toString()))
        db.close()
    }

    // Borrar (HU 9.0)
    fun borrarReto(id: Int) {
        val db = this.writableDatabase
        db.delete("retos", "id=?", arrayOf(id.toString()))
        db.close()
    }

    // Listar (HU 6.0) - El más reciente arriba
    fun obtenerRetos(): MutableList<RetoModel> {
        val lista = mutableListOf<RetoModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM retos ORDER BY id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(RetoModel(cursor.getInt(0), cursor.getString(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun obtenerRetoAleatorio(): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT descripcion FROM retos ORDER BY RANDOM() LIMIT 1", null)
        var reto = "¡No hay retos guardados!"
        if (cursor.moveToFirst()) {
            reto = cursor.getString(0)
        }
        cursor.close()
        return reto
    }
}

// Modelo de datos simple
data class RetoModel(val id: Int, val descripcion: String)