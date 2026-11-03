package com.payamaknashnas.app.data

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

/**
 * تبدیل تاریخ میلادی به شمسی و قالب‌بندی فارسی.
 * پیاده‌سازی خالص Kotlin (بدون وابستگی به ICU) تا در تست واحد هم قابل بررسی باشد.
 * الگوریتم: پورت استاندارد jalaali-js.
 */
object PersianDate {

    private val persianDigits = charArrayOf(
        '\u06F0', '\u06F1', '\u06F2', '\u06F3', '\u06F4',
        '\u06F5', '\u06F6', '\u06F7', '\u06F8', '\u06F9'
    )

    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') sb.append(persianDigits[ch - '0']) else sb.append(ch)
        }
        return sb.toString()
    }

    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val idx = persianDigits.indexOf(ch)
            when {
                idx >= 0 -> sb.append(idx)
                ch in '\u0660'..'\u0669' -> sb.append((ch - '\u0660').digitToChar())
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)

    /** خروجی: سال، ماه، روز شمسی */
    fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        return if (days < 186) {
            Triple(jy, 1 + days / 31, 1 + days % 31)
        } else {
            Triple(jy, 7 + (days - 186) / 30, 1 + (days - 186) % 30)
        }
    }

    private fun cal(millis: Long): Calendar =
        GregorianCalendar(TimeZoneHolder.ir, Locale.US).apply { timeInMillis = millis }

    private object TimeZoneHolder {
        val ir: java.util.TimeZone = java.util.TimeZone.getTimeZone("Asia/Tehran")
    }

    private fun two(value: Int): String = if (value < 10) "0$value" else value.toString()

    private fun parts(millis: Long): IntArray {
        val c = cal(millis)
        val (jy, jm, jd) = toJalali(
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.DAY_OF_MONTH)
        )
        return intArrayOf(
            jy, jm, jd,
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            c.get(Calendar.DAY_OF_WEEK)
        )
    }

    /** مثال: ۱۴۰۵/۰۸/۱۳ - ۱۴:۳۰ */
    fun fullDateTime(millis: Long): String {
        val p = parts(millis)
        return toPersianDigits("${p[0]}/${two(p[1])}/${two(p[2])} - ${two(p[3])}:${two(p[4])}")
    }

    /** مثال: ۱۴۰۵/۰۸/۱۳ */
    fun dateOnly(millis: Long): String {
        val p = parts(millis)
        return toPersianDigits("${p[0]}/${two(p[1])}/${two(p[2])}")
    }

    /** مثال: ۱۴:۳۰ */
    fun timeOnly(millis: Long): String {
        val p = parts(millis)
        return toPersianDigits("${two(p[3])}:${two(p[4])}")
    }

    fun relative(millis: Long, now: Long = System.currentTimeMillis()): String {
        val diff = now - millis
        if (diff < 0) return "همین حالا"
        val minutes = diff / 60_000L
        return when {
            minutes < 1 -> "همین حالا"
            minutes < 60 -> toPersianDigits(minutes.toString()) + " دقیقه پیش"
            minutes < 24 * 60 -> toPersianDigits((minutes / 60).toString()) + " ساعت پیش"
            minutes < 48 * 60 -> "دیروز"
            minutes < 7 * 24 * 60 -> toPersianDigits((minutes / (24 * 60)).toString()) + " روز پیش"
            else -> dateOnly(millis)
        }
    }

    fun weekdayShort(millis: Long): String {
        val idx = parts(millis)[5]
        val names = arrayOf("یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه")
        return names[(idx - 1).coerceIn(0, 6)]
    }

    fun defaultLocale(): Locale = Locale("fa", "IR")
}
