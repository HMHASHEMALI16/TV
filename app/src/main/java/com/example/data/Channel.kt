package com.example.data

data class Channel(
    val id: String,
    val name: String,
    val streamUrl: String = ""
) {
    /**
     * Formats 0-based index to 1-based serial number: 0 -> "1", 1 -> "2", etc.
     */
    fun serialNumber(index: Int): String {
        return (index + 1).toString()
    }

    /**
     * Formats 0-based index to 2-digit channel number, e.g. 0 -> "01", 9 -> "10"
     */
    fun formattedNumber(index: Int): String {
        val number = index + 1
        return if (number < 10) "0$number" else number.toString()
    }
}
