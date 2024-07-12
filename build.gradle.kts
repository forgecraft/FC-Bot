plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.0"
    idea
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

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
}

