package com.template.webserver.model

import org.hibernate.annotations.GenericGenerator
import javax.persistence.*


@Entity
@Table(
        name = "users",
        uniqueConstraints = [UniqueConstraint(columnNames = ["username"]), UniqueConstraint(columnNames = ["email"])]
)
data class User(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
        @GenericGenerator(name = "increment", strategy = "increment")
        @Column(name = "id")
        val id: Int? = null,

        @Column(name = "username")
        val username: String,

        @Column(name = "email")
        val email: String,

        @Column(name = "password")
        val password: String,

        @Column(name = "network_id")
        val networkId: String,

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")], inverseJoinColumns = [JoinColumn(name = "role_id")])
        val roles: MutableSet<Role> = mutableSetOf()
) {
    constructor() : this(null, "empty", "empty@abv.bg", "password", "empty")
}