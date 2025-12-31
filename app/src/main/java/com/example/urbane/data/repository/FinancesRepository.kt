package com.example.urbane.data.repository
import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Expense
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getCurrentUserId
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class FinancesRepository (private val sessionManager: SessionManager){

    suspend fun registerExpense(amount:Double,description: String){
        try {
            val adminId = getCurrentUserId(sessionManager)
            val residentialId = getResidentialId(sessionManager)
            val data = Expense(adminId = adminId, amount = amount, description = description, residentialId = residentialId!!)
            supabase.from("expenses").insert(data)
        }catch (e: Exception){
            Log.e("FinancesRepository","Error creando egreso: $e")
        }
    }

    suspend fun getAllExpenses(): List<Expense>{
        return try {
            val residentialId = getResidentialId(sessionManager)
            supabase.from("expenses").select(columns = Columns.list(
                "id",
                "amount",
                "description",
                "createdAt",
                "adminId",
                "residentialId",
                "admin:users(id,name,photoUrl)"
            )) {
                filter {
                    eq("residentialId", residentialId!!)
                }
            }.decodeList<Expense>()

        }catch (e: Exception){
            Log.e("FinancesRepository","Error obteniendo egresos: $e")
            throw e
        }

    }


    suspend fun getBalance(): Double {
        return try {
            val residentialId = getResidentialId(sessionManager)

            val ingresos = supabase.from("payments")
                .select(columns = Columns.list("paidAmount")) {
                    filter {
                        eq("residentialId", residentialId!!)

                    }
                }.decodeList<Map<String, Double>>()
                .sumOf { it["paidAmount"] ?: 0.0 }

            print(ingresos)

            val egresos = supabase.from("expenses")
                .select(columns = Columns.list("amount")) {
                    filter {
                        eq("residentialId", residentialId!!)
                    }
                }.decodeList<Map<String, Double>>()
                .sumOf { it["amount"] ?: 0.0 }

            print(egresos)


            ingresos - egresos
        } catch (e: Exception) {
            Log.e("FinancesRepository", "Error obteniendo balance: $e")
            0.0
        }
    }
}