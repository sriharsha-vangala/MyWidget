package com.example.mywidget.json

import org.json.JSONObject

object UiSchemaValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )

    fun validate(json: JSONObject): ValidationResult {
        val errors = mutableListOf<String>()

        // 1. Required root
        if (!json.has("root")) {
            errors.add("Missing 'root' node")
        }

        // 2. Required resources
        if (!json.has("resources")) {
            errors.add("Missing 'resources' block")
        } else {
            val resources = json.getJSONObject("resources")
            if (!resources.has("color")) errors.add("Missing 'resources.color'")
            if (!resources.has("font")) errors.add("Missing 'resources.font'")
        }

        // Collect all IDs (excluding resources)
        val allIds = json.keys().asSequence().filter { it != "resources" }.toSet()

        // Track parent counts
        val parentCount = mutableMapOf<String, Int>()

        for (id in allIds) {
            if (id == "resources") continue

            val node = json.optJSONObject(id) ?: continue

            // Children validation
            if (node.has("children")) {
                val children = node.getJSONArray("children")
                for (i in 0 until children.length()) {
                    val childId = children.getString(i)
                    if (!allIds.contains(childId)) {
                        errors.add("Child '$childId' in '$id' does not exist")
                    } else {
                        parentCount[childId] = parentCount.getOrDefault(childId, 0) + 1
                    }
                }
            }
        }

        // Root should not be child of anyone
        if (parentCount.containsKey("root")) {
            errors.add("'root' cannot be a child of another node")
        }

        // Each node (except root) must have exactly one parent
        for ((child, count) in parentCount) {
            if (child != "root" && count > 1) {
                errors.add("Node '$child' has multiple parents ($count)")
            }
        }

        for (id in allIds) {
            if (id != "root" && !parentCount.containsKey(id)) {
                errors.add("Node '$id' has no parent")
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }
}