import java.net.URI
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
    idea
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
    `maven-publish`
    id("me.modmuss50.mod-publish-plugin") version "1.1.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

version = project.properties["version"] as String

sourceSets {
    main {
        java {
            srcDir("src-server/main/java")
            srcDir("src/main/java")
        }
        resources {
            srcDir("src-server/main/resources")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.javacord:javacord:3.8.0")
    implementation("org.tomlj:tomlj:1.1.1")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.apache.logging.log4j:log4j-api:2.25.4")
    implementation("org.apache.logging.log4j:log4j-core:2.25.4")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.25.4")
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

idea {
    project {
        settings {
            runConfigurations {
                create<Application>("Debug App") {
                    mainClass = "fcdiscord.server.Main"
                    moduleName = idea.module.name + ".main"
                    jvmArgs = "-Dlog4j.configurationFile=log4j2.xml -Dorg.apache.logging.log4j.simplelog.StatusLogger.level=DEBUG"
                    workingDirectory = projectDir.absolutePath
                }
            }
        }
    }
}
