import com.google.cloud.tools.jib.gradle.JibExtension
import no.tornado.fxlauncher.gradle.FXLauncherExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.3.41"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", kotlin_version))
        classpath("com.github.jengelman.gradle.plugins:shadow:4.0.3")
        classpath("no.tornado:fxlauncher-gradle-plugin:1.0.20")
    }
}

plugins {
    java
    application
    id("com.google.cloud.tools.jib") version "1.7.0"
}

application {
    mainClassName = "com.akazlou.dynoman.DynomanApp"
}

group = "dynoman"
version = "1.0.0-SNAPSHOT"

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
    compile(kotlin("stdlib-jdk8", kotlin_version))
    compile(kotlin("reflect", kotlin_version))
    compile("com.amazonaws", "aws-java-sdk-dynamodb", "1.11.609")
    compile("com.github.ben-manes.caffeine", "caffeine", "2.8.0")
    compile("no.tornado", "tornadofx", "1.7.19")
    compile("org.reflections", "reflections", "0.9.11")
    compile("com.squareup.okhttp3", "okhttp", "4.2.0")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.0")
    testCompile("org.testcontainers:testcontainers:1.12.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

configure<FXLauncherExtension> {
    applicationVendor = "Aliaksandr Kazlou (aliaksandr.kazlou@gmail.com)"
    applicationUrl = "http://com.akazlou.dynoman.s3-website-us-west-2.amazonaws.com"
    applicationMainClass = "com.akazlou.dynoman.DynomanApp"
    acceptDowngrade = false
}

configure<JibExtension> {
    from {
        image = "amazoncorretto:8u232"
    }
    to {
        image = "dynoman:latest"
        tags = setOf("amazoncorretto", "amazoncorretto-8u232")
    }
    container {
        mainClass = "com.akazlou.dynoman.DynomanApp"
        creationTime = "USE_CURRENT_TIMESTAMP"
    }
}

tasks.getting(Jar::class) {
    manifest {
        attributes(mapOf("Main-Class" to "com.akazlou.dynoman.DynomanApp"))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}