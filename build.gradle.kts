import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.application
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.java
import org.gradle.kotlin.dsl.kotlinModule
import org.gradle.kotlin.dsl.repositories
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.testing.Test

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.2.51"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlinModule("gradle-plugin", kotlin_version))
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
}

val kotlin_version: String by extra

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlinModule("stdlib-jdk8", kotlin_version))
    compile("no.tornado", "tornadofx", "1.7.16")
    compile("com.amazonaws", "aws-java-sdk-dynamodb", "1.11.330")
    testCompile("io.kotlintest:kotlintest-runner-junit5:3.1.7")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}