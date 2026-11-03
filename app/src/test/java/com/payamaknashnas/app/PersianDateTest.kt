package com.payamaknashnas.app

import com.payamaknashnas.app.data.PersianDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersianDateTest {

    @Test
    fun `نوروز ۱۴۰۵ برابر ۲۱ مارس ۲۰۲۶ است`() {
        val (y, m, d) = PersianDate.toJalali(2026, 3, 21)
        assertEquals(1405, y)
        assertEquals(1, m)
        assertEquals(1, d)
    }

    @Test
    fun `پایان سال ۱۴۰۵ برابر ۲۰ مارس ۲۰۲۷ است`() {
        val (y, m, d) = PersianDate.toJalali(2027, 3, 20)
        assertEquals(1405, y)
        assertEquals(12, m)
        assertEquals(29, d)
    }

    @Test
    fun `سال کبیسه شمسی ۱۴۰۳ دارد ۳۰ روز در اسفند`() {
        val (y, m, d) = PersianDate.toJalali(2025, 3, 20)
        assertEquals(1403, y)
        assertEquals(12, m)
        assertEquals(30, d)
    }

    @Test
    fun `تبدیل ارقام فارسی و لاتین رفت و برگشتی است`() {
        val latin = "09121234567"
        val fa = PersianDate.toPersianDigits(latin)
        assertTrue(fa.contains('۰'))
        assertEquals(latin, PersianDate.toEnglishDigits(fa))
    }

    @Test
    fun `آخر هفته جمعه است`() {
        // ۲۰۲۶-۱۱-۰۶ جمعه
        val millis = java.util.GregorianCalendar(2026, 10, 6, 12, 0, 0).timeInMillis
        assertEquals("جمعه", PersianDate.weekdayShort(millis))
    }

    @Test
    fun `فاصله زمانی صفر با همین حالا نمایش داده می‌شود`() {
        val now = System.currentTimeMillis()
        assertEquals("همین حالا", PersianDate.relative(now, now))
    }
}
