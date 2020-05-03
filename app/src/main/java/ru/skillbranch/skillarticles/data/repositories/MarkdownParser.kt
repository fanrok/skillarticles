package ru.skillbranch.skillarticles.data.repositories

import java.lang.StringBuilder
import java.security.interfaces.ECKey
import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = "\n"

    //group regex
    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP =
            "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP = "(~~.+?~~)"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
    private const val BLOCK_CODE_GROUP = "(^```[\\s\\S]+&```$)"
    private const val ORDER_LIST_GROUP = "(^\\d{1,2}\\.\\s.+?$)"
    private const val IMAGE_GROUP = "(^!\\[[^\\[\\]]*?\\]\\(.*?\\)$)"

    //result regex
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP|$IMAGE_GROUP|$BLOCK_CODE_GROUP|$ORDER_LIST_GROUP"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    /**
     * parse markdown text to elements
     */
    fun parse(string: String): List<MarkdownElement> {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return elements.fold(mutableListOf()) { acc, element ->
            val last = acc.lastOrNull()
            when (element) {
                is Element.Image -> acc.add(
                        MarkdownElement.Image(
                                element,
                                last?.bounds?.second ?: 0
                        )
                )
                is Element.BlockCode -> acc.add(
                        MarkdownElement.Scroll(
                                element,
                                last?.bounds?.second ?: 0
                        )
                )
                else -> {
                    if (last is MarkdownElement.Text) last.elements.add(element)
                    else acc.add(
                            MarkdownElement.Text(
                                    mutableListOf(element),
                                    last?.bounds?.second ?: 0
                            )
                    )
                }
            }
            acc
        }
    }

    /**
     * clear markdown text to string without markdown characters
     */
    fun clear(string: String?): String? {
//        return string?.let {
//            it.replace("#", "")
//                .replace("*", "")
//                .replace("___", "")
//                .replace("[", "")
//                .replace("]", "")
//                .replace("~~", "")
//                .replace("__", "")
//                .replace("---", "")
//                .replace("+", "")
//                .replace("-", "")
//                .replace(">", "")
//
//        }
        string ?: return null
        var clearString = ""
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()
            // we found a mark
            if (lastStartIndex < startIndex) {
                // check what was before the mark
                clearString += string.substring(lastStartIndex, startIndex)
            }
            // check what kind of mark this was
            var text: String
            val groups = 1..9
            var group = -1
            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                //NOT FOUND -> BREAK
                -1 -> break@loop
                //1 -> LIST GROUP
                1 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex).toString()
                    val subs = clear(text)
                    clearString += if (subs.isNullOrEmpty()) text else subs

                    lastStartIndex = endIndex
                }

                //2 -> HEADER GROUP
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.substring(startIndex, endIndex))
                    val level = reg!!.value.length

                    clearString += string.substring(startIndex + level.inc(), endIndex)
                    lastStartIndex = endIndex
                }

                //3 -> QUOTE_GROUP
                3 -> {
                    text = string.substring(startIndex.plus(2), endIndex)
                    val subs = clear(text)
                    clearString += if (subs.isNullOrEmpty()) text else subs
                    lastStartIndex = endIndex
                }

                //4 -> ITALIC_GROUP
                4 -> {
                    text = string.substring(startIndex.inc(), endIndex.dec())
                    val subs = clear(text)
                    clearString += if (subs.isNullOrEmpty()) text else subs
                    lastStartIndex = endIndex
                }

                //5 -> BOLD_GROUP
                5 -> {
                    text = string.substring(startIndex.plus(2), endIndex.plus(-2))
                    val subs = clear(text)
                    clearString += if (subs.isNullOrEmpty()) text else subs
                    lastStartIndex = endIndex
                }

                //6 -> STRIKE_GROUP
                6 -> {
                    text = string.substring(startIndex.plus(2), endIndex.plus(-2))
                    val subs = clear(text)
                    clearString += if (subs.isNullOrEmpty()) text else subs
                    lastStartIndex = endIndex
                }

                //7 -> RULE_GROUP
                7 -> {
                    clearString += " "
                    lastStartIndex = endIndex
                }

                //8 -> INLINE CODE GROUP
                8 -> {
                    text = string.substring(startIndex.inc(), endIndex.dec())
                    clearString += text
                    lastStartIndex = endIndex
                }

                //9 -> LINK_GROUP
                9 -> {
                    text = string.substring(startIndex, endIndex)
                    val (title: String, _) = "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    clearString += title
                    lastStartIndex = endIndex
                }


                //10 -> BLOCK CODE
//                10 -> {
//
//                }
//
//                //11 -> ORDERED LIST
//                11 -> {
//
//                }
            }
        }

        // check if there's any more text left
        if (lastStartIndex < string.length) {
            clearString += string.substring(lastStartIndex, string.length)
        }

        return clearString
    }

    /**
     * find markdown elements in markdown text
     */
    private fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            if (lastStartIndex < startIndex) {
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }
            var text: CharSequence

            val groups = 1..12
            var group = -1
            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                //NOT FOUND -> BREAK
                -1 -> break@loop

                //UNORDERED LIST
                1 -> {
                    //text without "*. "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subs = findElements((text))
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //HEADER
                2 -> {
                    //text without "{#} "
                    val req = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = req!!.value.length

                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)

                    val element = Element.Header(level, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //QUOTE
                3 -> {
                    //text without "> "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subelements = findElements(text)
                    val element = Element.Quote(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ITALIC
                4 -> {
                    //text without "*{}*"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements = findElements(text)
                    val element = Element.Italic(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BOLD
                5 -> {
                    //text without "**{}**"
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subelements = findElements(text)
                    val element = Element.Bold(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //STRIKE
                6 -> {
                    //text without "~~{}~~"
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subelements = findElements(text)
                    val element = Element.Strike(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                7 -> {
                    //text without "***" insert empty character
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                8 -> {
                    //text without "`{}`"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val element = Element.InlineCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //LINK
                9 -> {
                    //full text for regex
                    text = string.subSequence(startIndex, endIndex)
                    val (title: String, link: String) = "\\[(.*)]\\((.*)\\)".toRegex()
                            .find(text)!!.destructured
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
//                10 -> BLOCK CODE - optionally
                10 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.plus(-3)).toString()
                    val element = Element.BlockCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //11 -> NUMERIC LIST
                11 -> {
                    val reg = "(^\\d{1,2}.)".toRegex().find(string.substring(startIndex, endIndex))
                    val order = reg!!.value
                    text = string.subSequence(startIndex.plus(order.length.inc()), endIndex).toString()
                    val subs = findElements(text)
                    val element =
                            Element.OrderedListItem(
                                    order,
                                    text.toString(),
                                    subs
                            )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //12 Image Group
                12 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (alt, url, title) = "^!\\[([^\\[\\]]*?)?]\\((.*?) \"(.?)\"\\)$".toRegex().find(text)!!.destructured
                    val element = Element.Image(url, if (alt.isBlank()) null else alt, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
            }

        }

        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element.Text(text))
        }
        return parents
    }
}

sealed class MarkdownElement() {
    abstract val offset: Int
    val bounds: Pair<Int, Int> by lazy {
        when (this) {
            is Text -> {
                val end = elements.fold(offset) { acc, el ->
                    acc + el.spread().map { it.text.length }.sum()
                }
                offset to end
            }
            is Image -> offset to image.text.length + offset
            is Scroll -> offset to blockCode.text.length + offset
        }
    }

    data class Text(
            val elements: MutableList<Element>,
            override val offset: Int = 0
    ) : MarkdownElement()

    data class Image(
            val image: Element.Image,
            override val offset: Int = 0
    ) : MarkdownElement()

    data class Scroll(
            val blockCode: Element.BlockCode,
            override val offset: Int = 0
    ) : MarkdownElement()
}

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
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Image(
            val url: String,
            val alt: String?,
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element()
}

private fun Element.spread(): List<Element> {
    val elements = mutableListOf<Element>()
    if (this.elements.isNotEmpty()) elements.addAll(this.elements.spread())
    else elements.add(this)
    return elements
}

private fun List<Element>.spread(): List<Element>{
    val elements = mutableListOf<Element>()
    forEach { elements.addAll(it.spread()) }
    return elements
}

private fun Element.clearContent(): String{
    return StringBuilder().apply{
        val element = this@clearContent
        if(element.elements.isEmpty()) append(element.text)
        else element.elements.forEach { append(it.clearContent()) }
    }.toString()
}

fun List<MarkdownElement>.clearContent(): String{
    return  StringBuilder().apply{
        this@clearContent.forEach {
            when(it){
                is MarkdownElement.Text -> it.elements.forEach{el->clearContent()}
                is MarkdownElement.Image -> append(it.image.clearContent())
                is MarkdownElement.Scroll -> append(it.blockCode.clearContent())
            }
        }
    }.toString()
}