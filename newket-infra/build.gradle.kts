dependencies {
    implementation(project(":newket-core"))

    //jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    //mySQL
    runtimeOnly("com.mysql:mysql-connector-j")

    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
}
tasks {
    bootJar {
        isEnabled = false
    }
    jar {
        isEnabled = true
    }
}