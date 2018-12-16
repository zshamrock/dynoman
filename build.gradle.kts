import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.application
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.java
import org.gradle.kotlin.dsl.repositories
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.testing.Test

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.3.11"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", kotlin_version))
        classpath("com.github.jengelman.gradle.plugins:shadow:2.0.4")
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
}

val kotlin_version: String by extra

repositories {
    mavenCentral()
}

// TODO: Upgrade all dependencies
dependencies {
    compile(kotlin("stdlib-jdk8", kotlin_version))
    compile("no.tornado", "tornadofx", "1.7.16")
    compile("com.amazonaws", "aws-java-sdk-dynamodb", "1.11.461")
    compile("org.jetbrains.kotlin", "kotlin-reflect", kotlin_version)
    compile("org.reflections", "reflections", "0.9.11")
    testCompile("io.kotlintest:kotlintest-runner-junit5:3.1.7")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.getByName<Jar>("jar") {
    manifest {
        attributes(mapOf("Main-Class" to "com.akazlou.dynoman.DynomanApp"))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}