package watch.gnag.website.webClient

import com.detroitlabs.middleware.core.webclient.customObjectMapper
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import watch.gnag.website.models.github.PageLinks
import watch.gnag.website.models.github.PagedGithubResponse
import watch.gnag.website.models.github.Release
import watch.gnag.website.models.github.Repo

@Component
class GitHubAPIClient(
    webclientBuilder: WebClient.Builder,
    objectMapper: ObjectMapper
) {

    companion object {
        private const val GITHUB_BASE_URL = "https://api.github.com"
    }

    private val webClient = webclientBuilder
        .baseUrl(GITHUB_BASE_URL)
        .customObjectMapper(
            objectMapper.copy()
                .apply {
                    propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
        )
        .defaultHeader("Accept", "application/json")
        .build()

    fun getUserRepos(accessToken: String, page: Int = 1) = webClient.get()
        .uri {
            it.pathSegment("user")
                .pathSegment("repos")
                .queryParam("per_page", "30")
                .queryParam("page", page.toString())
                .build()
        }
        .header("Authorization", "token $accessToken")
        .exchangeToMono { response ->
            if (response.statusCode().is2xxSuccessful) {
                response.bodyToMono<List<Repo>>()
                    .map { repos ->
                        val pageLinks = PageLinks(response.headers())
                        PagedGithubResponse<List<Repo>>(
                            pageLinks,
                            repos
                        )
                    }
            } else {
                response.createException().map { throw it }
            }
        }

    fun getLatestRepoRelease(owner: String, repoName: String) = webClient.get()
        .uri {
            it.pathSegment("repos")
                .pathSegment("{owner}")
                .pathSegment("{repoName}")
                .pathSegment("releases")
                .pathSegment("latest")
                .build(owner, repoName)
        }
        .retrieve()
        .bodyToMono<Release>()
}
