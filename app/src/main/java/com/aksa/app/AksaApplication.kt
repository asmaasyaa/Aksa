package com.aksa.app

import android.app.Application
import com.aksa.app.data.repository.AksaRepositoryProvider

class AksaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AksaRepositoryProvider.initialize(this)
    }
}
