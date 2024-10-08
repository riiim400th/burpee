import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.20-Beta2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.2.6"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.poi:poi-ooxml:5.3.0")
    implementation("net.portswigger.burp.extensions:montoya-api:2024.7")
}

tasks.test {
    useJUnitPlatform()
}


tasks.withType<KotlinCompile>().configureEach{
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}