package watch.gnag.website

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["watch.gnag.website", "com.detroitlabs"])
class WebsiteApplication

fun main(args: Array<String>) {
	runApplication<WebsiteApplication>(*args)
}
