package com.template.webserver.model

import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "roles")
data class Role(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
        @GenericGenerator(name = "increment", strategy = "increment")
        @Column(name = "id")
        val id: Int? = null,

        @Enumerated(EnumType.STRING)
        @Column(name = "name")
        val name: ERole
) {
    constructor() : this(null, ERole.ROLE_CLIENT)
}
