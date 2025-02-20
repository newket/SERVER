dependencies {
    implementation(project(":newket-application"))
    implementation(project(":newket-client"))
    implementation(project(":newket-core"))
    implementation(project(":newket-domain"))
    implementation(project(":newket-infra"))
    implementation(project(":newket-scheduler"))

    //mongo
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

}
tasks {
    bootJar {
        isEnabled = true
    }
    jar {
        isEnabled = true
    }
}