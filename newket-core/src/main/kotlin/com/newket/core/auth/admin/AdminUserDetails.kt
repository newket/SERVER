package com.newket.core.auth.admin

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AdminUserDetails(
    private val adminUserInfo: AdminUserInfo
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${adminUserInfo.role}"))

    override fun getPassword(): String = adminUserInfo.password
    override fun getUsername(): String = adminUserInfo.username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = adminUserInfo.isEnabled

    fun getAdminUserInfo(): AdminUserInfo = adminUserInfo
}