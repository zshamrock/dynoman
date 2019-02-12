import no.tornado.fxlauncher.gradle.FXLauncherExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.3.21"

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
    compile("no.tornado", "tornadofx", "1.7.18")
    compile("com.amazonaws", "aws-java-sdk-dynamodb", "1.11.495")
    compile("org.jetbrains.kotlin", "kotlin-reflect", kotlin_version)
    compile("org.reflections", "reflections", "0.9.11")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.11")
    testCompile("org.testcontainers:testcontainers:1.10.4")
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

tasks.getting(Jar::class) {
    manifest {
        attributes(mapOf("Main-Class" to "com.akazlou.dynoman.DynomanApp"))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.getting(Test::class) {
    useJUnitPlatform()
}