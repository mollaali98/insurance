package com.template.webserver.service

import com.fasterxml.jackson.annotation.JsonIgnore
import com.template.webserver.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


data class UserDetailsImpl(
        val id: Int,
        private val username: String,
        val email: String,
        @field:JsonIgnore
        private val password: String,
        private val authorities: Collection<GrantedAuthority>
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }


    companion object {
        private const val serialVersionUID = 1L
    }
}

fun User.build(): UserDetailsImpl {
    val authorities: List<GrantedAuthority> = roles.map { role -> SimpleGrantedAuthority(role.name.name) }
    return UserDetailsImpl(id!!, username, email, password, authorities)
}