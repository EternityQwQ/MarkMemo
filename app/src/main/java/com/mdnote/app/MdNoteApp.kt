package com.mdnote.app

import android.app.Application
import com.mdnote.app.data.db.NoteDatabase

class MdNoteApp : Application() {

    val database: NoteDatabase by lazy {
        NoteDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}