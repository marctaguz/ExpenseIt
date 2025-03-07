package com.example.expenseit.utils

import androidx.room.TypeConverter
import java.math.BigDecimal

//TypeConverter because Room does not support BigDecimal directly
class BigDecimalConverter {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String {
        return value?.toPlainString() ?: "0.00"
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal {
        return value?.toBigDecimalOrNull()?.setScale(2, BigDecimal.ROUND_HALF_UP) ?: BigDecimal("0.00")
    }
}
