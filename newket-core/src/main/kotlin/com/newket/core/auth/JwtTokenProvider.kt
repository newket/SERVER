package com.newket.core.auth

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.io.StringReader
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
    private val appleProperties: AppleProperties,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    private val key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))

    fun createAccessToken(userId: Long): String {
        return createToken(userId, jwtProperties.accessTokenExpirationTime)
    }

    fun createRefreshToken(userId: Long): String {
        return createToken(userId, jwtProperties.refreshTokenExpirationTime)
    }

    private fun createToken(userId: Long, expireTime: Long): String {
        val now = Date()
        val issuer = "newket"
        return Jwts.builder()
            .issuer(issuer)
            .claim("id", userId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + expireTime))
            .signWith(key)
            .compact()
    }

    fun reissueToken(token: String): Map<String, String> {
        if (!refreshTokenRepository.existsToken(token)) {
            throw AuthException.RefreshTokenNotFoundException()
        }
        validateToken(token)
        val userId = validateToken(token)["id"].toString().toLong()
        val accessToken = createAccessToken(userId)
        val refreshToken = createRefreshToken(userId)
        refreshTokenRepository.updateToken(userId, refreshToken)
        return mapOf(
            "accessToken" to accessToken,
            "refreshToken" to refreshToken
        )
    }

    fun validateToken(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload

        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException,
                is UnsupportedJwtException,
                is MalformedJwtException,
                is SecurityException -> throw AuthException.AuthFailedException("잘못된 토큰입니다.")
                is ExpiredJwtException -> throw AuthException.ExpiredTokenException()
                else -> throw AuthException.AuthFailedException("토큰 검증 중 알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    fun getAuthentication(token: String): Authentication {
        val userId = validateToken(token)["id"].toString()
        val authorities = Collections.singleton(SimpleGrantedAuthority("ROLE_USER"))
        val user = User(userId, "", authorities)
        return UsernamePasswordAuthenticationToken(user, "", authorities)
    }

    fun generateAppleClientSecret(): String {
        val reader = StringReader(appleProperties.privateKey.trimIndent())
        val privateKey = JcaPEMKeyConverter().getPrivateKey(
            PrivateKeyInfo.getInstance(PEMParser(reader).readObject())
        )
        return Jwts.builder()
            .issuer(appleProperties.teamId)
            .subject(appleProperties.clientId)
            .expiration(Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant()))
            .issuedAt(Date(System.currentTimeMillis()))
            .audience().add("https://appleid.apple.com").and()
            .signWith(privateKey)
            .header().keyId(appleProperties.keyId).and()
            .compact()
    }
}