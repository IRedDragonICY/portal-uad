package com.uad.portal

data class Session(
    var session: String? = null,
    var userInfo: UserInfo? = null,
    var credentials: Credentials? = null
)

data class ReglabSession(
    var remember_web_59ba36addc2b2f9401580f014c7f58ea4e30989d: String? = null,
    var credentials: ReglabCredentials? = null
)