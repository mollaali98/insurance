package com.template.webserver.repository

import com.template.webserver.model.ERole
import com.template.webserver.model.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface RoleRepository : JpaRepository<Role?, Long?> {
    fun findByName(name: ERole): Optional<Role>
}