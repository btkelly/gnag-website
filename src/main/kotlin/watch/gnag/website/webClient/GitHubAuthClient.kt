package watch.gnag.website.webClient

import com.detroitlabs.middleware.core.webclient.customObjectMapper
import com.detroitlabs.middleware.core.webclient.queryParam
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import watch.gnag.website.configuration.GitHubAppProperties
import watch.gnag.website.models.github.AccessTokenResponse
import java.net.URI

@Component
class GitHubAuthClient(
    private val gitHubAppProperties: GitHubAppProperties,
    webclientBuilder: WebClient.Builder,
    objectMapper: ObjectMapper
) {

    companion object {
        private const val GITHUB_AUTHENTICATION_BASE_URL = "https://github.com/login"
    }

    private val webClient = webclientBuilder
        .baseUrl(GITHUB_AUTHENTICATION_BASE_URL)
        .customObjectMapper(
            objectMapper.copy()
                .apply {
                    propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                }
        )
        .queryParam("client_id", gitHubAppProperties.id)
        .queryParam("client_secret", gitHubAppProperties.secret)
        .defaultHeader("Accept", "application/json")
        .build()

    fun getAuthenticationURI() = URI("$GITHUB_AUTHENTICATION_BASE_URL/oauth/authorize?scope=repo&client_id=${gitHubAppProperties.id}")

    fun getAccessToken(tempCode: String) = webClient.post()
        .uri {
            it.pathSegment("oauth")
                .pathSegment("access_token")
                .queryParam("code", tempCode)
                .build()
        }
        .retrieve()
        .bodyToMono<AccessTokenResponse>()

}