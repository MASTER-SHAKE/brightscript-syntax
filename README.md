# BrightScript Syntax

![Build](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.4.0-blue)
![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange)

BrightScript and BrighterScript language support for JetBrains IDEs (WebStorm, IntelliJ IDEA, etc.).

## Features

- **Syntax Highlighting**: for BrightScript (.brs) and BrighterScript (.bs) files, with customizable colors
- **Code Completion**: keywords, types after `as`, built-in functions, identifiers and member chains
- **Quick Documentation**: Ctrl+Q / hover shows signatures for built-in functions
- **Code Folding**: function, sub, if, for, while, class, namespace, try blocks with informative placeholders
- **Structure View**: Ctrl+F12 / Alt+7 lists functions and subs in the file
- **Go to Declaration**: Ctrl+Click on a function call jumps to its definition
- **Find Usages**: Alt+F7 on function and sub declarations
- **Code Formatting**: Ctrl+Alt+L with automatic block indentation
- **Smart Enter**: auto-inserts closing tags (end function, end if, ...) and continues line comments
- **Live Templates**: `func`, `sub`, `if`, `ife`, `for`, `fore`, `wh`, `try`, `pr`
- **Error Highlighting**: unclosed strings, unmatched brackets, unknown characters
- **Color Preview**: gutter swatch and color picker for `&h` hex color literals
- **Brace Matching**: highlights matching (), [], {}
- **Roku Flavour Selector**: status bar widget for projects with a `flavours/` folder

## Supported Language Features

- Keywords: `function`, `sub`, `if`, `else`, `for`, `while`, `end`, `return`, etc.
- Data types: `string`, `integer`, `float`, `boolean`, `object`, `array`
- Operators: `+`, `-`, `*`, `/`, `=`, `<>`, `<`, `>`, `<=`, `>=`, `and`, `or`, `not`
- Comments: `'` and `REM`
- Built-in constants: `true`, `false`, `invalid`

## Installation

### From JetBrains Marketplace (Recommended)

1. Open your JetBrains IDE (WebStorm, IntelliJ IDEA, etc.)
2. Go to `Settings/Preferences` → `Plugins`
3. Search for "BrightScript Syntax"
4. Click `Install`
5. Restart the IDE

### Manual Installation

1. Download the latest release from the [releases page](https://github.com/MASTER-SHAKE/brightscript-syntax/releases)
2. Open your JetBrains IDE
3. Go to `Settings/Preferences` → `Plugins` → ⚙️ → `Install Plugin from Disk...`
4. Select the downloaded `.zip` file
5. Restart the IDE

### Build from Source

```bash
# Clone the repository
git clone https://github.com/maximpietukhov/brightscript-syntax.git
cd brightscript-syntax

# Build the plugin
./gradlew buildPlugin

# The plugin will be in build/distributions/
```

## Usage

Once installed, the plugin will automatically recognize and highlight `.brs` files. You can customize the color scheme:

1. Go to `Settings/Preferences` → `Editor` → `Color Scheme` → `BrightScript`
2. Customize colors for keywords, strings, comments, etc.

## Development

This plugin is built using:
- Kotlin
- IntelliJ Platform SDK
- Gradle

### Project Structure

```
brightscript-syntax/
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── com/maximpietukhov/brightscript/
│       │       ├── BrightScriptLanguage.kt
│       │       ├── BrightScriptFileType.kt
│       │       ├── BrightScriptLexer.kt
│       │       ├── BrightScriptTokenTypes.kt
│       │       ├── BrightScriptSyntaxHighlighter.kt
│       │       ├── BrightScriptSyntaxHighlighterFactory.kt
│       │       ├── BrightScriptColorSettingsPage.kt
│       │       ├── BrightScriptParserDefinition.kt
│       │       ├── BrightScriptParser.kt
│       │       ├── BrightScriptElementTypes.kt
│       │       ├── BrightScriptPsiElement.kt
│       │       ├── BrightScriptFile.kt
│       │       ├── BrightScriptFoldingBuilder.kt
│       │       └── BrightScriptIcons.kt
│       └── resources/
│           ├── META-INF/
│           │   └── plugin.xml
│           └── icons/
│               └── brightscript.svg
├── build.gradle.kts
└── README.md
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License.

## Author

**Maxim Pietukhov 🧙‍♂️**

## Support

If you encounter any issues or have suggestions, please [open an issue](https://github.com/MASTER-SHAKE/brightscript-syntax/issues).

## Code Folding

The plugin supports code folding for better navigation through your BrightScript files:

### Supported Blocks
- **Functions and Subroutines**: `function...end function`, `sub...end sub`
- **Conditionals**: `if...end if` (multi-line blocks)
- **Loops**: `for...end for`, `while...end while`
- **Classes**: `class...end class`
- **Namespaces**: `namespace...end namespace`
- **Error Handling**: `try...end try`

### Settings
Make sure code folding is enabled in your IDE settings:
`Settings → Editor → General → Code Folding` → enable "Show code folding outline"

## Changelog

See the full version history in the plugin description on the
[JetBrains Marketplace](https://plugins.jetbrains.com/) or in `plugin.xml` change notes.

### 1.4.0 (latest)
- Structure View, Find Usages, live templates
- Automated test suite (`./gradlew test`)

### 1.3.5
- BrighterScript fixes: .bs files in Go to Declaration, annotations and template strings no longer flagged as errors
