package com.finmojarra.app

import android.app.Application
import com.finmojarra.app.di.AppContainer

class FinMojarraApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
