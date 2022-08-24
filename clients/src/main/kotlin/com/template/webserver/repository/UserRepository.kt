package com.template.webserver.repository

import com.template.webserver.model.ERole
import com.template.webserver.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User?, Long?> {
    fun findByUsername(username: String): Optional<User>
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
    @Query("select u from User u join u.roles r where r.name = :name")
    fun findByRole(name: ERole): List<User>
}