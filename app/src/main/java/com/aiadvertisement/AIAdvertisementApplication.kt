package com.aiadvertisement

import android.app.Application
import com.aiadvertisement.core.storage.MMKVHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AIAdvertisementApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MMKVHelper.init(this)
    }
}
