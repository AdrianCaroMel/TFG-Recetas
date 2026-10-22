package es.tfg.recetas.ui.common

import es.tfg.recetas.core.model.MeasureUnit
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private val spanish: Locale = Locale.forLanguageTag("es-ES")

fun formatEuros(cents: Number): String =
    NumberFormat.getCurrencyInstance(spanish).format(cents.toDouble() / 100.0)

fun formatQuantity(quantity: Double, unit: MeasureUnit): String {
    val number = NumberFormat.getNumberInstance(spanish).apply { maximumFractionDigits = 1 }
    return when (unit) {
        MeasureUnit.GRAM -> if (quantity >= 1000) "${number.format(quantity / 1000)} kg" else "${quantity.roundToInt()} g"
        MeasureUnit.MILLILITER -> if (quantity >= 1000) "${number.format(quantity / 1000)} L" else "${quantity.roundToInt()} ml"
        MeasureUnit.UNIT -> {
            val n = number.format(quantity)
            if (quantity <= 1.0) "$n unidad" else "$n unidades"
        }
    }
}
