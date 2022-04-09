import java.util.Properties

plugins {
    kotlin("jvm") version "1.6.10"
    `maven-publish`
    signing
}

group = "dev.mslalith"
version = "0.2.0"

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
    testImplementation("app.cash.turbine:turbine:0.7.0")
    testImplementation("com.google.truth:truth:1.1.3")
}

object Meta {
    const val description = "Poller is a simple Kotlin library which runs a certain task at a regular interval"
    const val license = "Apache-2.0"
    const val githubRepo = "mslalith/poller"
    const val localUrl = "https://s01.oss.sonatype.org/service/local/"
    const val releaseUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val snapshotUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.get().kotlin)
}

val javadocJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Javadoc JAR"
    archiveClassifier.set("javadoc")
}

val secretPropsFile = project.rootProject.file("local.properties")
val properties = if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply { load(it) }
    }
} else null

fun getExtraString(name: String) = properties?.toMap()?.get(name)?.toString()

val sonatypeUsername: String? = getExtraString("ossrhUsername")
val sonatypePassword: String? = getExtraString("ossrhPassword")

signing {
    useGpgCmd()
    sign(publishing.publications)
}

publishing {
    repositories {
        maven {
            name = "poller"
            setUrl(if (version.toString().contains("SNAPSHOT")) Meta.snapshotUrl else Meta.releaseUrl)
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom {
                name.set(project.name)
                description.set(Meta.description)
                url.set("https://github.com/${Meta.githubRepo}")
                licenses {
                    license {
                        name.set(Meta.license)
                        url.set("https://github.com/${Meta.githubRepo}/blob/main/LICENSE.md")
                    }
                }
                developers {
                    developer {
                        id.set("mslalith")
                        name.set("M S Lalith")
                        email.set("santosh2397@gmail.com")
                        organization.set("M S Lalith")
                        organizationUrl.set("https://mslalith.dev")
                    }
                }
                scm {
                    url.set("https://github.com/${Meta.githubRepo}.git")
                    connection.set("scm:git:git://github.com/${Meta.githubRepo}.git")
                    developerConnection.set("scm:git:git://github.com/${Meta.githubRepo}.git")
                }
                issueManagement {
                    url.set("https://github.com/${Meta.githubRepo}/issues")
                }
            }
        }
    }
}
