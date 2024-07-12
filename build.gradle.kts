import java.net.URI

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.0"
    idea
    `maven-publish`
    id("me.modmuss50.mod-publish-plugin") version "0.5.2"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

version = project.properties["version"] as String

sourceSets {
    main {
        java {
            srcDir("src-server/main/java")
            srcDir("src/main/java")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.javacord:javacord:3.8.0")
    implementation("org.tomlj:tomlj:1.1.1")
    implementation("com.grack:nanojson:1.9")
}

tasks {
    shadowJar {
        manifest {
            attributes(
                "Main-Class" to "fcdiscord.server.Main"
            )
        }
    }

    build {
        dependsOn(shadowJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "net.forgecraft.services"
            from(components["java"])
        }
    }
    repositories {
        val token = providers.environmentVariable("GITHUB_TOKEN")
        if (token.isPresent) {
            maven {
                name = "GitHubPackages"
                url = URI("https://maven.pkg.github.com/forgecraft/FC-Bot")
                credentials {
                    username = providers.environmentVariable("GITHUB_ACTOR").get()
                    password = token.get()
                }
            }
        } else {
            println("GitHub token not found, skipping GitHub Packages repository")
        }

        val sapsToken = providers.environmentVariable("SAPS_TOKEN")
        if (sapsToken.isPresent) {
            maven {
                name = "MikeysSapsMavenRepository"
                url = URI("https://maven.saps.dev/releases")
                credentials {
                    username = "forgecraft"
                    password = sapsToken.get()
                }
            }
        } else {
            println("SAPS token not found, skipping SAPS repository")
        }
    }
}

publishMods {
    dryRun = providers.environmentVariable("GITHUB_TOKEN").getOrNull() == null
    file = project.provider { project.tasks.shadowJar }.flatMap { it.get().archiveFile }
    additionalFiles.from( project.provider { project.tasks.jar }.flatMap { it.get().archiveFile }  )
    changelog = ""
    type = STABLE

    github {
        repository = "forgecraft/FC-Bot"
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        commitish = providers.environmentVariable("GITHUB_SHA").orElse("dryRun")
        tagName = providers.environmentVariable("GITHUB_REF_NAME").orElse("dryRun")
    }
}
