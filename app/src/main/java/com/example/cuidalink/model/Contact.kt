package com.example.cuidalink.model

import android.net.Uri

data class Contact(
    val id: String,
    val name: String,
    val photoUri: Uri? = null
)
