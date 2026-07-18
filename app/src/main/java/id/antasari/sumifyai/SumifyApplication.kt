package id.antasari.sumifyai

import android.app.Application

class SumifyApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
