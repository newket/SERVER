package com.newket.core.auth.admin


import com.newket.core.exception.AdminException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.stereotype.Service

@Service
class AdminSessionAuthService(
    private val authenticationManager: AuthenticationManager,
    private val adminUserRepository: AdminUserRepository
) {

    fun login(username: String, password: String, request: HttpServletRequest): AdminUserInfo {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(username, password)
            )

            SecurityContextHolder.getContext().authentication = authentication

            val session = request.getSession(true)
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
            )

            val adminUserDetails = authentication.principal as AdminUserDetails
            val adminUserInfo = adminUserDetails.getAdminUserInfo()

            adminUserRepository.updateLastLoginAt(adminUserInfo.username)

            return adminUserInfo
        } catch (e: Exception) {
            throw AdminException.AdminUserNotFoundException()
        }
    }

    fun logout(request: HttpServletRequest) {
        request.getSession(false)?.invalidate()
        SecurityContextHolder.clearContext()
    }
}