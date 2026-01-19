package com.example.urbane.utils
import androidx.annotation.StringRes
import com.example.urbane.R
@StringRes
fun getTipoPropiedadLabelRes(tipo: String?): Int {
    return when (tipo) {
        "Casa" -> R.string.casa
        "Apartamento" -> R.string.apartamento
        "Villa" -> R.string.villa
        "Terreno" -> R.string.terreno
        "Local" -> R.string.local
        else -> R.string.sin_tipo
    }
}