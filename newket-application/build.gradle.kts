dependencies {
    implementation(project(":newket-core"))
    implementation(project(":newket-domain"))
    implementation(project(":newket-client"))
    implementation(project(":newket-infra"))

    //jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}

tasks {
    bootJar {
        isEnabled = false
    }
    jar {
        isEnabled = true
    }
}