package com.template.webserver.payload.response

data class JwtResponse(
        val accessToken: String,
        val id: Int,
        val username: String,
        val email: String,
        val roles: List<String>,
        val tokenType: String = "Bearer"
) {
    constructor() : this("", 1, "", "", listOf())
}