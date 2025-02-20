package com.newket.core.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User

fun getCurrentUserId(): Long {
    return try {
        val principal = SecurityContextHolder.getContext().authentication.principal as User
        principal.username.toLong()
    } catch (ex: Exception){
        throw AuthException.AuthFailedException()
    }
}