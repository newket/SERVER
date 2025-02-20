package com.newket.core.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.newket.core.auth.AuthException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ExceptionHandlerFilter(
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (exception: AuthException) {
            response.apply {
                this.status = HttpServletResponse.SC_UNAUTHORIZED
                this.contentType = MediaType.APPLICATION_JSON_VALUE
                this.characterEncoding = "UTF-8"
                this.writer.write(objectMapper.writeValueAsString(ErrorResponse(exception.code, exception.message)))
            }
        }
    }
}