package com.template.webserver.controller

import com.template.webserver.config.security.jwt.JwtUtils
import com.template.webserver.model.ERole
import com.template.webserver.model.Role
import com.template.webserver.model.User
import com.template.webserver.payload.request.LoginRequest
import com.template.webserver.payload.request.SignupRequest
import com.template.webserver.payload.response.JwtResponse
import com.template.webserver.payload.response.MessageResponse
import com.template.webserver.repository.RoleRepository
import com.template.webserver.repository.UserRepository
import com.template.webserver.service.UserDetailsImpl
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
class AuthController(
        private val authenticationManager: AuthenticationManager,
        private val userRepository: UserRepository,
        private val roleRepository: RoleRepository,
        private val encoder: PasswordEncoder,
        private val jwtUtils: JwtUtils
) {

    @PostMapping("/signin")
    fun authenticateUser(@RequestBody loginRequest: LoginRequest): ResponseEntity<*> {
        val authentication: Authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))
        SecurityContextHolder.getContext().authentication = authentication
        val jwt = jwtUtils.generateJwtToken(authentication)
        val userDetails = authentication.principal as UserDetailsImpl
        val roles = userDetails.authorities.map { item: GrantedAuthority -> item.authority }
        return ResponseEntity.ok<Any>(
                JwtResponse(
                        accessToken = jwt,
                        id = userDetails.id,
                        username = userDetails.username,
                        email = userDetails.email,
                        roles = roles
                )
        )
    }

//    @PostMapping("/signup")
//    fun registerUser(@RequestBody signUpRequest: SignupRequest): ResponseEntity<*> {
//
//        if (userRepository.existsByUsername(signUpRequest.username)) {
//            return ResponseEntity
//                    .badRequest()
//                    .body<Any>(MessageResponse("Error: Username is already taken!"))
//        }
//
//        if (userRepository.existsByEmail(signUpRequest.email)) {
//            return ResponseEntity
//                    .badRequest()
//                    .body<Any>(MessageResponse("Error: Email is already in use!"))
//        }
//
//        val roles = signUpRequest.roles.map { role: String ->
//            when (role) {
//                "insuree" -> {
//                    val adminRole: Role = roleRepository.findByName(ERole.ROLE_INSUREE)
//                            .orElseThrow { error("Error: Role is not found.") }
//                    adminRole
//                }
//                "insurer" -> {
//                    val modRole: Role = roleRepository.findByName(ERole.ROLE_INSURER)
//                            .orElseThrow { error("Error: Role is not found.") }
//                    modRole
//                }
//                else -> {
//                    error("Error: Role is not found.")
//                }
//            }
//        }
//
//        // Create new user's account
//        val user = User(
//                username = signUpRequest.username,
//                email = signUpRequest.email,
//                password = encoder.encode(signUpRequest.password),
//                roles = roles.toMutableSet()
//        )
//
//        userRepository.save(user)
//        return ResponseEntity.ok<Any>(MessageResponse("User registered successfully!"))
//    }
}