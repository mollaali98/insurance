package com.template.webserver.payload.request

data class LoginRequest(
        val username: String,
        val password: String
) {
    constructor() : this("", "")
}