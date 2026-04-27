package com.example.cuidalink.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.model.Contact
import com.example.cuidalink.network.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    data class GameUiState(
        val currentContact: Contact? = null,
        val options: List<String> = emptyList(),
        val score: Int = 0,
        val isGameOver: Boolean = false,
        val isLoading: Boolean = false,
        val message: String? = null,
        val allContacts: List<Contact> = emptyList()
    )

    private val _gameState = MutableStateFlow(GameUiState(isLoading = true))
    val gameState: StateFlow<GameUiState> = _gameState.asStateFlow()

    private var availableContacts = listOf<Contact>()

    fun fetchContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            _gameState.value = _gameState.value.copy(isLoading = true)
            val contacts = mutableListOf<Contact>()
            
            val cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                    ContactsContract.Contacts.PHOTO_URI
                ),
                null,
                null,
                null
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val photoIndex = it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

                while (it.moveToNext()) {
                    val id = it.getString(idIndex)
                    val name = it.getString(nameIndex)
                    val photoUriString = it.getString(photoIndex)
                    val photoUri = if (photoUriString != null) Uri.parse(photoUriString) else null
                    
                    if (name != null) {
                        contacts.add(Contact(id, name, photoUri))
                    }
                }
            }

            // Sincronizar con Supabase Storage
            val syncedContacts = syncWithSupabase(contacts)
            
            val shuffledWithPhoto = syncedContacts.filter { it.photoUri != null }.shuffled()
            availableContacts = shuffledWithPhoto
            
            _gameState.value = _gameState.value.copy(
                isLoading = false, 
                score = 0, 
                isGameOver = false,
                allContacts = syncedContacts.sortedBy { it.name }
            )
            nextQuestion()
        }
    }

    private suspend fun syncWithSupabase(localContacts: List<Contact>): List<Contact> {
        return try {
            val user = SupabaseConfig.client.auth.currentUserOrNull() ?: return localContacts
            val uid = user.id
            val bucket = SupabaseConfig.client.storage.from("contacts")
            
            // Listar archivos en la carpeta del usuario
            val remoteFiles = bucket.list(uid)
            
            localContacts.map { contact ->
                val remoteFileName = "contact_${contact.id}.jpg"
                val hasRemotePhoto = remoteFiles.any { it.name == remoteFileName }
                
                if (hasRemotePhoto) {
                    val publicUrl = bucket.publicUrl("$uid/$remoteFileName")
                    contact.copy(photoUri = Uri.parse(publicUrl))
                } else {
                    contact
                }
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "Error sincronizando con Supabase: ${e.message}")
            localContacts
        }
    }

    fun checkGuess(guess: String) {
        val currentState = _gameState.value
        val contactName = currentState.currentContact?.name ?: ""
        val isCorrect = guess == contactName
        
        if (isCorrect) {
            _gameState.value = currentState.copy(
                score = currentState.score + 1,
                message = "¡Correcto!"
            )
            nextQuestion()
        } else {
            _gameState.value = currentState.copy(
                message = "¡Incorrecto! Era ${contactName}"
            )
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        if (availableContacts.isNotEmpty()) {
            val nextContact = availableContacts.first()
            availableContacts = availableContacts.drop(1)
            
            val otherNames = _gameState.value.allContacts
                .filter { it.name != nextContact.name }
                .map { it.name }
                .distinct()
                .shuffled()
                .take(3)
            
            val options = (otherNames + nextContact.name).shuffled()

            _gameState.value = _gameState.value.copy(
                currentContact = nextContact,
                options = options,
                isLoading = false
            )
        } else {
            _gameState.value = _gameState.value.copy(
                isGameOver = true,
                currentContact = null,
                message = if (_gameState.value.score > 0) "¡Juego terminado!" else "No hay contactos con foto disponibles"
            )
        }
    }

    fun updateContactPhoto(contactId: String, newPhotoUri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            try {
                _gameState.value = _gameState.value.copy(isLoading = true)
                
                val user = SupabaseConfig.client.auth.currentUserOrNull() ?: throw Exception("Usuario no autenticado")
                val uid = user.id

                val inputStream = contentResolver.openInputStream(newPhotoUri)
                val bytes = inputStream?.readBytes() ?: throw Exception("No se pudo leer la imagen")
                inputStream.close()

                val fileName = "$uid/contact_$contactId.jpg"
                val bucket = SupabaseConfig.client.storage.from("contacts")
                
                bucket.upload(fileName, bytes) {
                    upsert = true
                }

                val publicUrl = bucket.publicUrl(fileName)
                val remoteUri = Uri.parse(publicUrl)

                val updatedContacts = _gameState.value.allContacts.map {
                    if (it.id == contactId) it.copy(photoUri = remoteUri) else it
                }
                
                // Actualizar también la lista de disponibles para el juego
                availableContacts = updatedContacts.filter { it.photoUri != null }.shuffled()

                _gameState.value = _gameState.value.copy(
                    allContacts = updatedContacts.sortedBy { it.name },
                    isLoading = false,
                    message = "Imagen actualizada en la nube"
                )
            } catch (e: Exception) {
                _gameState.value = _gameState.value.copy(
                    isLoading = false,
                    message = "Error al subir: ${e.message}"
                )
            }
        }
    }
}
