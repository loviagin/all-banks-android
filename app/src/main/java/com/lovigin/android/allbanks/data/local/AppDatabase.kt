package com.lovigin.android.allbanks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lovigin.android.allbanks.data.local.dao.AccountDao
import com.lovigin.android.allbanks.data.local.dao.BankDao
import com.lovigin.android.allbanks.data.local.dao.CategoryDao
import com.lovigin.android.allbanks.data.local.dao.LoanDao
import com.lovigin.android.allbanks.data.local.dao.TransactionDao
import com.lovigin.android.allbanks.data.local.dao.UserDao
import com.lovigin.android.allbanks.data.local.entity.AccountEntity
import com.lovigin.android.allbanks.data.local.entity.BankEntity
import com.lovigin.android.allbanks.data.local.entity.CategoryEntity
import com.lovigin.android.allbanks.data.local.entity.LoanEntity
import com.lovigin.android.allbanks.data.local.entity.TransactionEntity
import com.lovigin.android.allbanks.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        BankEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        LoanEntity::class,
        TransactionEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bankDao(): BankDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun loanDao(): LoanDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "allbanks.db"
            )
                .fallbackToDestructiveMigration(true)
                .build()
                .also { INSTANCE = it }
        }
    }
}