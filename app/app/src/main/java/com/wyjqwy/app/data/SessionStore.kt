package com.wyjqwy.app.data

import android.content.Context

class SessionStore(context: Context) {
    private val sp = context.getSharedPreferences("wyjqwy_session", Context.MODE_PRIVATE)

    fun getAccessToken(): String? = sp.getString("access_token", null)
    fun getRefreshToken(): String? = sp.getString("refresh_token", null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        sp.edit().putString("access_token", accessToken).putString("refresh_token", refreshToken).apply()
    }

    fun clear() {
        sp.edit().clear().apply()
    }
}
