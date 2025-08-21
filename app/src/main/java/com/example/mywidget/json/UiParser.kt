package com.example.mywidget.json

import org.json.JSONObject
import kotlin.collections.forEach

object UiParser {
    fun parseUiJsonMap(jsonString: String): UiElement {
        val rootObj = JSONObject(jsonString)

        // Extract resources
        val resources = rootObj.optJSONObject("resources")

        // Helper to resolve colors/fonts later if needed
//        val colorMap = mutableMapOf<String, String>()
        resources?.optJSONObject("color")?.let { colors ->
            colors.keys().forEach { key ->
                ColorRegistry.registerColor(key, colors.getString(key))
            }
        }
//        ColorRegistry.registerColors(colorMap)

        val fontMap = mutableMapOf<String, String>()
        resources?.optJSONObject("font")?.let { fonts ->
            fonts.keys().forEach { key -> fontMap[key] = fonts.getString(key) }
        }


        // Remove resources key
        val nodesMap = mutableMapOf<String, JSONObject>()
        rootObj.keys().forEach { key ->
            if (key != "resources") nodesMap[key] = rootObj.getJSONObject(key)
        }

        // Recursive builder
        fun buildNode(id: String): UiElement {
            val obj = nodesMap[id]!!
            val attrs = mutableMapOf<String, String>()
            obj.optJSONObject("attributes")?.let { a ->
                a.keys().forEach { key ->
                    attrs[key] = a.get(key).toString()
                }
            }


            val childrenIds = obj.optJSONArray("children")
            val children = mutableListOf<UiElement>()
            if (childrenIds != null) {
                for (i in 0 until childrenIds.length()) {
                    val childId = childrenIds.getString(i)
                    buildNode(childId).let { children.add(it) }
                }
            }

            val type = obj.optString("type", "column") // default root
            return if (type == "text") {
                val text = obj.optString("text", "")
                UiElement.TextNode(id, text, attrs)
            } else {
                val layout = when (type.lowercase()) {
                    "row" -> UiElement.Layout.Row
                    "column" -> UiElement.Layout.Column
                    "stack" -> UiElement.Layout.Stack
                    else -> UiElement.Layout.Column
                }
                UiElement.Container(id, layout, attrs, children)
            }
        }

        // Root node assumed to be "root"
        return buildNode("root")
    }
}