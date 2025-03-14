package com.newket.api.controller.auth

import com.newket.application.auth.AuthService
import com.newket.application.auth.dto.*
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
class AuthController(
    private val authService: AuthService
) {
    //온보딩 전
    @PostMapping(AuthApi.V1.SOCIAL_LOGIN_KAKAO)
    fun socialLoginKakao(
        @RequestBody socialLoginKakaoRequest: SocialLoginKakaoRequest
    ): Mono<TokenResponse> {
        return authService.socialLoginKakao(socialLoginKakaoRequest)
    }

    @PostMapping(AuthApi.V1.SOCIAL_LOGIN_APPLE)
    fun socialLoginApple(
        @RequestBody socialLoginAppleRequest: SocialLoginAppleRequest
    ): TokenResponse {
        return authService.socialLoginApple(socialLoginAppleRequest)
    }

    // 회원가입
    @PostMapping(AuthApi.V1.SIGNUP_KAKAO)
    fun signupKakao(
        @RequestBody signUpKakaoRequestRequest: SignUpKakaoRequest
    ): Mono<TokenResponse> {
        return authService.signupKakao(signUpKakaoRequestRequest)
    }

    @PostMapping(AuthApi.V1.SIGNUP_APPLE)
    fun signUpApple(
        @RequestBody signUpRequest: SignUpAppleRequest
    ): TokenResponse {
        return authService.signUpApple(signUpRequest)
    }

    // 토큰 갱신
    @PutMapping(AuthApi.V1.REISSUE)
    fun reissueToken(@RequestBody reissueRequest: ReissueRequest): TokenResponse {
        return authService.reissueToken(reissueRequest)
    }

    //탈퇴 v2.2.0 이후 제거됨
    @DeleteMapping(AuthApi.V1.BASE_URL)
    fun withdraw() {
        return authService.withdraw()
    }

    //탈퇴
    @DeleteMapping(AuthApi.V1.WITHDRAW_KAKAO)
    fun withdrawKakao() {
        return authService.withdraw()
    }

    @DeleteMapping(AuthApi.V1.WITHDRAW_APPLE)
    fun withdrawApple(@RequestBody request: WithdrawAppleRequest) {
        return authService.withdrawApple(request)
    }
}