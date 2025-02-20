import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("java-test-fixtures")
	id("org.springframework.boot") version "3.2.4"
	id("io.spring.dependency-management") version "1.1.4"

	kotlin("jvm") version "1.9.23"
	kotlin("kapt") version "1.9.23"
	kotlin("plugin.spring") version "1.9.23"
	kotlin("plugin.jpa") version "1.9.23"


	id("org.hidetake.swagger.generator") version "2.18.2"
	id("com.epages.restdocs-api-spec") version "0.18.2"
}

val kotestVersion = "5.8.1"
val mockkVersion = "1.13.10"

allprojects {
	group = "com.newket"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}

	tasks.withType<BootJar> {
		enabled=false
	}
}

subprojects {
	apply(plugin = "kotlin")
	apply(plugin = "kotlin-kapt")
	apply(plugin = "kotlin-spring")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")
	apply(plugin = "java-test-fixtures")



	tasks.withType<JavaCompile> {
		sourceCompatibility = "17"
		targetCompatibility = "17"
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs += "-Xjsr305=strict"
			jvmTarget = "17"
		}
	}

	kapt {
		keepJavacAnnotationProcessors = true
	}

	dependencies {
		// kotlin
		implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
		implementation("org.jetbrains.kotlin:kotlin-reflect")

		//spring
		implementation("org.springframework.boot:spring-boot-starter-web")

		//test
		testImplementation("org.springframework.boot:spring-boot-starter-test")
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

}