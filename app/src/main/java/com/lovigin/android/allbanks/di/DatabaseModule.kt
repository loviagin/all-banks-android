package com.lovigin.android.allbanks.di


import android.content.Context
import androidx.room.Room
import com.lovigin.android.allbanks.data.local.AllBanksDatabase
import com.lovigin.android.allbanks.data.local.AccountDao
import com.lovigin.android.allbanks.data.local.BankDao
import com.lovigin.android.allbanks.data.local.CategoryDao
import com.lovigin.android.allbanks.data.local.LoanDao
import com.lovigin.android.allbanks.data.local.TransactionDao
import com.lovigin.android.allbanks.data.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AllBanksDatabase =
        Room.databaseBuilder(context, AllBanksDatabase::class.java, "allbanks.db")
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()

    @Provides fun provideUserDao(db: AllBanksDatabase): UserDao = db.userDao()
    @Provides fun provideBankDao(db: AllBanksDatabase): BankDao = db.bankDao()
    @Provides fun provideAccountDao(db: AllBanksDatabase): AccountDao = db.accountDao()
    @Provides fun provideCategoryDao(db: AllBanksDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideLoanDao(db: AllBanksDatabase): LoanDao = db.loanDao()
    @Provides fun provideTransactionDao(db: AllBanksDatabase): TransactionDao = db.transactionDao()
}