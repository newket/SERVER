package com.newket.core.auth.admin

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AdminUserDetailsService(
    private val adminUserRepository: AdminUserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val adminUserInfo = adminUserRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("관리자를 찾을 수 없습니다: $username")
        return AdminUserDetails(adminUserInfo)
    }
}