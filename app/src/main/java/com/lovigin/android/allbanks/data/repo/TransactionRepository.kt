package com.lovigin.android.allbanks.data.repo

import com.lovigin.android.allbanks.data.local.TransactionDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    val dao: TransactionDao
)