package me.yapoo.fido2.handler.preregistration

import me.yapoo.fido2.config.ServerConfig
import me.yapoo.fido2.domain.registration.UserRegistrationChallenge
import me.yapoo.fido2.domain.registration.UserRegistrationChallengeRepository
import me.yapoo.fido2.domain.user.UserRepository
import me.yapoo.fido2.dto.AttestationConveyancePreference
import me.yapoo.fido2.dto.COSEAlgorithmIdentifier
import me.yapoo.fido2.dto.PublicKeyCredentialParameters
import me.yapoo.fido2.dto.PublicKeyCredentialRpEntity
import me.yapoo.fido2.dto.PublicKeyCredentialUserEntity
import java.time.Instant
import java.util.*

class PreregistrationHandler(
    private val userRepository: UserRepository,
    private val userRegistrationChallengeRepository: UserRegistrationChallengeRepository,
) {

    fun handle(
        request: PreregistrationRequest
    ): PreregistrationResponse {
        if (userRepository.find(request.username) != null) {
            throw Exception()
        }

        val userId = UUID.randomUUID().toString()
        val challenge = UserRegistrationChallenge(
            userId = userId,
            username = request.username,
            challenge = UUID.randomUUID().toString(),
            createdAt = Instant.now()
        )
        userRegistrationChallengeRepository.create(challenge)

        return PreregistrationResponse(
            rp = PublicKeyCredentialRpEntity(
                id = ServerConfig.rpid,
                name = ServerConfig.name
            ),
            user = PublicKeyCredentialUserEntity(
                id = userId,
                name = challenge.username,
                displayName = challenge.username
            ),
            challenge = challenge.challenge,
            pubKeyCredParams = listOf(
                PublicKeyCredentialParameters(
                    alg = COSEAlgorithmIdentifier.ES256,
                )
            ),
            timeout = challenge.timeout.toMillis().toInt(),
            authenticatorSelection = null,
            attestation = AttestationConveyancePreference.None,
            excludeCredentials = emptyList(),
            extensions = emptyMap(),
        )
    }
}
