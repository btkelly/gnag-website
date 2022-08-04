package watch.gnag.website.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gitHubApp")
data class GitHubAppProperties(
    val secret: String,
    val id: String
)