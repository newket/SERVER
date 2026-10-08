package com.newket.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.newket.core.auth.JwtAuthenticationFilter
import com.newket.core.auth.JwtTokenProvider
import com.newket.core.auth.admin.AdminUserDetailsService
import com.newket.core.exception.ExceptionHandlerFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val objectMapper: ObjectMapper,
    private val adminUserDetailsService: AdminUserDetailsService
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }

    @Bean
    fun daoAuthenticationProvider(passwordEncoder: PasswordEncoder): DaoAuthenticationProvider {
        return DaoAuthenticationProvider().apply {
            setUserDetailsService(adminUserDetailsService)
            setPasswordEncoder(passwordEncoder)
        }
    }

    @Bean
    fun securityContextRepository(): SecurityContextRepository =
        HttpSessionSecurityContextRepository()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            }
            .securityContext {
                it.securityContextRepository(securityContextRepository())
            }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.POST, "/api/v1/admins/ticket").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/admins/ticket/{ticketId}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/admins/ticket/musical").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/admins/ticket/musical/{ticketId}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/admins/ticket/{ticketId}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/admins/artist").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/admins/group").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/admins/place").hasRole("ADMIN")
                    .requestMatchers("/api/v1/auth/login/KAKAO").permitAll()
                    .anyRequest().permitAll()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.status = 401
                    response.contentType = "application/json;charset=UTF-8"
                    response.writer.write("""{"code":"ADMIN-1","message":"관리자만 가능한 요청입니다."}""")
                }
                it.accessDeniedHandler { _, response, _ ->
                    response.status = 401
                    response.contentType = "application/json;charset=UTF-8"
                    response.writer.write("""{"code":"ADMIN-1","message":"관리자만 가능한 요청입니다."}""")
                }
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .addFilterBefore(
                ExceptionHandlerFilter(objectMapper),
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }
}