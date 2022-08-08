package watch.gnag.website.models.github

import org.springframework.web.reactive.function.client.ClientResponse
import java.net.MalformedURLException

@Suppress("LoopWithTooManyJumpStatements", "multiline-if-else")
class PageLinks(headers: ClientResponse.Headers) {

    companion object {
        private const val DELIM_LINKS = ","
        private const val DELIM_LINK_PARAM = ";"
        private const val HEADER_LINK = "Link"
        private const val META_REL = "rel"
        private const val META_LAST = "last"
        private const val META_NEXT = "next"
        private const val META_FIRST = "first"
        private const val META_PREV = "prev"
    }

    var first: PageLink? = null
    var last: PageLink? = null
    var next: PageLink? = null
    var prev: PageLink? = null

    init {
        val linkHeader = headers.header(HEADER_LINK).firstOrNull()
        linkHeader?.let {

            val links = linkHeader.split(DELIM_LINKS.toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()

            for (link in links) {
                val segments = link.split(DELIM_LINK_PARAM.toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()

                if (segments.size < 2) continue

                var linkPart = segments[0].trim { it <= ' ' }

                if (!linkPart.startsWith("<") || !linkPart.endsWith(">"))
                    continue

                linkPart = linkPart.substring(1, linkPart.length - 1)

                for (i in 1 until segments.size) {

                    val rel = segments[i].trim { it <= ' ' }
                        .split("=".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()

                    if (rel.size < 2 || META_REL != rel[0]) continue

                    var relValue = rel[1]

                    if (relValue.startsWith("\"") && relValue.endsWith("\""))
                        relValue = relValue.substring(1, relValue.length - 1)

                    try {
                        if (META_FIRST == relValue) first =
                            PageLink(linkPart, relValue) else if (META_LAST == relValue) last =
                            PageLink(linkPart, relValue) else if (META_NEXT == relValue) next =
                            PageLink(linkPart, relValue) else if (META_PREV == relValue) prev =
                            PageLink(linkPart, relValue)
                    } catch (e: MalformedURLException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
