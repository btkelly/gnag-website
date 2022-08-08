import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import java.io.ByteArrayOutputStream

plugins {
	id("org.springframework.boot") version "2.7.2"
	id("io.spring.dependency-management") version "1.0.12.RELEASE"
	id("com.btkelly.gnag") version "3.0.3"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}

val imageRepo = "gcr.io"
val gcpProjectName = "gnag"
val versionNumber = "0.0.1"

group = "watch.gnag"
version = if (project.hasProperty("release")) versionNumber else "$versionNumber-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
	maven {
		url = uri("https://artifactory.detroitlabs.com/artifactory/detroit-labs-release")
		credentials {
			username = properties["artifactory_user"] as String
			password = properties["artifactory_password"] as String
		}
	}
}

gnag {
	github {
		repoName("btkelly/gnag-website")
		authToken("0000000000000")
		issueNumber("1")
	}
}

dependencies {
	implementation("com.detroitlabs.labscloud:core:4.4.1")
	implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.springframework.session:spring-session-core")

	// Required for running on the M1 Apple Chips
	runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.75.Final:osx-aarch_64")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<BootBuildImage> {
	environment = mapOf()
	imageName = "${imageRepo}/${gcpProjectName}/${project.name}:${fetchGitHash()}"
	tags = listOf("${imageRepo}/${gcpProjectName}/${project.name}:latest")
}

fun fetchGitHash(): String {
	val stdout = ByteArrayOutputStream()

	project.exec {
		commandLine = listOf("git", "rev-parse", "--short", "HEAD")
		standardOutput = stdout
	}

	return stdout.toString().trim()
}