package com.lovigin.android.allbanks.data.repository

import com.lovigin.android.allbanks.data.local.dao.UserDao
import com.lovigin.android.allbanks.data.local.entity.UserEntity


class UserRepository(private val userDao: UserDao) {
    suspend fun getOrCreateUser(): UserEntity {
        val existing = userDao.getFirstOrNull()
        if (existing != null) return existing
        val newUser = UserEntity(name = "", email = "")
        userDao.insert(newUser)
        return newUser
    }
}