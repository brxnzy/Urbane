package com.example.urbane.data.repository

import android.R.attr.name
import android.provider.SyncStateContract
import android.util.Log
import com.example.urbane.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class ResidentialRepository {

    suspend fun getResidentialId(name: String): Int {
        return try{
        val result = supabase.from("residentials")
            .select(columns = Columns.list("id")){
                filter {
                    eq("residentialName",name)
                }
            }.decodeSingle<Int>()

         result
        } catch (e: Exception){
            Log.e("Error obteniendo el id del residencial", e.toString())

        }


    }
}