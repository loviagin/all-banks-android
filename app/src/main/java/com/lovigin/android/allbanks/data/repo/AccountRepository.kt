package com.lovigin.android.allbanks.data.repo

import com.lovigin.android.allbanks.data.local.AccountDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    val dao: AccountDao
)