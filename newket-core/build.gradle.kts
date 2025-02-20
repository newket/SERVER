dependencies {
    //jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // security
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation ("org.bouncycastle:bcpkix-jdk15on:1.70")
}

tasks {
    bootJar {
        isEnabled = false
    }
    jar {
        isEnabled = true
    }
}