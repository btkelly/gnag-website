package watch.gnag.website.routes

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import watch.gnag.website.handlers.SiteHandler

@Configuration
class SiteRouter(private val siteHandler: SiteHandler) {

    @Bean
    fun siteRoutes() = router {
        GET("/", siteHandler::index)
        GET("/configHelper", siteHandler::configHelper)
    }

}