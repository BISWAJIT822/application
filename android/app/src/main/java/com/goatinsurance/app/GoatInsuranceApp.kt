package com.goatinsurance.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GoatInsuranceApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
