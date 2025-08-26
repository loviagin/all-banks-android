package com.lovigin.android.allbanks.data.repo

import com.lovigin.android.allbanks.data.local.BankDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankRepository @Inject constructor(
    val dao: BankDao
)