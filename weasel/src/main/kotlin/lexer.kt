sealed class Token {
    override fun toString(): String = javaClass.simpleName
    // Keywords
    object IF: Token()
    object THEN: Token()
    object ELSE: Token()

    // Symbols
    object LEFT_PAREN: Token()
    object RIGHT_PAREN: Token()
    object LAMBDA: Token()
    object RIGHT_ARROW: Token()

    // Idents
    data class IDENT(val ident: String): Token()

    // Literals
    data class BOOLEAN(val boolean: Boolean): Token()
    data class NUMBER(val number: Int): Token()

    // EOF
    object END_OF_FILE: Token()
}

class Peekable<T>(val iterator: Iterator<T>) {
    var lookahead: T? = null
    fun next(): T? = when {
        lookahead != null -> lookahead.also { lookahead = null }
        iterator.hasNext() -> iterator.next()
        else -> null
    }

    fun peek(): T? = next().also { lookahead = it }
}

class Lexer(val input: String) {
    val chars = Peekable(input.iterator())

    fun next(): Token {
        consumeWhitespace()
        val c = chars.next() ?: return Token.END_OF_FILE
        return when(c) {
            '(' -> Token.LEFT_PAREN
            ')' -> Token.RIGHT_PAREN
            '\\' -> Token.LAMBDA
            '-' -> if(chars.next() == '>') Token.RIGHT_ARROW else throw Exception("Unclosed arrow token")
            else -> when {
                c.isJavaIdentifierStart() -> ident(c)
                c.isDigit() -> number(c)
                else -> throw Exception("Unexpected $c")
            }
        }
    }

    private fun number(c: Char): Token {
        var res = c.toString()
        while (chars.peek()?.isDigit() == true) res += chars.next()
        return Token.NUMBER(res.toInt())
    }

    private fun ident(c: Char): Token {
        var res = c.toString()
        while (chars.peek()?.isJavaIdentifierPart() == true) res += chars.next()
        return when(res) {
            "if" -> Token.IF
            "then" -> Token.THEN
            "else" -> Token.ELSE
            "true" -> Token.BOOLEAN(true)
            "false" -> Token.BOOLEAN(false)
            else -> Token.IDENT(res)
        }
    }

    private fun consumeWhitespace() {
        while (chars.peek()?.isWhitespace() == true) chars.next()
    }
}

fun main() {
    val input = """
        if (\x1 -> equals 20 x1) 25
        then true
        else add 3 (multiply 4 5)
    """.trimIndent()
    val lexer = Lexer(input)
    while(lexer.next().also(::println) !is Token.END_OF_FILE) {}
}