plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.0"
    idea
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
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
//        archiveClassifier.set("")
        manifest {
            attributes(
                "Main-Class" to "fcdiscord.server.Main"
            )
        }
    }
}

