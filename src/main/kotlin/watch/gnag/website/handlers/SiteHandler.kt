package watch.gnag.website.handlers

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import watch.gnag.website.utils.SessionUtil
import java.net.URI

@Component
class SiteHandler {

    fun index(request: ServerRequest) = SessionUtil.clearSession(request.session())
        .then(
            ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("index")
        )

    fun configHelper(request: ServerRequest) = SessionUtil.getTokenFromSession(request.session())
        .flatMap {
            ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("confighelper")
        }
        .switchIfEmpty(ServerResponse.temporaryRedirect(URI("/startAuth")).build())
}
