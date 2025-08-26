package com.lovigin.android.allbanks.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*
import com.lovigin.android.allbanks.models.*
import com.lovigin.android.allbanks.models.Transaction

/** USER */
@Dao
interface UserDao {

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getOne(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

/** BANK */
@Dao
interface BankDao {
    @Query("SELECT * FROM banks WHERE isArchived = 0 ORDER BY name")
    fun observeAll(): Flow<List<Bank>>
    @Query("SELECT * FROM banks ORDER BY name")
    suspend fun getAll(): List<Bank>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bank: Bank)
    @Delete suspend fun delete(bank: Bank)
}

/** ACCOUNT */
@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY name")
    fun observeAll(): Flow<List<Account>>
    @Query("SELECT * FROM accounts WHERE bankId = :bankId AND isArchived = 0")
    fun observeByBank(bankId: UUID): Flow<List<Account>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(account: Account)
    @Delete suspend fun delete(account: Account)
}

/** CATEGORY */
@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    fun observeAll(): Flow<List<Category>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: Category)
    @Delete suspend fun delete(category: Category)
}

/** LOAN */
@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY name")
    fun observeAll(): Flow<List<Loan>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(loan: Loan)
    @Delete suspend fun delete(loan: Loan)
}

/** TRANSACTION */
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun observeAll(): Flow<List<Transaction>>
    @Query("SELECT * FROM transactions WHERE account = :accountId ORDER BY date DESC")
    fun observeByAccount(accountId: UUID): Flow<List<Transaction>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tx: Transaction)
    @Delete suspend fun delete(tx: Transaction)
}