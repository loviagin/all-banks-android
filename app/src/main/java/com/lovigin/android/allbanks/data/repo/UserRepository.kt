package com.lovigin.android.allbanks.data.repo

import com.lovigin.android.allbanks.data.local.UserDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    val dao: UserDao
)