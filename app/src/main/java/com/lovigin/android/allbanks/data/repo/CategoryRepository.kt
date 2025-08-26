package com.lovigin.android.allbanks.data.repo

import com.lovigin.android.allbanks.data.local.CategoryDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    val dao: CategoryDao
)