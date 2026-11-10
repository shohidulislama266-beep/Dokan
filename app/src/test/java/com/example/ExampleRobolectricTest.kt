package com.example

import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.localization.toBengaliNumerals
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleAppTest {

    @Test
    fun testBengaliNumeralsConversion() {
        val result = toBengaliNumerals("12345")
        assertEquals("১২৩৪৫", result)
    }

    @Test
    fun testFormatTakaEnglish() {
        val formatted = formatTaka(1500.0, AppLanguage.ENGLISH)
        assertEquals("৳ 1,500", formatted)
    }

    @Test
    fun testFormatTakaBangla() {
        val formatted = formatTaka(250.0, AppLanguage.BANGLA)
        assertEquals("৳ ২৫০", formatted)
    }
}
