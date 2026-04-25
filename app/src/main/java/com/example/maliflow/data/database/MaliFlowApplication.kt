package com.example.maliflow.data.database

import android.app.Application

class MaliFlowApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
}