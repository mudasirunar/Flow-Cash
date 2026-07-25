package com.mudasir.flowcash.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mudasir.flowcash.data.local.dao.AccountDao
import com.mudasir.flowcash.data.local.dao.BudgetDao
import com.mudasir.flowcash.data.local.dao.TransactionDao
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.data.local.entity.BudgetEntity
import com.mudasir.flowcash.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, BudgetEntity::class, AccountEntity::class],
    version = 5,
    exportSchema = false
)
abstract class FlowCashDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: FlowCashDatabase? = null

        fun getDatabase(context: Context): FlowCashDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlowCashDatabase::class.java,
                    "flowcash_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    // Seed default budget limits
                                    database.budgetDao().insertBudget(BudgetEntity("Food", 300.0))
                                    database.budgetDao().insertBudget(BudgetEntity("Shopping", 200.0))
                                    database.budgetDao().insertBudget(BudgetEntity("Bills", 500.0))
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
