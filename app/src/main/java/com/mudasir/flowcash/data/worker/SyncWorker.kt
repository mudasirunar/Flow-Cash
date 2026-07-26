package com.mudasir.flowcash.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.mudasir.flowcash.data.local.FlowCashDatabase
import com.mudasir.flowcash.data.remote.RemoteSyncManager

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val fbUser = FirebaseAuth.getInstance().currentUser ?: return Result.success()
        val db = FlowCashDatabase.getDatabase(applicationContext)
        val syncManager = RemoteSyncManager(db.transactionDao(), db.accountDao(), db.budgetDao())

        return try {
            syncManager.uploadUnsyncedData(fbUser.uid)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
