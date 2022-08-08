package watch.gnag.website.routes

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router
import watch.gnag.website.handlers.GitHubHandler

@Configuration
class GitHubRouter(private val gitHubHandler: GitHubHandler) {

    @Bean
    fun githubRoutes() = router {
        GET("/startAuth") { gitHubHandler.startAuth() }
        GET("/callback", gitHubHandler::authCallback)
        GET("/loadProjects", gitHubHandler::loadProjects)
        GET("/configForSlug", gitHubHandler::configForSlug)
    }
}
