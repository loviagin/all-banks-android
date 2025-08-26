package com.lovigin.android.allbanks.di


import android.content.Context
import com.lovigin.android.allbanks.data.local.AccountDao
import com.lovigin.android.allbanks.data.local.BankDao
import com.lovigin.android.allbanks.data.local.CategoryDao
import com.lovigin.android.allbanks.data.local.LoanDao
import com.lovigin.android.allbanks.data.local.TransactionDao
import com.lovigin.android.allbanks.data.local.UserDao
import com.lovigin.android.allbanks.data.repo.AccountRepository
import com.lovigin.android.allbanks.data.repo.BankRepository
import com.lovigin.android.allbanks.data.repo.CategoryRepository
import com.lovigin.android.allbanks.data.repo.LoanRepository
import com.lovigin.android.allbanks.data.repo.TransactionRepository
import com.lovigin.android.allbanks.data.repo.UserRepository
import com.lovigin.android.allbanks.store.DonationStore // ОСТАВЛЯЕМ только этот импорт!

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton
    fun provideUserRepository(dao: UserDao): UserRepository = UserRepository(dao)

    @Provides @Singleton
    fun provideBankRepository(dao: BankDao): BankRepository = BankRepository(dao)

    @Provides @Singleton
    fun provideAccountRepository(dao: AccountDao): AccountRepository = AccountRepository(dao)

    @Provides @Singleton
    fun provideCategoryRepository(dao: CategoryDao): CategoryRepository = CategoryRepository(dao)

    @Provides @Singleton
    fun provideLoanRepository(dao: LoanDao): LoanRepository = LoanRepository(dao)

    @Provides @Singleton
    fun provideTransactionRepository(dao: TransactionDao): TransactionRepository = TransactionRepository(dao)

    // SettingsViewModel использует это; тут НУЖЕН КЛАСС из пакета store, а не интерфейс из viewmodels
    @Provides @Singleton
    fun provideDonationStore(@ApplicationContext context: Context): DonationStore = DonationStore(context)
}