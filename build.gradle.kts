plugins {
    kotlin("jvm") version "1.5.10"
    java
    `java-library`
    `maven-publish`
}

group = "com.github.zimolab"
version = "0.1.2-SNAPSHOT"

val groupIdDef = group.toString()
val versionIdDef = version.toString()
val artifactIdDef = "js-array"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = groupIdDef
            version = versionIdDef
            artifactId = artifactIdDef
            from(components["kotlin"])
        }
    }
}