package com.uad.portal

data class Session(
    var session: String? = null,
    var userInfo: UserInfo? = null,
    var credentials: Credentials? = null
)