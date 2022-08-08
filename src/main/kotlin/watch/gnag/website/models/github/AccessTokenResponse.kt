package watch.gnag.website.models.github

data class AccessTokenResponse(
    val accessToken: String,
    val scope: String,
    val tokenType: String
)
