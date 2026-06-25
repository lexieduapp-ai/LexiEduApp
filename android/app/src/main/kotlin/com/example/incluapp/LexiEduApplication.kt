package com.example.incluapp

import android.app.Application

class LexiEduApplication : Application() {

    val container: LexiEduContainer by lazy { LexiEduContainer(this) }
}
