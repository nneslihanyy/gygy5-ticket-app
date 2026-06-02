package com.turkcell.core.util

private val turkishMonthsShort = listOf(
    "Oca", "Şub", "Mar", "Nis", "May", "Haz",
    "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara"
)

/**
 * ISO-8601 tarih string'ini okunabilir Türkçe formata çevirir.
 * Örnek: "2026-05-21T15:19:31.824Z" → "21 May 2026, 18:19"
 */
fun formatIsoDateTr(isoString: String): String {
    return try {
        // "2026-05-21T15:19:31.824Z" formatını parse et
        val cleaned = isoString.replace("Z", "")
        val parts = cleaned.split("T")
        val dateParts = parts[0].split("-")
        val timeParts = parts[1].split(":")

        val year = dateParts[0].toInt()
        val month = dateParts[1].toInt()
        val day = dateParts[2].toInt()
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        // UTC → Türkiye saati (+3)
        val adjustedHour = (hour + 3) % 24

        val monthName = turkishMonthsShort.getOrElse(month - 1) { "???" }
        "$day $monthName $year, ${adjustedHour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        isoString.take(16).replace("T", " ")
    }
}

/**
 * Kuruş cinsinden fiyatı TL formatına çevirir.
 * Örnek: 69999 → "₺699,99"
 */
fun formatPriceTl(cents: Long): String {
    val lira = cents / 100
    val kurus = cents % 100
    return if (kurus == 0L) "₺$lira" else "₺$lira,${kurus.toString().padStart(2, '0')}"
}