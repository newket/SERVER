package com.newket.api.controller.auth

import com.newket.application.auth.AuthService
import com.newket.application.auth.dto.*
import com.newket.infra.jpa.auth.constant.SocialLoginProvider
import org.springframework.web.bind.annotation.*

@RestController
class AuthController(
    private val authService: AuthService
) {
    //온보딩 전
    @PostMapping(AuthApi.V1.SOCIAL_LOGIN)
    fun socialLogin(
        @PathVariable provider: SocialLoginProvider,
        @RequestBody socialLoginRequest: SocialLogin.Request
    ): SocialLogin.Response {
        return authService.socialLogin(provider, socialLoginRequest)
    }

    @PostMapping(AuthApi.V1.SOCIAL_LOGIN_APPLE)
    fun socialLoginApple(
        @RequestBody socialLoginAppleRequest: SocialLoginApple.Request
    ): SocialLogin.Response {
        return authService.socialLoginApple(socialLoginAppleRequest)
    }

    //온보딩
    @PostMapping(AuthApi.V1.SIGNUP)
    fun signup(
        @PathVariable provider: SocialLoginProvider,
        @RequestBody signUpRequest: SignUp.V1.Request
    ): SignUp.Response {
        return authService.signupV1(provider, signUpRequest)
    }

    //v2
    @PostMapping(AuthApi.V2.SIGNUP)
    fun signupV2(
        @PathVariable provider: SocialLoginProvider,
        @RequestBody signUpRequest: SignUp.V2.Request
    ): SignUp.Response {
        return authService.signupV2(provider, signUpRequest)
    }

    @PostMapping(AuthApi.V1.SIGNUP_APPLE)
    fun signUpApple(
        @RequestBody signUpRequest: SignUpApple.Request
    ): SignUp.Response {
        return authService.signUpApple(signUpRequest)
    }

    @PutMapping(AuthApi.V1.REISSUE)
    fun reissueToken(@RequestBody reissueRequest: Reissue.Request): Reissue.Response {
        return authService.reissueToken(reissueRequest)
    }

    //탈퇴
    @DeleteMapping(AuthApi.V1.BASE_URL)
    fun withdraw() {
        return authService.withdraw()
    }

    //탈퇴
    @DeleteMapping(AuthApi.V1.WITHDRAW_APPLE)
    fun withdrawV2(@RequestBody request: WithdrawApple.Request) {
        return authService.withdrawApple(request)
    }
}