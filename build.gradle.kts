import java.time.Instant

plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
}

description = "Simple annotation-powered web app engine for Java"
group = "io.github.wasabithumb"
version = "0.2.0"

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
    implementation("org.jetbrains:annotations:26.0.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val metaFileName = "BUILD.txt"
    val metaFile = this.project.layout.buildDirectory.file("tmp/processResources/$metaFileName").get().asFile

    into("META-INF/xpdy") {
        from(metaFile.absolutePath)
    }

    doFirst {
        metaFile.parentFile.mkdirs()
        metaFile.writer(Charsets.UTF_8).use { writer ->
            writer.write("Version: ${project.version}\n")
            writer.write("Build-JDK: ${System.getProperty("java.version")}\n")
            writer.write("Build-Timestamp: ${Instant.now()}\n")
            writer.flush()
        }
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    (options as CoreJavadocOptions)
        .addBooleanOption("Xdoclint:none", true)
}

centralPortal {
    name = "xpdy"
    jarTask = tasks.jar
    sourcesJarTask = tasks.sourcesJar
    javadocJarTask = tasks.javadocJar
    pom {
        name = "xpdy"
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
