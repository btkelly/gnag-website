package watch.gnag.website.utils

import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono
import watch.gnag.website.models.github.AccessTokenResponse

object SessionUtil {

    private const val GITHUB_TOKEN_SESSION_KEY = "session_token"

    fun saveTokenToSession(session: Mono<WebSession>, accessTokenResponse: AccessTokenResponse): Mono<Unit> {
        return session.map { it.attributes[GITHUB_TOKEN_SESSION_KEY] = accessTokenResponse.accessToken }
    }

    fun getTokenFromSession(session: Mono<WebSession>): Mono<String> {
        return session.mapNotNull { it.getAttribute(GITHUB_TOKEN_SESSION_KEY) }
    }

    fun clearSession(session: Mono<WebSession>): Mono<Unit> {
        return session.flatMap { it.invalidate() }.thenReturn(Unit)
    }
}
