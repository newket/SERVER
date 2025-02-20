dependencies {
    implementation(project(":newket-core"))
    implementation(project(":newket-domain"))
    implementation(project(":newket-infra"))
    implementation(project(":newket-client"))

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