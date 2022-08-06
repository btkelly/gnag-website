package watch.gnag.website.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Validated
@ConfigurationProperties(prefix = "github")
class GitHubAppProperties {

    @NotBlank
    lateinit var secret: String

    @NotBlank
    lateinit var id: String

    var redirectUrl: String? = null

}