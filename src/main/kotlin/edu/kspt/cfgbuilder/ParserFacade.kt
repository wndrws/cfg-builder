package edu.kspt.cfgbuilder
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files


class ParserFacade {
    private fun readFile(file: File, encoding: Charset): String {
        val encoded = Files.readAllBytes(file.toPath())
        return String(encoded, encoding)
    }

    fun parse(file: File, vararg listeners: Python3Listener): Python3Parser.File_inputContext {
        val code = readFile(file, Charset.forName("UTF-8"))
        return parse(code, *listeners)
    }

    fun parse(code: String, vararg listeners: Python3Listener): Python3Parser.File_inputContext {
        val lexer = Python3Lexer(ANTLRInputStream(code))
        val tokens = CommonTokenStream(lexer)
        val parser = Python3Parser(tokens)
        for (listener in listeners) {
            parser.addParseListener(listener)
        }
        return parser.file_input()
    }
}