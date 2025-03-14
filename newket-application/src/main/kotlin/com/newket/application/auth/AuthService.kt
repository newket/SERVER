package com.newket.application.auth

import com.newket.application.auth.dto.*
import com.newket.client.oauth.apple.AppleOauthClient
import com.newket.client.oauth.kakao.KakaoOauthClient
import com.newket.core.auth.JwtTokenProvider
import com.newket.core.auth.RefreshTokenRepository
import com.newket.core.auth.getCurrentUserId
import com.newket.domain.user.exception.UserException
import com.newket.domain.user.UserAppender
import com.newket.domain.user.UserModifier
import com.newket.domain.user.UserReader
import com.newket.domain.user.UserRemover
import com.newket.infra.jpa.auth.constant.SocialLoginProvider
import com.newket.infra.jpa.user.constant.UserType
import com.newket.infra.jpa.user.entity.SocialInfo
import com.newket.infra.jpa.user.entity.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class AuthService(
    private val kakaoOAuthClient: KakaoOauthClient,
    private val userAppender: UserAppender,
    private val userReader: UserReader,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userModifier: UserModifier,
    private val appleOauthClient: AppleOauthClient,
    private val userRemover: UserRemover
) {
    // 회원가입
    @Transactional
    fun signupKakao(request: SignUpKakaoRequest): TokenResponse {
        val kakaoUserInfo = kakaoOAuthClient.retrieveUserInfo(request.accessToken)
            ?: throw UserException.KakaoUserNotFoundException()
        val newUser = User(
            socialInfo = SocialInfo(
                socialId = kakaoUserInfo.id,
                socialLoginProvider = SocialLoginProvider.KAKAO
            ),
            name = kakaoUserInfo.getName(),
            nickname = kakaoUserInfo.getName(),
            email = kakaoUserInfo.getEmail(),
            type = UserType.USER
        ).apply {
            userAppender.addUser(this)
        }

        val accessToken = jwtTokenProvider.createAccessToken(newUser.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(newUser.id)
        refreshTokenRepository.save(newUser.id, refreshToken)
        return TokenResponse(accessToken, refreshToken)
    }

    @Transactional
    fun signUpApple(request: SignUpAppleRequest): TokenResponse {
        val newUser = User(
            socialInfo = SocialInfo(
                socialId = request.socialId,
                socialLoginProvider = SocialLoginProvider.APPLE
            ),
            name = request.name,
            nickname = request.name,
            email = request.email,
            type = UserType.USER
        ).apply {
            userAppender.addUser(this)
        }

        val accessToken = jwtTokenProvider.createAccessToken(newUser.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(newUser.id)
        refreshTokenRepository.save(newUser.id, refreshToken)
        return TokenResponse(accessToken, refreshToken)
    }

    @Transactional
    fun socialLoginKakao(request: SocialLoginKakaoRequest): TokenResponse {
        val kakaoUserInfo = kakaoOAuthClient.retrieveUserInfo(request.accessToken)
            ?: throw UserException.KakaoUserNotFoundException()

        return userReader.findBySocialIdAndProviderOrNull(kakaoUserInfo.id, SocialLoginProvider.KAKAO)?.let { user ->
            val accessToken = jwtTokenProvider.createAccessToken(user.id)
            val refreshToken = jwtTokenProvider.createRefreshToken(user.id)
            refreshTokenRepository.saveOrUpdateToken(user.id, refreshToken)
            TokenResponse(accessToken, refreshToken)
        } ?: throw UserException.UserNotFoundException()
    }

    @Transactional
    fun socialLoginApple(request: SocialLoginAppleRequest): TokenResponse {
        return userReader.findBySocialIdAndProviderOrNull(request.socialId, SocialLoginProvider.APPLE)?.let { user ->
            val accessToken = jwtTokenProvider.createAccessToken(user.id)
            val refreshToken = jwtTokenProvider.createRefreshToken(user.id)
            refreshTokenRepository.saveOrUpdateToken(user.id, refreshToken)
            TokenResponse(accessToken, refreshToken)
        } ?: throw UserException.UserNotFoundException()
    }

    @Transactional
    fun reissueToken(request: ReissueRequest): TokenResponse {
        val reissueToken = jwtTokenProvider.reissueToken(request.refreshToken)
        return TokenResponse(
            reissueToken.getValue("accessToken"),
            reissueToken.getValue("refreshToken")
        )
    }

    @Transactional
    fun withdraw() {
        val userId = getCurrentUserId()
        val user = userReader.findById(userId)
        userRemover.deleteAllUserDevice(userId)
        userModifier.updateSocialIdWithdraw(user)
    }

    @Transactional
    fun withdrawApple(request: WithdrawAppleRequest) {
        val userId = getCurrentUserId()
        //애플에서 앱 탈퇴
        val appleInfo = appleOauthClient.retrieveUserInfo(request.authorizationCode)
        appleOauthClient.revoke(appleInfo.access_token)

        val user = userReader.findById(userId)
        userRemover.deleteAllUserDevice(userId)
        userModifier.updateSocialIdWithdraw(user)
    }
}