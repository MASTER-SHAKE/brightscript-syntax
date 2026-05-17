plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.maximpietukhov"
version = "1.3.1"

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
        untilBuild.set("261.*")

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
            <h3>1.3.1</h3>
            <ul>
                <li>Extended IDE compatibility up to 2026.1 (build 261.*)</li>
            </ul>
            <h3>1.3.0</h3>
            <ul>
                <li>Added BrighterScript (.bs) file support</li>
                <li>Extended IDE compatibility up to 2025.1</li>
                <li>Fixed color provider crash on IDEs without XML plugin</li>
                <li>Added 'void' type keyword highlighting</li>
                <li>Fixed lexer token classification for overlapping keyword/type/builtin sets</li>
            </ul>
            <h3>1.2.9</h3>
            <ul>
                <li>Color preview in gutter - hex color literals (&amp;hRRGGBB, &amp;hRRGGBBAA) now show a colored square in the editor gutter with built-in color picker</li>
            </ul>
            <h3>1.2.8</h3>
            <ul>
                <li>Fixed semicolon (;) incorrectly marked as error - now properly recognized for print concatenation</li>
                <li>Fixed number type suffixes (0!, 1.5#, 10%, 100&amp;) incorrectly marked as error</li>
                <li>Added support for exponent notation (1.5e10, 2.0d-5)</li>
                <li>Added ? as shorthand for print keyword</li>
            </ul>
            <h3>1.2.7</h3>
            <ul>
                <li>Syntax error highlighting - underlines unclosed strings, unmatched brackets, and unknown characters</li>
                <li>Fixed Smart Enter for nested blocks - now correctly adds closing tags for nested if/for/while inside parent blocks</li>
                <li>Smart Enter now adds proper body indentation (4 spaces) when auto-closing blocks</li>
            </ul>
            <h3>1.2.6</h3>
            <ul>
                <li>Improved code completion - now suggests identifiers from current file</li>
                <li>Context-aware member completion - suggests only relevant members based on the object chain</li>
            </ul>
            <h3>1.2.5</h3>
            <ul>
                <li>Fixed Smart Enter - No longer inserts duplicate closing tags when block already has matching end tag</li>
            </ul>
            <h3>1.2.4</h3>
            <ul>
                <li>Quick comment/uncomment with Ctrl+/ - Toggle line comments using the apostrophe (') prefix</li>
                <li>Smart Enter - Auto-insert closing tags when pressing Enter after function, sub, if, for, while, class, namespace, try</li>
                <li>Code completion - Keywords, types (after 'as'), and built-in functions with auto-parentheses</li>
                <li>Code formatting - Automatic indentation for function, sub, if, for, while, class, namespace, try blocks</li>
            </ul>
            <h3>1.2.3</h3>
            <ul>
                <li>Function call highlighting</li>
                <li>Go to Declaration - Ctrl+Click on function calls to navigate to definition</li>
            </ul>
            <h3>1.2.2</h3>
            <ul>
                <li>REM comment highlighting - REM comments now highlighted in yellow</li>
                <li>Double-apostrophe comments - '' comments also highlighted in yellow for special/doc comments</li>
                <li>Function/class declaration highlighting - Names after function, sub, and class keywords are now highlighted distinctly</li>
                <li>Added "REM Comment" and "Function/Class Declaration" to Color Settings page</li>
            </ul>
            <h3>1.2.1</h3>
            <ul>
                <li>Initial release</li>
                <li>Basic syntax highlighting for BrightScript</li>
                <li>Support for .brs file extension</li>
                <li>Keyword highlighting</li>
                <li>String and number literals support</li>
                <li>Comment highlighting</li>
                <li>Code folding support</li>
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
