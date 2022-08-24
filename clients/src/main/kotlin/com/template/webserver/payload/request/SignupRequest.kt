package com.template.webserver.payload.request

data class SignupRequest(
        val username: String,
        val email: String,
        val roles: Set<String>,
        val password: String
) {
    constructor() : this("", "", setOf(), "")
}