package me.yapoo.fido2.handler.authentication

import com.webauthn4j.WebAuthnAuthenticationManager
import com.webauthn4j.data.AuthenticationParameters
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.client.challenge.DefaultChallenge
import com.webauthn4j.server.ServerProperty
import com.webauthn4j.util.Base64Util
import com.webauthn4j.validator.exception.ValidationException
import me.yapoo.fido2.config.ServerConfig
import me.yapoo.fido2.domain.authentication.AuthenticatorRepository
import me.yapoo.fido2.domain.authentication.UserAuthenticationChallengeRepository

class AuthenticationHandler(
    private val userAuthenticationChallengeRepository: UserAuthenticationChallengeRepository,
    private val authenticatorRepository: AuthenticatorRepository,
) {

    fun handle(
        request: AuthenticationRequest
    ) {
        val authenticationRequest = com.webauthn4j.data.AuthenticationRequest(
            Base64Util.decode(request.id),
            Base64Util.decode(request.response.userHandle),
            Base64Util.decode(request.response.authenticatorData),
            Base64Util.decode(request.response.clientDataJSON),
            Base64Util.decode(request.response.signature)
        )
        val serverChallenge = userAuthenticationChallengeRepository.find(
            String(authenticationRequest.userHandle)
        ) ?: throw Exception()

        val authenticator = authenticatorRepository.find(authenticationRequest.credentialId)
            ?: throw Exception()

        val authenticationParameters = AuthenticationParameters(
            ServerProperty(
                Origin.create(ServerConfig.origin),
                ServerConfig.rpid,
                DefaultChallenge(serverChallenge.challenge.toByteArray()),
                null
            ),
            authenticator,
            listOf(authenticator.attestedCredentialData.credentialId),
            true,
            true,
        )

        val manager = WebAuthnAuthenticationManager()
        try {
            manager.validate(authenticationRequest, authenticationParameters)
        } catch (e: ValidationException) {
            throw Exception(e)
        }

        authenticator.counter++
        authenticatorRepository.update(authenticator)
    }
}