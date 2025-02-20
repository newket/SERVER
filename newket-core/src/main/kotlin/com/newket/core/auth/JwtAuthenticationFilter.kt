package com.newket.core.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {
    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        extractToken(request)?.let{
            val authentication = jwtTokenProvider.getAuthentication(it)
            SecurityContextHolder.getContext().authentication = authentication
        }
        chain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = "Authorization"
        val prefix = "Bearer "
        val bearerToken = request.getHeader(header)

        return if (StringUtils.hasText(bearerToken) &&
            bearerToken.startsWith(prefix)
        ) {
            bearerToken.substring(prefix.length)
        } else {
            null
        }
    }
}