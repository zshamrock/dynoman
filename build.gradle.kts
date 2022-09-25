import com.google.cloud.tools.jib.gradle.JibExtension
import no.tornado.fxlauncher.gradle.FXLauncherExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.7.10"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", kotlin_version))
        classpath("com.github.jengelman.gradle.plugins:shadow:4.0.3")
        classpath("no.tornado:fxlauncher-gradle-plugin:1.0.21")
    }
}

plugins {
    java
    application
    id("com.google.cloud.tools.jib") version "3.2.0"
}

application {
    mainClass.set("com.akazlou.dynoman.DynomanApp")
}

group = "dynoman"
version = "1.0.0-SNAPSHOT"

project.setProperty("mainClassName", application.mainClass.get())

apply {
    plugin("kotlin")
    plugin("com.github.johnrengelman.shadow")
    plugin("no.tornado.fxlauncher")
}

val kotlin_version: String by extra

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8", kotlin_version))
    implementation(kotlin("reflect", kotlin_version))
    implementation("com.amazonaws:aws-java-sdk-dynamodb:1.12.310")
    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3")
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.reflections:reflections:0.10.2")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.controlsfx:controlsfx:8.40.18")
    implementation("org.partiql:partiql-lang-kotlin:0.6.0")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("org.testcontainers:testcontainers:1.17.3")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

configure<FXLauncherExtension> {
    applicationVendor = "Aliaksandr Kazlou (aliaksandr.kazlou@gmail.com)"
    applicationUrl = "http://com.akazlou.dynoman.s3-website-us-west-2.amazonaws.com"
    applicationMainClass = application.mainClass.get()
    applicationVersion = "1.0.0"
    applicationTitle = "DynamoDB Manager"
    applicationName = "dynoman"
    cacheDir = "deps"
    acceptDowngrade = false
}

// docker run --net host -v /tmp/.X11-unix:/tmp/.X11-unix -v $HOME/.Xauthority:/root/.Xauthority -e DISPLAY=unix$DISPLAY dynoman
configure<JibExtension> {
    from {
        image = "amazoncorretto:8u252"
    }
    to {
        image = "dynoman:latest"
        tags = setOf("amazoncorretto", "amazoncorretto-8u252")
    }
    container {
        mainClass = application.mainClass.get()
        creationTime = "USE_CURRENT_TIMESTAMP"
        jvmFlags = listOf(
            "-Djdk.gtk.verbose=true",
            "-Djavafx.verbose=true"
        )
    }
}

tasks.getting(Jar::class) {
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass.get()))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}