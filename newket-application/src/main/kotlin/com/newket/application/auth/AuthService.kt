package com.newket.application.auth

import com.newket.application.auth.dto.*
import com.newket.client.oauth.apple.AppleOauthClient
import com.newket.client.oauth.kakao.KakaoOauthClient
import com.newket.core.auth.JwtTokenProvider
import com.newket.core.auth.RefreshTokenRepository
import com.newket.core.auth.getCurrentUserId
import com.newket.domain.artist.service.ArtistAppender
import com.newket.domain.user.exception.UserException
import com.newket.domain.user.service.UserAppender
import com.newket.domain.user.service.UserModifier
import com.newket.domain.user.service.UserReader
import com.newket.domain.user.service.UserRemover
import com.newket.infra.jpa.auth.constant.SocialLoginProvider
import com.newket.infra.jpa.notifiacation.entity.ArtistNotification
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
    private val artistAppender: ArtistAppender,
    private val userModifier: UserModifier,
    private val appleOauthClient: AppleOauthClient,
    private val userRemover: UserRemover
) {
    //V1
    @Transactional
    fun signupV1(socialLoginProvider: SocialLoginProvider, request: SignUp.V1.Request): SignUp.Response {
        val kakaoUserInfo = kakaoOAuthClient.retrieveUserInfo(request.accessToken)
            ?: throw UserException.KakaoUserNotFoundException()
        val newUser = User(
            socialInfo = SocialInfo(
                socialId = kakaoUserInfo.id,
                socialLoginProvider = socialLoginProvider
            ),
            name = kakaoUserInfo.getName(),
            nickname = kakaoUserInfo.getName(),
            email = kakaoUserInfo.getEmail(),
            type = UserType.USER
        ).apply {
            userAppender.addUser(this)
        }
        request.favoriteArtistIds.map {
            artistAppender.addUserFavoriteArtist(
                ArtistNotification(
                    userId = newUser.id,
                    artistId = it
                )
            )
        }

        val accessToken = jwtTokenProvider.createAccessToken(newUser.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(newUser.id)
        refreshTokenRepository.save(newUser.id, refreshToken)
        return SignUp.Response(accessToken, refreshToken)
    }

    //V2
    @Transactional
    fun signupV2(socialLoginProvider: SocialLoginProvider, request: SignUp.V2.Request): SignUp.Response {
        val kakaoUserInfo = kakaoOAuthClient.retrieveUserInfo(request.accessToken)
            ?: throw UserException.KakaoUserNotFoundException()
        val newUser = User(
            socialInfo = SocialInfo(
                socialId = kakaoUserInfo.id,
                socialLoginProvider = socialLoginProvider
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
        return SignUp.Response(accessToken, refreshToken)
    }

    @Transactional
    fun signUpApple(request: SignUpApple.Request): SignUp.Response {
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
        return SignUp.Response(accessToken, refreshToken)
    }

    @Transactional
    fun socialLogin(socialLoginProvider: SocialLoginProvider, request: SocialLogin.Request): SocialLogin.Response {
        val kakaoUserInfo = kakaoOAuthClient.retrieveUserInfo(request.accessToken)
            ?: throw UserException.KakaoUserNotFoundException()

        return userReader.findBySocialIdAndProviderOrNull(kakaoUserInfo.id, socialLoginProvider)?.let { user ->
            val accessToken = jwtTokenProvider.createAccessToken(user.id)
            val refreshToken = jwtTokenProvider.createRefreshToken(user.id)
            refreshTokenRepository.saveOrUpdateToken(user.id, refreshToken)
            SocialLogin.Response(accessToken, refreshToken)
        } ?: throw UserException.UserNotFoundException()
    }

    @Transactional
    fun socialLoginApple(request: SocialLoginApple.Request): SocialLogin.Response {
        return userReader.findBySocialIdAndProviderOrNull(request.socialId, SocialLoginProvider.APPLE)?.let { user ->
            val accessToken = jwtTokenProvider.createAccessToken(user.id)
            val refreshToken = jwtTokenProvider.createRefreshToken(user.id)
            refreshTokenRepository.saveOrUpdateToken(user.id, refreshToken)
            SocialLogin.Response(accessToken, refreshToken)
        } ?: throw UserException.UserNotFoundException()
    }

    @Transactional
    fun reissueToken(request: Reissue.Request): Reissue.Response {
        val reissueToken = jwtTokenProvider.reissueToken(request.refreshToken)
        return Reissue.Response(
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
    fun withdrawApple(request: WithdrawApple.Request) {
        val userId = getCurrentUserId()
        //애플에서 앱 탈퇴
        val appleInfo = appleOauthClient.retrieveUserInfo(request.authorizationCode)
        appleOauthClient.revoke(appleInfo.access_token)

        val user = userReader.findById(userId)
        userRemover.deleteAllUserDevice(userId)
        userModifier.updateSocialIdWithdraw(user)
    }
}