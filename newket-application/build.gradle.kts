dependencies {
    implementation(project(":newket-core"))
    implementation(project(":newket-domain"))
    implementation(project(":newket-client"))
    implementation(project(":newket-infra"))

    //jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // webclient
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    //s3
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")
}

tasks {
    bootJar {
        isEnabled = false
    }
    jar {
        isEnabled = true
    }
}