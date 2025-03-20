package com.newket.api.controller.auth

import com.newket.application.auth.AuthService
import com.newket.application.auth.dto.*
import com.newket.infra.jpa.auth.constant.SocialLoginProvider
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
class AuthController(
    private val authService: AuthService
) {
    // 회원가입
    @PostMapping(AuthApi.V1.SIGNUP)
    fun signup(
        @PathVariable provider: SocialLoginProvider,
        @RequestBody signUpRequest: SignUpRequest
    ): TokenResponse {
        return authService.signup(provider, signUpRequest)
    }

    @PostMapping(AuthApi.V1.SIGNUP_KAKAO)
    fun signupKakao(
        @RequestBody signUpKakaoRequestRequest: SignUpKakaoRequest
    ): Mono<TokenResponse> {
        return authService.signupKakao(signUpKakaoRequestRequest)
    }

    // 로그인
    @PostMapping(AuthApi.V1.SOCIAL_LOGIN)
    fun socialLogin(
        @PathVariable provider: SocialLoginProvider,
        @RequestBody socialLoginRequest: SocialLoginRequest
    ): TokenResponse {
        return authService.socialLogin(provider, socialLoginRequest)
    }

    @PostMapping(AuthApi.V1.SOCIAL_LOGIN_KAKAO)
    fun socialLoginKakao(
        @RequestBody socialLoginKakaoRequest: SocialLoginKakaoRequest
    ): Mono<TokenResponse> {
        return authService.socialLoginKakao(socialLoginKakaoRequest)
    }

    // 토큰 갱신
    @PutMapping(AuthApi.V1.REISSUE)
    fun reissueToken(@RequestBody reissueRequest: ReissueRequest): TokenResponse {
        return authService.reissueToken(reissueRequest)
    }

    //탈퇴
    @DeleteMapping(AuthApi.V1.BASE_URL)
    fun withdraw() {
        return authService.withdraw()
    }

    //탈퇴 v2.2.0 이후 제거됨
    @DeleteMapping(AuthApi.V1.WITHDRAW_KAKAO)
    fun withdrawKakao() {
        return authService.withdraw()
    }

    @DeleteMapping(AuthApi.V1.WITHDRAW_APPLE)
    fun withdrawApple(@RequestBody request: WithdrawAppleRequest) {
        return authService.withdrawApple(request)
    }
}