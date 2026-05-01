package nl.jkool.beerxmlviewer

import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import java.math.BigDecimal
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

private val XML_NUMBER_PATTERN = Regex("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")

fun beerXmlToJSONObject(xml: String?): JSONObject {
    val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = false
        isIgnoringComments = true
        isCoalescing = true
        isExpandEntityReferences = false
        setFeatureIfSupported(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        setFeatureIfSupported("http://apache.org/xml/features/disallow-doctype-decl", true)
        setFeatureIfSupported("http://xml.org/sax/features/external-general-entities", false)
        setFeatureIfSupported("http://xml.org/sax/features/external-parameter-entities", false)
        setFeatureIfSupported("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    }

    val documentBuilder = documentBuilderFactory.newDocumentBuilder().apply {
        setEntityResolver { _, _ -> InputSource(StringReader("")) }
    }
    val document = documentBuilder.parse(InputSource(StringReader(xml ?: throw IllegalArgumentException())))
    val root = document.documentElement
    return JSONObject().put(root.nodeName, root.toJsonValue())
}

private fun DocumentBuilderFactory.setFeatureIfSupported(feature: String, value: Boolean) {
    try {
        setFeature(feature, value)
    } catch (_: Exception) {
    }
}

private fun Element.toJsonValue(): Any {
    val childElements = mutableListOf<Element>()
    val text = StringBuilder()
    val children = childNodes

    for (index in 0 until children.length) {
        when (val childNode = children.item(index)) {
            is Element -> childElements.add(childNode)
            else -> if (childNode.nodeType == Node.TEXT_NODE || childNode.nodeType == Node.CDATA_SECTION_NODE) {
                text.append(childNode.nodeValue)
            }
        }
    }

    if (childElements.isEmpty() && !hasAttributes()) {
        return parseXmlText(text.toString())
    }

    val json = JSONObject()
    val nodeAttributes = attributes
    for (index in 0 until nodeAttributes.length) {
        val attribute = nodeAttributes.item(index)
        json.put(attribute.nodeName, parseXmlText(attribute.nodeValue))
    }
    childElements.forEach { childElement ->
        json.putRepeated(childElement.nodeName, childElement.toJsonValue())
    }
    text.toString().trim().takeIf { it.isNotEmpty() }?.let { content ->
        json.put("content", parseXmlText(content))
    }
    return json
}

private fun JSONObject.putRepeated(name: String, value: Any) {
    when (val existing = opt(name)) {
        null -> put(name, value)
        is JSONArray -> existing.put(value)
        else -> put(name, JSONArray().put(existing).put(value))
    }
}

private fun parseXmlText(text: String): Any {
    val value = text.trim()
    if (value.isEmpty()) return ""
    if (value.equals("true", ignoreCase = true)) return true
    if (value.equals("false", ignoreCase = true)) return false
    if (value.equals("null", ignoreCase = true)) return JSONObject.NULL
    if (XML_NUMBER_PATTERN.matches(value)) {
        try {
            return BigDecimal(value)
        } catch (_: NumberFormatException) {
        }
    }
    return value
}
