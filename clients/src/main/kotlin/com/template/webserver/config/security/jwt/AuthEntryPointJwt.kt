package com.template.webserver.config.security.jwt

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException;
import javax.servlet.http.HttpServletRequest

@Component
class AuthEntryPointJwt : AuthenticationEntryPoint {

    override fun commence(
            request: HttpServletRequest?,
            response: HttpServletResponse,
            authException: AuthenticationException
    ) {
        logger.error("Unauthorized error: {}", authException.message)
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AuthEntryPointJwt::class.java)
    }
}