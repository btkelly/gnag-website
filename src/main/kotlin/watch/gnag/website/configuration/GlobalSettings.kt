package watch.gnag.website.configuration

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GlobalSettings {

    @Bean
    fun layoutDialect() = LayoutDialect()
}
