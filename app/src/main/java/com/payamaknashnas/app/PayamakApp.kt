package com.payamaknashnas.app

import android.app.Application
import com.payamaknashnas.app.data.AppContainer

class PayamakApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
