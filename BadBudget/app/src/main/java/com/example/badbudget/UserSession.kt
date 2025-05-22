package com.example.badbudget

import android.content.Context

object UserSession {

    private const val KEY_USER = "logged_user"

    fun init(context: Context, userId: String) =
        context.getSharedPreferences("bb_prefs", 0)
            .edit().putString(KEY_USER, userId).apply()

    fun id(context: Context): String =
        context.getSharedPreferences("bb_prefs", 0)
            .getString(KEY_USER, "") ?: ""
}
