package com.example.ui

import java.util.Calendar

object JalaliCalendarHelper {

    data class JalaliDate(
        val year: Int,
        val month: Int, // 1-indexed
        val day: Int,   // 1-indexed
        val dayOfWeekName: String,
        val monthName: String,
        val holidayName: String? = null
    )

    private val persianMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun toPersianDigits(input: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return input.map { char ->
            if (char in '0'..'9') persianDigits[char - '0'] else char
        }.joinToString("")
    }

    fun getJalaliDate(dateString: String): JalaliDate {
        // Expected format: "yyyy-MM-dd"
        val parts = dateString.split("-")
        val gy = parts[0].toInt()
        val gm = parts[1].toInt()
        val gd = parts[2].toInt()

        val gCal = Calendar.getInstance()
        gCal.set(gy, gm - 1, gd)
        val dayOfWeek = gCal.get(Calendar.DAY_OF_WEEK)

        val jDate = gregorianToJalali(gy, gm, gd)
        val jy = jDate[0]
        val jm = jDate[1]
        val jd = jDate[2]

        val dayOfWeekName = when (dayOfWeek) {
            Calendar.SUNDAY -> "یکشنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنجشنبه"
            Calendar.FRIDAY -> "جمعه"
            Calendar.SATURDAY -> "شنبه"
            else -> ""
        }

        // Determine holidays (Jalali-based and known Lunar for 1405-1406 SH)
        val holiday = getIranianHoliday(jy, jm, jd, dayOfWeek)

        return JalaliDate(jy, jm, jd, dayOfWeekName, persianMonths[jm - 1], holiday)
    }

    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(0, 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy2 = gy - 1600
        var gm2 = gm - 1
        var gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 0 until gm2) {
            var days = gDaysInMonth[i + 1]
            if (i == 1 && ((gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0)) {
                days++
            }
            gDayNo += days
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        var i = 0
        while (i < 12 && jDayNo >= jDaysInMonth[i + 1]) {
            jDayNo -= jDaysInMonth[i + 1]
            i++
        }
        jm = i + 1
        jd = jDayNo + 1

        return intArrayOf(jy, jm, jd)
    }

    private fun getIranianHoliday(jy: Int, jm: Int, jd: Int, dayOfWeek: Int): String? {
        // Friday is the official weekend in Iran
        if (dayOfWeek == Calendar.FRIDAY) {
            return "تعطیلات آخر هفته (جمعه)"
        }

        // Solar / Jalali National Holidays
        if (jm == 1) {
            if (jd in 1..4) return "تعطیلات نوروز"
            if (jd == 12) return "روز جمهوری اسلامی ایران"
            if (jd == 13) return "روز طبیعت (سیزده بدر)"
        }
        if (jm == 3) {
            if (jd == 14) return "رحلت حضرت امام خمینی"
            if (jd == 15) return "قیام خونین ۱۵ خرداد"
        }
        if (jm == 11 && jd == 22) {
            return "پیروزی انقلاب اسلامی ایران"
        }
        if (jm == 12 && jd == 29) {
            return "ملی شدن صنعت نفت ایران"
        }

        // Lunar Calendar mapping for year 1405 SH (2026-2027 AD)
        if (jy == 1405) {
            when (jm) {
                4 -> { // Tir 1405
                    if (jd == 4) return "تاسوعای حسینی"
                    if (jd == 5) return "عاشورای حسینی"
                    if (jd == 27) return "عید سعید اضحی (قربان)"
                }
                5 -> { // Mordad 1405
                    if (jd == 4) return "عید سعید غدیر خم"
                    if (jd == 13) return "اربعین حسینی"
                    if (jd == 21) return "رحلت پیامبر اکرم و شهادت امام حسن مجتبی"
                    if (jd == 23) return "شهادت امام رضا (ع)"
                    if (jd == 31) return "شهادت امام حسن عسکری (ع)"
                }
                6 -> { // Shahrivar 1405
                    if (jd == 10) return "میلاد رسول اکرم و امام جعفر صادق (ع)"
                }
                8 -> { // Aban 1405
                    if (jd == 23) return "شهادت حضرت فاطمه زهرا (س)"
                }
                10 -> { // Dey 1405
                    if (jd == 12) return "ولادت امام علی (ع)"
                    if (jd == 26) return "مبعث رسول اکرم (ص)"
                }
                11 -> { // Bahman 1405
                    if (jd == 14) return "ولادت امام مهدی (عج)"
                }
                12 -> { // Esfand 1405
                    if (jd == 20) return "شهادت امام علی (ع)"
                    if (jd == 29) return "عید سعید فطر"
                }
            }
        }

        // Lunar Calendar mapping for year 1406 SH (2027-2028 AD)
        if (jy == 1406) {
            when (jm) {
                1 -> {
                    if (jd == 1) return "عید سعید فطر (تعطیل دوم)"
                }
                2 -> {
                    if (jd == 22) return "شهادت امام جعفر صادق (ع)"
                }
                4 -> {
                    if (jd == 23) return "تاسوعای حسینی"
                    if (jd == 24) return "عاشورای حسینی"
                }
                5 -> {
                    if (jd == 15) return "عید سعید اضحی (قربان)"
                    if (jd == 23) return "عید سعید غدیر خم"
                }
            }
        }

        return null
    }
}
