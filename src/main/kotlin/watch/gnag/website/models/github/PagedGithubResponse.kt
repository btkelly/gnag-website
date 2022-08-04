package watch.gnag.website.models.github

data class PagedGithubResponse<T>(
    val pageLinks: PageLinks,
    val data: T
)