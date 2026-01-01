package com.example.urbane.data.repository
import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Expense
import com.example.urbane.data.model.UserMinimal
import com.example.urbane.data.remote.supabase
import com.example.urbane.data.model.Transaction
import com.example.urbane.ui.admin.finances.model.TransactionType
import com.example.urbane.utils.getCurrentUserId
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlin.collections.filter
import kotlin.collections.firstOrNull

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

    suspend fun getTransactionsByDateRange(
        startDate: String,
        endDate: String
    ): List<Transaction> {
        return try {
            val transactions = mutableListOf<Transaction>()

            val ingresos = getIngresosByDateRange(startDate, endDate)
            transactions.addAll(ingresos)

            // Obtener EGRESOS (expenses)
            val egresos = getEgresosByDateRange(startDate, endDate)
            transactions.addAll(egresos)

            // Ordenar por fecha descendente
            transactions.sortedByDescending { it.date }

        } catch (e: Exception) {
            Log.e("FinancesRepository", "Error obteniendo transacciones: $e")
            emptyList()
        }
    }


    private suspend fun getIngresosByDateRange(
        startDate: String,
        endDate: String
    ): List<Transaction> {
        return try {
            val residentialId = getResidentialId(sessionManager)

            val payments = supabase.from("payments")
                .select(
                    columns = Columns.list(
                        "id",
                        "createdAt",
                        "transactions:payments_transactions(id,amount,date:createdAt)",
                        "resident:users(id,name,photoUrl)"
                    )
                ) {
                    filter {
                        eq("residentialId", residentialId!!)
                        gte("createdAt", startDate)
                        lt("createdAt", endDate)
                        neq("createdAt", "null")
                        neq("status", "Pendiente")
                    }
                }.decodeList<PaymentResponse>()
            print(payments)
            payments.mapNotNull { payment ->
                try {
                    Transaction(
                        id = payment.id,
                        type = TransactionType.INGRESO,
                        amount = payment.transactions.firstOrNull()?.amount ?: 0.0,
                        description = "Pago de cuota - ${payment.resident?.name ?: "Residente"}",
                        date = payment.transactions.firstOrNull()?.date ?: "",
                        createdBy = payment.resident?.id ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("FinancesRepository", "Error parseando ingreso: ${payment.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FinancesRepository", "Error obteniendo ingresos: $e")
            emptyList()
        }
    }
    private suspend fun getEgresosByDateRange(
        startDate: String,
        endDate: String
    ): List<Transaction> {
        return try {
            val residentialId = getResidentialId(sessionManager)

            val expenses = supabase.from("expenses")
                .select(
                    columns = Columns.list(
                        "id",
                        "amount",
                        "description",
                        "createdAt",
                        "adminId",
                        "residentialId",
                        "admin:users(id,name, photoUrl)"
                    )
                ) {
                    filter {
                        eq("residentialId", residentialId!!)
                        gte("createdAt", startDate)
                        lt("createdAt", endDate)
                    }
                }.decodeList<Expense>()

            expenses.mapNotNull { expense ->
                try {
                    Transaction(
                        id = expense.id!!,
                        type = TransactionType.EGRESO,
                        amount = expense.amount,
                        description = expense.description,
                        date = expense.createdAt!!,
                        createdBy = expense.admin?.id ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("FinancesRepository", "Error parseando egreso: ${expense.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FinancesRepository", "Error obteniendo egresos: $e")
            emptyList()
        }
    }

    suspend fun getFinancialSummary(
        startDate: String,
        endDate: String
    ): FinancialSummary {
        return try {
            val transactions = getTransactionsByDateRange(startDate, endDate)

            val totalIngresos = transactions
                .filter { it.type == TransactionType.INGRESO }
                .sumOf { it.amount }

            val totalEgresos = transactions
                .filter { it.type == TransactionType.EGRESO }
                .sumOf { it.amount }

            FinancialSummary(
                totalIngresos = totalIngresos,
                totalEgresos = totalEgresos,
                balance = totalIngresos - totalEgresos,
                cantidadIngresos = transactions.count { it.type == TransactionType.INGRESO },
                cantidadEgresos = transactions.count { it.type == TransactionType.EGRESO }
            )
        } catch (e: Exception) {
            Log.e("FinancesRepository", "Error obteniendo resumen financiero: $e")
            FinancialSummary()
        }
    }
}

data class FinancialSummary(
    val totalIngresos: Double = 0.0,
    val totalEgresos: Double = 0.0,
    val balance: Double = 0.0,
    val cantidadIngresos: Int = 0,
    val cantidadEgresos: Int = 0
)

@Serializable
data class PaymentResponse(
    val id: Int,
    val resident: UserMinimal? = null,
    val createdAt: String,
    val transactions: List<Transaction> = emptyList()
)