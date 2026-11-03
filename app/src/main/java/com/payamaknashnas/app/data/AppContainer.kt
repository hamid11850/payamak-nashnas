package com.payamaknashnas.app.data

import android.content.Context

/**
 * نقطه دسترسی سراسری به لایه داده.
 * اگر فایل google-services.json واقعی در پروژه قرار گرفته باشد، لایه Firebase
 * فعال می‌شود؛ در غیر این صورت برنامه در «حالت نمایشی» با داده محلی اجرا می‌شود
 * تا ظاهر و جریان صفحات بدون Firebase هم قابل بررسی باشد.
 */
object AppContainer {

    @Volatile private var initialized = false

    lateinit var repo: Repo
        private set
    lateinit var auth: AuthApi
        private set
    var demoMode: Boolean = false
        private set

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val firebaseReady = isFirebaseConfigured(context)
            demoMode = !firebaseReady
            if (firebaseReady) {
                repo = FirestoreRepo()
                auth = FirebaseAuthApi()
            } else {
                val demo = DemoRepo()
                repo = demo
                auth = DemoAuthApi(demo)
            }
            initialized = true
        }
    }

    private fun isFirebaseConfigured(context: Context): Boolean {
        val id = context.resources.getIdentifier("google_app_id", "string", context.packageName)
        return id != 0
    }
}
