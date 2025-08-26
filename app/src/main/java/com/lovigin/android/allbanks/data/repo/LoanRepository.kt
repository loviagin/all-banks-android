package com.lovigin.android.allbanks.data.repo

import com.lovigin.android.allbanks.data.local.LoanDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    val dao: LoanDao
)