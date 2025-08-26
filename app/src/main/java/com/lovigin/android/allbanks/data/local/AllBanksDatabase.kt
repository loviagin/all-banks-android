package com.lovigin.android.allbanks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lovigin.android.allbanks.models.*

@Database(
    entities = [User::class, Account::class, Bank::class, Loan::class, Transaction::class, Category::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AllBanksDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bankDao(): BankDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun loanDao(): LoanDao
    abstract fun transactionDao(): TransactionDao
}