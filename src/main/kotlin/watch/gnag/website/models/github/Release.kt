package watch.gnag.website.models.github

data class Release(
    val id: Long,
    val url: String,
    val tagName: String,
    val targetCommitish: String,
    val name: String
) {

    fun getVersionNumber() = tagName.replace("v", "")
}
