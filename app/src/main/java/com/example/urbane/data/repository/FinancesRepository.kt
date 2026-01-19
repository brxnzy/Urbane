package com.example.urbane.data.repository

import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Expense
import com.example.urbane.data.model.Transaction
import com.example.urbane.data.model.UserMinimal
import com.example.urbane.data.remote.supabase
import com.example.urbane.ui.admin.finances.model.TransactionType
import com.example.urbane.utils.getCurrentUserId
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FinancesRepository(private val sessionManager: SessionManager) {

    suspend fun registerExpense(amount: Double, description: String) {
        try {
            val adminId = getCurrentUserId(sessionManager)
            val residentialId = getResidentialId(sessionManager)
            val data = Expense(
                adminId = adminId,
                amount = amount,
                description = description,
                residentialId = residentialId!!
            )
            supabase.from("expenses").insert(data)
        } catch (e: Exception) {
            Log.e("FinancesRepository", "Error creando egreso: $e")
        }
    }

    suspend fun getAllExpenses(): List<Expense> {
        return try {
            val residentialId = getResidentialId(sessionManager)
            supabase.from("expenses").select(
                columns = Columns.list(
                    "id",
                    "amount",
                    "description",
                    "createdAt",
                    "adminId",
                    "residentialId",
                    "admin:users(id,name,photoUrl)"
                )
            ) {
                filter {
                    eq("residentialId", residentialId!!)
                }
            }.decodeList<Expense>()

        } catch (e: Exception) {
            Log.e("FinancesRepository", "Error obteniendo egresos: $e")
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

            val ingresos = getIncomesByDateRange(startDate, endDate)
            transactions.addAll(ingresos)

            // Obtener EGRESOS (expenses)
            val egresos = getExpensesByDateRange(startDate, endDate)
            transactions.addAll(egresos)

            // Ordenar por fecha descendente
            transactions.sortedByDescending { it.date }

        } catch (e: Exception) {
            Log.e("FinancesRepository", "Error obteniendo transacciones: $e")
            emptyList()
        }
    }

    suspend fun getIncomesByMonth(): Double {
        return try {
            val residentialId = getResidentialId(sessionManager)
            Log.d("FinancesRepository", "ResidentialId: $residentialId")

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startDate = calendar.time
            calendar.add(Calendar.MONTH, 1)
            val endDate = calendar.time

            val transactions = supabase
                .from("payments_transactions")
                .select(
                    columns = Columns.list(
                        "id",
                        "amount",
                        "date:createdAt"
                    )
                ) {
                    filter {
                        eq("residentialId", residentialId!!)
                        gte("createdAt", startDate)
                        lt("createdAt", endDate)
                    }
                }
                .decodeList<Transaction>()

            Log.d(
                "FinancesRepository",
                "Transacciones mes actual: ${transactions.size}"
            )

            val total = transactions.sumOf { it.amount ?: 0.0 }

            Log.d(
                "FinancesRepository",
                "Total ingresos mes actual: $total"
            )

            total

        } catch (e: Exception) {
            Log.e(
                "FinancesRepository",
                "Error obteniendo ingresos del mes actual",
                e
            )
            0.0
        }
    }


    suspend fun getExpensesByMonth(): Double {
        return try {
            val residentialId = getResidentialId(sessionManager)

            // Obtener el mes y año actual
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) // 0-11 (Enero = 0)

            // Configurar fecha de inicio (primer día del mes actual a las 00:00:00)
            calendar.set(year, currentMonth, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(calendar.time)

            // Configurar fecha de fin (primer día del siguiente mes a las 00:00:00)
            calendar.add(Calendar.MONTH, 1)
            val endDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(calendar.time)

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

            Log.d("FinancesRepository", "Egresos obtenidos para mes actual: ${expenses.size}")

            // Sumar todos los montos de los egresos
            val totalEgresos = expenses.sumOf { expense ->
                expense.amount
            }

            Log.d("FinancesRepository", "Total egresos mes actual: $totalEgresos")

            totalEgresos

        } catch (e: Exception) {
            Log.e("FinancesRepository", "Error obteniendo egresos del mes actual: $e")
            0.0 // Retornar 0 en caso de error
        }
    }


    private suspend fun getIncomesByDateRange(
        startDate: String,
        endDate: String
    ): List<Transaction> {
        return try {
            val residentialId = getResidentialId(sessionManager)

            val transactions = supabase
                .from("payments_transactions")
                .select(
                    columns = Columns.list(
                        "id",
                        "amount",
                        "createdAt",
                        "payment:payments(id,resident:users(id,name))"
                    )
                ) {
                    filter {
                        eq("residentialId", residentialId!!)
                        gte("createdAt", startDate)
                        lt("createdAt", endDate)
                    }
                }
                .decodeList<TransactionResponse>()

            transactions.mapNotNull { tx ->
                try {
                    Transaction(
                        id = tx.id,
                        type = TransactionType.INGRESO,
                        amount = tx.amount ?: 0.0,
                        description = "Pago de cuota - ${tx.payment?.resident?.name ?: "Residente"}",
                        date = tx.createdAt,
                        createdBy = tx.payment?.resident?.id ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("FinancesRepository", "Error parseando transacción ${tx.id}", e)
                    null
                }
            }

        } catch (e: Exception) {
            Log.e("FinancesRepository", "Error obteniendo ingresos", e)
            emptyList()
        }
    }

    private suspend fun getExpensesByDateRange(
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

@Serializable
data class TransactionResponse(
    val id: Int,
    val amount: Double?,
    val createdAt: String,
    val payment: PaymentLite?
)

@Serializable
data class PaymentLite(
    val id: Long,
    val resident: ResidentLite?
)

@Serializable
data class ResidentLite(
    val id: String,
    val name: String
)
