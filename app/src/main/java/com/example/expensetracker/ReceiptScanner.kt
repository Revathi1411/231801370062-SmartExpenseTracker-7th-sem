package com.example.expensetracker

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

// Reads text from a receipt image using ML Kit and finds the amount
object ReceiptScanner {

    fun scan(
        context: Context,
        imageUri: Uri,
        onResult: (Double?) -> Unit,
        onError: (String) -> Unit
    ) {
        val image = try {
            InputImage.fromFilePath(context, imageUri)
        } catch (e: Exception) {
            onError("Could not open the image")
            return
        }

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onResult(findLargestAmount(visionText.text))
            }
            .addOnFailureListener {
                onError("Receipt scanning failed. Try another image.")
            }
    }

    // Finds all numbers in the text and returns the largest valid one.
    // Numbers with decimals (like 450.00) are checked first because receipt totals
    // have decimals, while years, bill numbers and quantities usually do not.
    fun findLargestAmount(text: String): Double? {
        val numberPattern = Regex("\\d+(,\\d{3})*(\\.\\d{1,2})?")
        var largestWithDecimal = 0.0
        var largestWhole = 0.0

        for (match in numberPattern.findAll(text)) {
            val cleaned = match.value.replace(",", "")
            val value = cleaned.toDoubleOrNull() ?: continue

            // ignore zero and very big numbers (phone numbers, card numbers etc.)
            if (value <= 0 || value > 1000000) continue

            if (cleaned.contains(".")) {
                if (value > largestWithDecimal) largestWithDecimal = value
            } else {
                if (value > largestWhole) largestWhole = value
            }
        }

        if (largestWithDecimal > 0) return largestWithDecimal
        if (largestWhole > 0) return largestWhole
        return null
    }
}
