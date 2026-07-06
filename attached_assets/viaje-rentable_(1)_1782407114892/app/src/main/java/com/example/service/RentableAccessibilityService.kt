package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RentableAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        val texts = mutableListOf<String>()
        findTextNodes(rootNode, texts)
        
        if (texts.isEmpty()) return
        
        var foundEarnings: Double? = null
        var foundPickup: Double? = null
        var foundPickupUnit = "km"
        var foundTrip: Double? = null
        var foundPlatform = "Otro"
        
        val packageName = event.packageName?.toString() ?: ""
        if (packageName.contains("uber")) {
            foundPlatform = "Uber"
        } else if (packageName.contains("didi")) {
            foundPlatform = "DiDi"
        }
        
        for (text in texts) {
            val cleaned = text.trim()
            
            // Look for monetary indicators like "$119.51" or "$75.48"
            if (cleaned.startsWith("$") || cleaned.contains("$")) {
                val valueStr = cleaned.replace("$", "").replace(",", ".").trim()
                val parsedValue = valueStr.toDoubleOrNull()
                if (parsedValue != null && foundEarnings == null) {
                    foundEarnings = parsedValue
                }
            }
            // Look for distances ending with "km" or "m"
            else if (cleaned.lowercase().contains("km") || cleaned.lowercase().endsWith("km")) {
                val numPart = cleaned.lowercase().replace("km", "").replace(",", ".").trim().toDoubleOrNull()
                if (numPart != null) {
                    if (foundPickup == null) {
                        foundPickup = numPart
                        foundPickupUnit = "km"
                    } else if (foundTrip == null) {
                        foundTrip = numPart
                    }
                }
            } else if (cleaned.lowercase().contains(" m") || cleaned.lowercase().endsWith("m")) {
                val numPart = cleaned.lowercase().replace(" m", "").replace("m", "").replace(",", ".").trim().toDoubleOrNull()
                if (numPart != null) {
                    if (foundPickup == null) {
                        foundPickup = numPart
                        foundPickupUnit = "m"
                    } else if (foundTrip == null) {
                        foundTrip = numPart
                    }
                }
            }
        }
        
        // If any useful driver data was retrieved in the background, broadcast it for live UI update
        if (foundEarnings != null || foundPickup != null || foundTrip != null) {
            val intent = Intent("com.example.VIAJE_RENTABLE_DETECTION").apply {
                putExtra("platform", foundPlatform)
                if (foundEarnings != null) putExtra("earnings", foundEarnings)
                if (foundPickup != null) putExtra("pickup", foundPickup)
                putExtra("pickupUnit", foundPickupUnit)
                if (foundTrip != null) putExtra("trip", foundTrip)
            }
            sendBroadcast(intent)
        }
    }

    private fun findTextNodes(node: AccessibilityNodeInfo, list: MutableList<String>) {
        val text = node.text?.toString()
        if (!text.isNullOrBlank()) {
            list.add(text)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                findTextNodes(child, list)
                child.recycle()
            }
        }
    }

    override fun onInterrupt() {
        Log.d("RentableService", "Service Interrupted")
    }
}
