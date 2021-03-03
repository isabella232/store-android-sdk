package com.xsolla.android.storesdkexample

import android.app.Application
import android.content.Context
import com.xsolla.android.login.LoginConfig
import com.xsolla.android.login.XLogin

class App: Application() {

    companion object {
        private var instance: App? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.USE_OAUTH) {
            val loginConfig = LoginConfig.OauthBuilder()
                    .setProjectId(BuildConfig.LOGIN_ID)
                    .setOauthClientId(BuildConfig.OAUTH_CLIENT_ID)
                    .setSocialConfig(XLogin.SocialConfig(facebookAppId = BuildConfig.FACEBOOK_CREDENTIAL,
                            googleServerId = BuildConfig.GOOGLE_CREDENTIAL))
                .build()

            XLogin.init(this, loginConfig)
        } else {
            val loginConfig = LoginConfig.JwtBuilder()
                    .setProjectId(BuildConfig.LOGIN_ID)
                    .setSocialConfig(XLogin.SocialConfig(facebookAppId = BuildConfig.FACEBOOK_CREDENTIAL,
                            googleServerId = BuildConfig.GOOGLE_CREDENTIAL))
                .build()

            XLogin.init(this, loginConfig)
        }
    }
}