package watch.gnag.website.handlers

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import watch.gnag.website.utils.SessionUtil
import watch.gnag.website.webClient.GitHubAPIClient
import watch.gnag.website.webClient.GitHubAuthClient
import java.net.URI

@Component
class GitHubHandler(
    private val gitHubAuthClient: GitHubAuthClient,
    private val gitHubAPIClient: GitHubAPIClient
    ) {

    fun startAuth(request: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.temporaryRedirect(gitHubAuthClient.getAuthenticationURI()).build()
    }

    fun authCallback(request: ServerRequest): Mono<ServerResponse> {
        val tempCode = request.queryParam("code").orElseGet { throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing required code parameter") }

        return gitHubAuthClient.getAccessToken(tempCode)
            .flatMap { accessTokenResponse -> SessionUtil.saveTokenToSession(request.session(), accessTokenResponse)}
            .then(ServerResponse.temporaryRedirect(URI("/configHelper")).build())
    }

    fun loadProjects(request: ServerRequest): Mono<ServerResponse> {
        return SessionUtil.getTokenFromSession(request.session())
            .flatMap { accessToken ->
                gitHubAPIClient.getUserRepos(accessToken)
                    .flatMapMany { pagedResponse ->
                        val firstPage = pagedResponse.pageLinks.next?.getPageNum() ?: 1
                        val lastPage = pagedResponse.pageLinks.last?.getPageNum() ?: 1
                        Flux.range(firstPage, lastPage)
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap { page -> gitHubAPIClient.getUserRepos(accessToken, page) }
                            .mergeWith(pagedResponse.toMono())
                    }
                    .map { pagedResponse -> pagedResponse.data }
                    .flatMapIterable { repoList -> repoList }
                    .collectList()
                    .map { repos -> repos.distinctBy { it.fullName }.sortedBy { it.fullName.lowercase() } }
                    .flatMap { ServerResponse.ok().bodyValue(it) }
            }
            .switchIfEmpty(ServerResponse.temporaryRedirect(URI("/startAuth")).build())
    }

    fun configForSlug(request: ServerRequest): Mono<ServerResponse> {
        val slug = request.queryParam("slug").orElseGet { throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing required slug parameter") }

        return SessionUtil.getTokenFromSession(request.session())
            .flatMap { accessToken ->
                gitHubAPIClient.getLatestRepoRelease("btkelly", "gnag")
                    .map { latestRelease -> latestRelease.getVersionNumber() }
                    .flatMap { latestVersion ->
                        ServerResponse.ok()
                            .contentType(MediaType.TEXT_HTML)
                            .render("gnagconfig", slug, accessToken, latestVersion)
                    }
            }
            .switchIfEmpty(ServerResponse.temporaryRedirect(URI("/startAuth")).build())
    }
}