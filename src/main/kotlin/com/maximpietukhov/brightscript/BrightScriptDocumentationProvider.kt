package com.maximpietukhov.brightscript

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

// Quick Documentation (Ctrl+Q / hover) for BrightScript built-in functions.
// Built-ins have no PSI references, so we hook the lookup at the leaf-token
// level via getCustomDocumentationElement.
class BrightScriptDocumentationProvider : AbstractDocumentationProvider() {

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int
    ): PsiElement? {
        if (lookup(contextElement) != null) return contextElement
        // Caret may sit just after the name end - probe the previous char.
        if (targetOffset > 0) {
            val prev = file.findElementAt(targetOffset - 1)
            if (lookup(prev) != null) return prev
        }
        return null
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val doc = lookup(element) ?: lookup(originalElement) ?: return null
        val sb = StringBuilder()
        sb.append(DocumentationMarkup.DEFINITION_START)
            .append(StringUtil.escapeXmlEntities(doc.signature))
            .append(DocumentationMarkup.DEFINITION_END)
        sb.append(DocumentationMarkup.CONTENT_START)
            .append(StringUtil.escapeXmlEntities(doc.description))
            .append(DocumentationMarkup.CONTENT_END)
        if (doc.returnType != null) {
            sb.append(DocumentationMarkup.SECTIONS_START)
                .append(DocumentationMarkup.SECTION_HEADER_START)
                .append("Returns:")
                .append(DocumentationMarkup.SECTION_SEPARATOR)
                .append(StringUtil.escapeXmlEntities(doc.returnType))
                .append(DocumentationMarkup.SECTION_END)
                .append(DocumentationMarkup.SECTIONS_END)
        }
        return sb.toString()
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        val doc = lookup(element) ?: lookup(originalElement) ?: return null
        return StringUtil.escapeXmlEntities(doc.signature)
    }

    private fun lookup(element: PsiElement?): BuiltinDoc? {
        if (element == null) return null
        if (element.firstChild != null) return null
        if (element.node.elementType != BrightScriptTokenTypes.BUILTIN_FUNCTION) return null
        return DOCS[element.text.lowercase()]
    }

    private data class BuiltinDoc(
        val signature: String,
        val description: String,
        val returnType: String?
    )

    companion object {
        // IMPORTANT: keys MUST match BrightScriptLexer.BUILTIN_FUNCTIONS (lowercase).
        // A name missing here just yields no popup (graceful). Keep in sync.
        private val DOCS: Map<String, BuiltinDoc> = mapOf(
            // Math
            "abs" to BuiltinDoc(
                "Abs(x as Float) as Float",
                "Returns the absolute value of x.", "Float"
            ),
            "atn" to BuiltinDoc(
                "Atn(x as Float) as Float",
                "Returns the arctangent of x, in radians.", "Float"
            ),
            "cdbl" to BuiltinDoc(
                "Cdbl(x as Integer) as Double",
                "Converts x to a double-precision floating point value.", "Double"
            ),
            "cint" to BuiltinDoc(
                "Cint(x as Float) as Integer",
                "Rounds x to the nearest integer.", "Integer"
            ),
            "cos" to BuiltinDoc(
                "Cos(x as Float) as Float",
                "Returns the cosine of x, where x is in radians.", "Float"
            ),
            "csng" to BuiltinDoc(
                "Csng(x as Integer) as Float",
                "Converts x to a single-precision floating point value.", "Float"
            ),
            "exp" to BuiltinDoc(
                "Exp(x as Float) as Float",
                "Returns e raised to the power x.", "Float"
            ),
            "fix" to BuiltinDoc(
                "Fix(x as Float) as Integer",
                "Truncates x toward zero, returning its integer part.", "Integer"
            ),
            "int" to BuiltinDoc(
                "Int(x as Float) as Integer",
                "Returns the largest integer less than or equal to x (floor).", "Integer"
            ),
            "log" to BuiltinDoc(
                "Log(x as Float) as Float",
                "Returns the natural logarithm (base e) of x.", "Float"
            ),
            "rnd" to BuiltinDoc(
                "Rnd(range as Integer) as Dynamic",
                "If range is greater than 0 returns an Integer from 1 to range; if 0 returns a Float from 0 to 1.",
                "Dynamic"
            ),
            "sgn" to BuiltinDoc(
                "Sgn(x as Float) as Integer",
                "Returns the sign of x: -1, 0, or 1.", "Integer"
            ),
            "sin" to BuiltinDoc(
                "Sin(x as Float) as Float",
                "Returns the sine of x, where x is in radians.", "Float"
            ),
            "sqr" to BuiltinDoc(
                "Sqr(x as Float) as Float",
                "Returns the square root of x.", "Float"
            ),
            "tan" to BuiltinDoc(
                "Tan(x as Float) as Float",
                "Returns the tangent of x, where x is in radians.", "Float"
            ),
            // Runtime
            "createobject" to BuiltinDoc(
                "CreateObject(name as String [, arg as Dynamic ...]) as Object",
                "Creates and returns a BrightScript component by class name (for example roArray, roSGNode, roDateTime).",
                "Object"
            ),
            "type" to BuiltinDoc(
                "Type(var as Dynamic [, version as String]) as String",
                "Returns the type name of var as a string.", "String"
            ),
            "getglobalaa" to BuiltinDoc(
                "GetGlobalAA() as Object",
                "Returns the global associative array (the root scope shared across the script).",
                "Object"
            ),
            "box" to BuiltinDoc(
                "Box(value as Dynamic) as Object",
                "Boxes an intrinsic value into its object form (for example Integer becomes roInt).",
                "Object"
            ),
            "eval" to BuiltinDoc(
                "Eval(code as String) as Dynamic",
                "Evaluates a string as BrightScript code. Largely deprecated; avoid in new code.",
                "Dynamic"
            ),
            "getlastruncompileerror" to BuiltinDoc(
                "GetLastRunCompileError() as Object",
                "Returns an roList describing the last compile error produced by Run(), or invalid.",
                "Object"
            ),
            "getlastrunruntimeerror" to BuiltinDoc(
                "GetLastRunRuntimeError() as Integer",
                "Returns the error code from the last runtime error produced by Run().",
                "Integer"
            ),
            // Utility
            "sleep" to BuiltinDoc(
                "Sleep(milliseconds as Integer) as Void",
                "Suspends script execution for the given number of milliseconds.", null
            ),
            "wait" to BuiltinDoc(
                "Wait(timeout as Integer, port as Object) as Object",
                "Waits for an event on a message port. A timeout of 0 waits indefinitely.",
                "Object"
            ),
            "getinterface" to BuiltinDoc(
                "GetInterface(object as Object, ifname as String) as Interface",
                "Returns the named interface of object if it is supported, otherwise invalid.",
                "Interface"
            ),
            "findmemberfunction" to BuiltinDoc(
                "FindMemberFunction(object as Object, funcName as String) as Interface",
                "Returns the interface that provides funcName on object, otherwise invalid.",
                "Interface"
            ),
            "uptime" to BuiltinDoc(
                "UpTime(dummy as Integer) as Float",
                "Returns the number of seconds since the system booted. The argument is ignored.",
                "Float"
            ),
            "rebootsystem" to BuiltinDoc(
                "RebootSystem() as Void",
                "Reboots the Roku device.", null
            ),
            "listdir" to BuiltinDoc(
                "ListDir(path as String) as Object",
                "Returns an roList of the file and directory names in path.", "Object"
            ),
            "readasciifile" to BuiltinDoc(
                "ReadAsciiFile(path as String) as String",
                "Returns the entire contents of the file at path as a string.", "String"
            ),
            "writeasciifile" to BuiltinDoc(
                "WriteAsciiFile(path as String, text as String) as Boolean",
                "Writes text to the file at path. Returns true on success.", "Boolean"
            ),
            "copyfile" to BuiltinDoc(
                "CopyFile(source as String, destination as String) as Boolean",
                "Copies a file. Returns true on success.", "Boolean"
            ),
            "movefile" to BuiltinDoc(
                "MoveFile(source as String, destination as String) as Boolean",
                "Moves or renames a file. Returns true on success.", "Boolean"
            ),
            "matchfiles" to BuiltinDoc(
                "MatchFiles(path as String, pattern as String) as Object",
                "Returns an roList of names in path matching the shell-style pattern.", "Object"
            ),
            "deletefile" to BuiltinDoc(
                "DeleteFile(path as String) as Boolean",
                "Deletes the file at path. Returns true on success.", "Boolean"
            ),
            "deletedirectory" to BuiltinDoc(
                "DeleteDirectory(path as String) as Boolean",
                "Deletes the empty directory at path. Returns true on success.", "Boolean"
            ),
            "createdirectory" to BuiltinDoc(
                "CreateDirectory(path as String) as Boolean",
                "Creates a directory at path. Returns true on success.", "Boolean"
            ),
            "formatdrive" to BuiltinDoc(
                "FormatDrive(drive as String, fsType as String) as Boolean",
                "Formats a drive. Development builds only. Returns true on success.", "Boolean"
            ),
            "strtoi" to BuiltinDoc(
                "StrToI(s as String [, radix as Integer]) as Integer",
                "Parses and returns the leading integer value from a string (0 if none).",
                "Integer"
            ),
            "rungarbagecollector" to BuiltinDoc(
                "RunGarbageCollector() as Object",
                "Forces a garbage collection cycle and returns an roAssociativeArray of collection stats.",
                "Object"
            ),
            "parsejson" to BuiltinDoc(
                "ParseJson(jsonString as String) as Object",
                "Parses a JSON-formatted string into an roArray or roAssociativeArray; invalid on failure.",
                "Object"
            ),
            "formatjson" to BuiltinDoc(
                "FormatJson(value as Object [, flags as Integer]) as String",
                "Serializes a value into a JSON-formatted string.", "String"
            ),
            "tr" to BuiltinDoc(
                "Tr(source as String) as String",
                "Returns the localized translation of source for the current locale.", "String"
            ),
            // String
            "ucase" to BuiltinDoc(
                "UCase(s as String) as String",
                "Returns s converted to upper case.", "String"
            ),
            "lcase" to BuiltinDoc(
                "LCase(s as String) as String",
                "Returns s converted to lower case.", "String"
            ),
            "asc" to BuiltinDoc(
                "Asc(s as String) as Integer",
                "Returns the Unicode code point of the first character of s.", "Integer"
            ),
            "chr" to BuiltinDoc(
                "Chr(code as Integer) as String",
                "Returns the character for the given Unicode code point.", "String"
            ),
            "instr" to BuiltinDoc(
                "Instr(start as Integer, s as String, search as String) as Integer",
                "Returns the 1-based index of search within s starting at start, or 0 if not found.",
                "Integer"
            ),
            "left" to BuiltinDoc(
                "Left(s as String, n as Integer) as String",
                "Returns the leftmost n characters of s.", "String"
            ),
            "len" to BuiltinDoc(
                "Len(s as String) as Integer",
                "Returns the number of characters in s.", "Integer"
            ),
            "mid" to BuiltinDoc(
                "Mid(s as String, p as Integer [, n as Integer]) as String",
                "Returns a substring of s starting at 1-based position p, optionally n characters long.",
                "String"
            ),
            "right" to BuiltinDoc(
                "Right(s as String, n as Integer) as String",
                "Returns the rightmost n characters of s.", "String"
            ),
            "str" to BuiltinDoc(
                "Str(value as Float) as String",
                "Returns the string form of a number, with a leading space for non-negative values.",
                "String"
            ),
            "stri" to BuiltinDoc(
                "StrI(value as Integer [, radix as Integer]) as String",
                "Returns the string form of an integer, optionally in the given radix (2 to 36).",
                "String"
            ),
            "stringi" to BuiltinDoc(
                "StringI(n as Integer, ch as Integer) as String",
                "Returns a string of n characters whose Unicode code point is ch.", "String"
            ),
            "val" to BuiltinDoc(
                "Val(s as String [, radix as Integer]) as Double",
                "Parses and returns a number from a string; an optional radix parses an integer.",
                "Double"
            ),
            "substitute" to BuiltinDoc(
                "Substitute(template as String, arg0 as String [, arg1, arg2, arg3]) as String",
                "Returns template with placeholders ^0 through ^3 replaced by the given arguments.",
                "String"
            )
        )
    }
}
