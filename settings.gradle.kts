plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "backend"
include("newket-api")
include("newket-application")
include("newket-core")
include("newket-domain")
include("newket-client")
include("newket-scheduler")
include("newket-infra")
