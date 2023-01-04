package me.yapoo.domain.registration

import java.time.Duration
import java.time.Instant

data class UserRegistrationChallenge(
    val userId: String,
    val username: String,
    val challenge: String,
    val createdAt: Instant,
) {

    val timeout: Duration = Duration.ofMinutes(5)
}