package com.example.williamrecipeapp.model

import android.content.Context
import android.content.SharedPreferences
import com.example.williamrecipeapp.R
import java.util.*

class UserSetting(private val context: Context) {

    private var pref = context.getSharedPreferences("UserSetting", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor

    init {
        editor = pref.edit()
        editor.apply()
    }

    fun login(userId: String) {
        editor.putString("userID", userId)
        editor.commit()
    }

    fun logout() {
        editor.putString("userID", "")
        editor.commit()
    }

    fun isLoggedIn(): Boolean {
        val userId = pref.getString("userID", "")
        return !userId.isNullOrBlank()
    }

    fun getUserId(): String? {
        return pref.getString("userID", "")
    }

}