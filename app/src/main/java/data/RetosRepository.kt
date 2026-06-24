package com.univalle.picobotella.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.univalle.picobotella.RetoModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RetosRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth // Auth para saber quién es el usuario actual
) {
    // Función para obtener la carpeta privada del usuario actual
    private fun getUserCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("usuarios").document(uid).collection("mis_retos")
    }

    suspend fun getRetos(): List<RetoModel> {
        val collection = getUserCollection() ?: return emptyList()
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.map { doc ->
                RetoModel(id = doc.id, descripcion = doc.getString("descripcion") ?: "")
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addReto(descripcion: String) {
        val collection = getUserCollection() ?: return
        val nuevoReto = mapOf("descripcion" to descripcion)
        collection.add(nuevoReto).await()
    }

    suspend fun updateReto(id: String, nuevaDescripcion: String) {
        val collection = getUserCollection() ?: return
        collection.document(id).update("descripcion", nuevaDescripcion).await()
    }

    suspend fun deleteReto(id: String) {
        val collection = getUserCollection() ?: return
        collection.document(id).delete().await()
    }
}