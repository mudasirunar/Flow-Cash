package com.mudasir.flowcash.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mudasir.flowcash.data.local.dao.TransactionDao
import com.mudasir.flowcash.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FlowCashDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

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
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
