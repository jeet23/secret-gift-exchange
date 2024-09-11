plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.3"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "com.tenable"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

sourceSets {
	main {
		java.srcDirs("build/generated/src/main/fabrikt")
	}
	test { java.srcDirs("$generationDir/src/test/kotlin") }
}


val fabrikt: Configuration by configurations.creating
val generationDir = "build/generated"
val apiFile = "$rootDir/openapi/api.yaml"
val fabriktSrcDir = "src/main/fabrikt"

dependencies {
	fabrikt("com.cjbooms:fabrikt:7.2.1")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.zalando:problem-spring-web-starter:0.27.0")

	// Dependencies required for Fabrikt
	implementation("com.squareup.okhttp3:okhttp:4.10.0")
	implementation("io.github.resilience4j:resilience4j-circuitbreaker:1.7.1")
	implementation("io.github.resilience4j:resilience4j-micrometer:1.7.1")
	implementation("io.github.resilience4j:resilience4j-retry:1.7.1")
	compileOnly("javax.validation:validation-api:2.0.1.Final")
	implementation("io.github.resilience4j:resilience4j-kotlin:2.2.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.mockk:mockk:1.12.0")  // Mocking framework
	testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")
}

tasks.withType<Test> {
	useJUnitPlatform()
}


tasks {
	val generateApiCode by creating(JavaExec::class) {
		inputs.file(apiFile)
		outputs.dir(generationDir)
		outputs.cacheIf { true }
		classpath(fabrikt)
		mainClass.set("com.cjbooms.fabrikt.cli.CodeGen")
		args =
			listOf(
				"--output-directory",
				generationDir,
				"--src-path",
				fabriktSrcDir,
				"--api-file",
				apiFile,
				"--base-package",
				"com.tenable.generated.fabrikt",
				"--http-model-opts",
				"X_EXTENSIBLE_ENUMS",
				"--targets",
				"HTTP_MODELS",
				"--targets",
				"CONTROLLERS",
			)
	}

	withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
		dependsOn(generateApiCode)
		kotlinOptions {
			jvmTarget = "21"
			incremental = true
			freeCompilerArgs = freeCompilerArgs + listOf("-Xjsr305=strict")
		}
	}
}