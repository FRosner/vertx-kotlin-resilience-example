import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.21"
}

group = "de.frosner"
version = "1.0-SNAPSHOT"

val vertxVersion = "3.6.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    compile(group = "io.vertx", name = "vertx-core", version = vertxVersion)
    compile(group = "io.vertx", name = "vertx-web", version = vertxVersion)
    compile(group = "io.vertx", name = "vertx-web-client", version = vertxVersion)
    compile(group = "io.vertx", name = "vertx-circuit-breaker", version = vertxVersion)
    compile(group = "io.vertx", name = "vertx-lang-kotlin", version = vertxVersion)
    compile(group = "io.vertx", name = "vertx-lang-kotlin-coroutines", version = vertxVersion)

    testCompile(group = "junit", name = "junit", version = "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
