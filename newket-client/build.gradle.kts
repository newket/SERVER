dependencies {
    implementation(project(":newket-core"))
    implementation(project(":newket-domain"))
    implementation(project(":newket-infra"))

    //fcm
    implementation("com.google.firebase:firebase-admin:9.2.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.16.1")
    implementation("com.google.guava:guava:32.0.1-jre")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // webclient
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    //slack
    implementation("com.slack.api:bolt:1.18.0")
    implementation("com.slack.api:bolt-servlet:1.18.0")
    implementation("com.slack.api:bolt-jetty:1.18.0")

    //Jsoup
    implementation("org.jsoup:jsoup:1.18.3")

    //kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    //playwright
    implementation("com.microsoft.playwright:playwright:1.42.0")

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