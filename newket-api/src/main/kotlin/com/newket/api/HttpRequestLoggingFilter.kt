package com.newket.api

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class HttpRequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startTime = System.currentTimeMillis()
        val method = request.method
        val uri = request.requestURI

        var exception: Exception? = null

        try {
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            exception = e
            throw e
        } finally {
            val duration = System.currentTimeMillis() - startTime
            val status = response.status

            // ExceptionHandler에서 저장한 예외 가져오기
            val handledException =
                request.getAttribute("request.exception") as? Exception

            val actualException = exception ?: handledException

            val requestLog =
                "[$method] $uri - $status (${duration}ms)"

            if (status >= 400 || actualException != null) {
                if (actualException != null) {
                    log.error(
                        "$requestLog - ${actualException.javaClass.simpleName}: ${actualException.message}",
                        actualException
                    )
                } else {
                    log.error(requestLog)
                }
            } else {
                log.info(requestLog)
            }
        }
    }
}