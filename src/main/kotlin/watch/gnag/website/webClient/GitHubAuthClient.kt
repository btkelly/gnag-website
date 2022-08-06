package watch.gnag.website.webClient

import com.detroitlabs.middleware.core.webclient.customObjectMapper
import com.detroitlabs.middleware.core.webclient.queryParam
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponentsBuilder
import watch.gnag.website.configuration.GitHubAppProperties
import watch.gnag.website.models.github.AccessTokenResponse
import java.net.URI

@Component
@EnableConfigurationProperties(GitHubAppProperties::class)
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

    fun getAuthenticationURI(): URI = UriComponentsBuilder.fromHttpUrl(GITHUB_AUTHENTICATION_BASE_URL)
        .pathSegment("oauth")
        .pathSegment("authorize")
        .queryParam("scope", "repo")
        .queryParam("client_id", gitHubAppProperties.id)
        .let { uriBuilder ->
            gitHubAppProperties.redirectUrl?.let {
                uriBuilder.queryParam("redirect_uri", "{redirectUri}").build(it)
            } ?: uriBuilder.build().toUri()
        }

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