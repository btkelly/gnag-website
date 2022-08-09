package watch.gnag.website.models.github

import java.net.URL

data class PageLink(val pageUrl: String, val rel: String) {

    fun getPageNum(): Int {
        val query = URL(pageUrl)
            .query
            .split("&".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()

        var pageNum = "1"

        for (param in query) {
            if (param.contains("page")) {
                val index = param.indexOf("=")
                pageNum = param.substring(index + 1)
            }
        }

        return try {
            pageNum.toInt()
        } catch (e: NumberFormatException) {
            1
        }
    }
}
