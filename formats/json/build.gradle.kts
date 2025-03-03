plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
}

description = "JSON format support for xpdy"
group = "io.github.wasabithumb"
version = "${rootProject.version}"

repositories {
    mavenCentral()
}

val targetJavaVersion = 17
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(rootProject)
    implementation("org.jetbrains:annotations:26.0.2")
    implementation("com.google.code.gson:gson:2.12.1")
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    (options as CoreJavadocOptions)
        .addBooleanOption("Xdoclint:none", true)
}

centralPortal {
    name = "xpdy-format-json"
    jarTask = tasks.jar
    sourcesJarTask = tasks.sourcesJar
    javadocJarTask = tasks.javadocJar
    pom {
        name = "xpdy-format-json"
        description = project.description
        url = "https://github.com/WasabiThumb/xpdy"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "wasabithumb"
                email = "wasabithumbs@gmail.com"
                organization = "Wasabi Codes"
                organizationUrl = "https://wasabithumb.github.io/"
                timezone = "-5"
            }
        }
        scm {
            connection = "scm:git:git://github.com/WasabiThumb/xpdy.git"
            url = "https://github.com/WasabiThumb/xpdy"
        }
    }
}