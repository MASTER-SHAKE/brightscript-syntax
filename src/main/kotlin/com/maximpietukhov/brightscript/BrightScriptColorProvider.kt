package com.maximpietukhov.brightscript

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import java.awt.Color

class BrightScriptColorProvider : ElementColorProvider {

    override fun getColorFrom(element: PsiElement): Color? {
        if (element.firstChild != null) return null

        val elementType = element.node.elementType
        val text = element.text

        // BrightScript: &hRRGGBB or &hRRGGBBAA
        if (elementType == BrightScriptTokenTypes.NUMBER_LITERAL && text.startsWith("&h", ignoreCase = true)) {
            return parseHexLiteral(text.substring(2))
        }

        // BrightScript strings: "#RRGGBB", "#RRGGBBAA", "0xRRGGBB", "0xRRGGBBAA"
        if (elementType == BrightScriptTokenTypes.STRING_LITERAL && text.startsWith("\"") && text.endsWith("\"")) {
            return parseStringColor(text.substring(1, text.length - 1))
        }

        // XML attribute values: backgroundColor="0x551A8B", color="#FF0000"
        if (elementType.toString() == "XML_ATTRIBUTE_VALUE_TOKEN") {
            return parseStringColor(text)
        }

        return null
    }

    private fun parseStringColor(value: String): Color? {
        if (value.startsWith("#")) {
            return parseHexLiteral(value.substring(1))
        }
        if (value.startsWith("0x", ignoreCase = true)) {
            return parseHexLiteral(value.substring(2))
        }
        return null
    }

    private fun parseHexLiteral(hexPart: String): Color? {
        return when (hexPart.length) {
            // RRGGBB
            6 -> {
                try {
                    val value = hexPart.toLong(16).toInt()
                    val r = (value shr 16) and 0xFF
                    val g = (value shr 8) and 0xFF
                    val b = value and 0xFF
                    Color(r, g, b)
                } catch (e: NumberFormatException) {
                    null
                }
            }
            // RRGGBBAA
            8 -> {
                try {
                    val value = hexPart.toLong(16)
                    val r = ((value shr 24) and 0xFF).toInt()
                    val g = ((value shr 16) and 0xFF).toInt()
                    val b = ((value shr 8) and 0xFF).toInt()
                    val a = (value and 0xFF).toInt()
                    Color(r, g, b, a)
                } catch (e: NumberFormatException) {
                    null
                }
            }
            else -> null
        }
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        if (!element.isValid) return

        val project = element.project
        val document = PsiDocumentManager.getInstance(project)
            .getDocument(element.containingFile) ?: return

        val originalText = element.text
        val elementType = element.node.elementType
        val startOffset = element.textRange.startOffset
        val endOffset = element.textRange.endOffset

        val newText = when {
            // &hRRGGBB / &hRRGGBBAA
            elementType == BrightScriptTokenTypes.NUMBER_LITERAL -> {
                val hexPart = originalText.substring(2)
                val upper = hexPart.any { it in 'A'..'F' }
                val hadAlpha = hexPart.length == 8
                formatHexColor(color, "&h", upper, hadAlpha)
            }
            // BrightScript string: "#RRGGBB"
            originalText.startsWith("\"#") -> {
                val inner = originalText.substring(2, originalText.length - 1)
                val upper = inner.any { it in 'A'..'F' }
                val hadAlpha = inner.length == 8
                "\"" + formatHexColor(color, "#", upper, hadAlpha) + "\""
            }
            // BrightScript string: "0xRRGGBB"
            originalText.startsWith("\"0x", ignoreCase = true) -> {
                val prefix = originalText.substring(1, 3)
                val inner = originalText.substring(3, originalText.length - 1)
                val upper = inner.any { it in 'A'..'F' }
                val hadAlpha = inner.length == 8
                "\"" + formatHexColor(color, prefix, upper, hadAlpha) + "\""
            }
            // XML attribute value: 0xRRGGBB or #RRGGBB (no quotes)
            elementType.toString() == "XML_ATTRIBUTE_VALUE_TOKEN" -> {
                val prefix: String
                val hexPart: String
                if (originalText.startsWith("#")) {
                    prefix = "#"
                    hexPart = originalText.substring(1)
                } else if (originalText.startsWith("0x", ignoreCase = true)) {
                    prefix = originalText.substring(0, 2)
                    hexPart = originalText.substring(2)
                } else return
                val upper = hexPart.any { it in 'A'..'F' }
                val hadAlpha = hexPart.length == 8
                formatHexColor(color, prefix, upper, hadAlpha)
            }
            else -> return
        }

        CommandProcessor.getInstance().executeCommand(
            project,
            {
                document.replaceString(startOffset, endOffset, newText)
                PsiDocumentManager.getInstance(project).commitDocument(document)
            },
            "Change Color",
            null,
            document
        )
    }

    private fun formatHexColor(color: Color, prefix: String, upperCase: Boolean, withAlpha: Boolean): String {
        val fmt = if (upperCase) "%02X" else "%02x"
        val hex = if (withAlpha || color.alpha != 255) {
            String.format("$fmt$fmt$fmt$fmt", color.red, color.green, color.blue, color.alpha)
        } else {
            String.format("$fmt$fmt$fmt", color.red, color.green, color.blue)
        }
        return "$prefix$hex"
    }
}
