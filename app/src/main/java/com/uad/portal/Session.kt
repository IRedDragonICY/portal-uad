package com.uad.portal

import com.uad.portal.API.Credentials
import com.uad.portal.API.ReglabCredentials

data class Session(
    var session: String? = null,
    var userInfo: UserInfo? = null,
    var credentials: Credentials? = null
)

data class ReglabSession(
    var session: String? = null,
    var credentials: ReglabCredentials? = null
)