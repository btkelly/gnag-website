package watch.gnag.website.webClient

import com.detroitlabs.middleware.core.webclient.customObjectMapper
import com.detroitlabs.middleware.core.webclient.queryParam
import com.fasterxml.jackson.annotation.JsonProperty.Access
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
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

    private val customObjectMapper = objectMapper.copy()
        .apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }

    private val webClient = webclientBuilder
        .baseUrl(GITHUB_AUTHENTICATION_BASE_URL)
        .customObjectMapper(customObjectMapper)
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
        .exchangeToMono { response ->
            if (response.statusCode().is2xxSuccessful) {
                response.bodyToMono<String>()
            } else {
                response.createException().map { throw it }
            }
        }
        .map { stringBody ->
            if (stringBody.contains("\"error\"")) {
                throw HttpServerErrorException(HttpStatus.BAD_REQUEST, "Error fetching access token")
            } else {
                customObjectMapper.readValue(stringBody, AccessTokenResponse::class.java)
            }
        }
        .onErrorContinue { t, u ->
            AccessTokenResponse(
                "fakeToken123", "", ""
            )
        }

}