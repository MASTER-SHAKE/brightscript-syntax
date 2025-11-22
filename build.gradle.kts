plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.maximpietukhov"
version = "1.2.2"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
intellij {
    version.set("2023.2.5")
    type.set("IU") // Target IDE Platform: IU for IntelliJ IDEA Ultimate

    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("243.*")

        // Extract the <!-- Plugin description --> section from README
        pluginDescription.set("""
            BrightScript syntax highlighting support for WebStorm and other JetBrains IDEs.

            Features:
            <ul>
                <li>Syntax highlighting for BrightScript (.brs) files</li>
                <li>Keywords, operators, and literals recognition</li>
                <li>Comments and string highlighting</li>
                <li>Support for BrightScript language constructs</li>
            </ul>
        """.trimIndent())

        changeNotes.set("""
            <h3>1.0.0</h3>
            <ul>
                <li>Initial release</li>
                <li>Basic syntax highlighting for BrightScript</li>
                <li>Support for .brs file extension</li>
            </ul>
        """.trimIndent())
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
