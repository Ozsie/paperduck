plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.spring") version "2.3.21"
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.diffplug.spotless") version "7.0.2"
}

group = "se.djupfeldt"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

spotless {
	kotlin {
		ktlint()
			.setEditorConfigPath("$projectDir/.editorconfig")
			.editorConfigOverride(
				mapOf(
					"ktlint_standard_no-wildcard-imports" to "disabled",
					"ktlint_standard_max-line-length" to "disabled",
					"ktlint_standard_property-naming" to "disabled",
					"ktlint_standard_discouraged-comment-location" to "disabled",
					"ktlint_standard_value-parameter-comment" to "disabled",
					"ktlint_standard_filename" to "disabled",
					"ktlint_standard_value-argument-comment" to "disabled",
				)
			)
	}
}

repositories {
	mavenCentral()
}

extra["springAiVersion"] = "2.0.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.ai:spring-ai-starter-model-openai")
	implementation("tools.jackson.module:jackson-module-kotlin")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
