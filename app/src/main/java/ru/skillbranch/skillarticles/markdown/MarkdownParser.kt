package ru.skillbranch.skillarticles.markdown

import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = "\n"

    //group regex
    private const val UNORDERED_LIST_ITEM_GROUP = "" //TODO implement me
    private const val HEADER_GROUP = "" //TODO implement me
    private const val QUOTE_GROUP = "" //TODO implement me
    private const val ITALIC_GROUP = "" //TODO implement me
    private const val BOLD_GROUP ="" //TODO implement me
    private const val STRIKE_GROUP = "" //TODO implement me
    private const val RULE_GROUP = "" //TODO implement me
    private const val INLINE_GROUP = "" //TODO implement me
    private const val LINK_GROUP = "" //TODO implement me
    private const val BLOCK_CODE_GROUP = "" //TODO implement me
    private const val ORDER_LIST_GROUP = "" //TODO implement me

    //result regex
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP"
    //|$BLOCK_CODE_GROUP|$ORDER_LIST_GROUP optionally

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    /**
     * parse markdown text to elements
     */
    fun parse(string: String): MarkdownText {
        //TODO implement me
    }

    /**
     * clear markdown text to string without markdown characters
     */
    fun clear(string: String?): String? {
        //TODO implement me
    }

    /**
     * find markdown elements in markdown text
     */
    private fun findElements(string: CharSequence): List<Element> {
        //TODO implement me

        loop@ while () {
            //TODO implement me
            //groups range for iterate by groups (1..9) or (1..11) optionally
            val groups = 1..11
            when () {
                //NOT FOUND -> BREAK
                -1 -> break@loop

                //UNORDERED LIST
                1 -> {
                    //text without "*. "
                    //TODO implement me
                }

                //HEADER
                2 -> {
                    //text without "{#} "
                    //TODO implement me
                }

                //QUOTE
                3 -> {
                    //text without "> "
                    //TODO implement me
                }

                //ITALIC
                4 -> {
                    //text without "*{}*"
                    //TODO implement me
                }

                //BOLD
                5 -> {
                    //text without "**{}**"
                    //TODO implement me
                }

                //STRIKE
                6 -> {
                    //text without "~~{}~~"
                    //TODO implement me
                }

                //RULE
                7 -> {
                    //text without "***" insert empty character
                    //TODO implement me
                }

                //RULE
                8 -> {
                    //text without "`{}`"
                    //TODO implement me
                }

                //LINK
                9 -> {
                    //full text for regex
                    //TODO implement me
                }
                //10 -> BLOCK CODE - optionally
                10 -> {
                    //TODO implement me
                }

                //11 -> NUMERIC LIST
                11 -> {
                    //TODO implement me
                }
            }

        }

        //TODO implement me
    }
}

data class MarkdownText(val elements: List<Element>)

sealed class Element() {
    abstract val text: CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = " ", //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: String,
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        val type: Type = Type.MIDDLE,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type { START, END, MIDDLE, SINGLE }
    }
}