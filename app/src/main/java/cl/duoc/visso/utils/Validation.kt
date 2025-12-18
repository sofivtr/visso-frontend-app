package cl.duoc.visso.utils

import java.util.regex.Pattern

/**
 * Objeto que contiene validaciones simples de RUT y Email
 */
object Validation {

    /**
     * Valida el RUT chileno
     * Reglas: Formato XX.XXX.XXX-X o XXXXXXXX-X, dígito verificador correcto
     */
    fun validateRut(rut: String): ValidationResult {
        if (rut.isBlank()) {
            return ValidationResult.Error("El RUT es requerido")
        }

        // Limpiar el RUT (quitar puntos y guión)
        val cleanRut = rut.replace(".", "").replace("-", "").trim()

        if (cleanRut.length < 8 || cleanRut.length > 9) {
            return ValidationResult.Error("RUT inválido")
        }

        try {
            val rutNumber = cleanRut.substring(0, cleanRut.length - 1).toInt()
            val dv = cleanRut.last().uppercaseChar()

            // Calcular dígito verificador
            var m = 0
            var s = 1
            var t = rutNumber
            while (t > 0) {
                s = (s + (t % 10) * (9 - m++ % 6)) % 11
                t /= 10
            }
            val dvCalculado = if (s > 0) (s - 1).toString().first() else 'K'

            if (dv != dvCalculado) {
                return ValidationResult.Error("RUT inválido: dígito verificador incorrecto")
            }

            return ValidationResult.Success
        } catch (e: Exception) {
            return ValidationResult.Error("RUT inválido")
        }
    }

    /**
     * Valida el email
     * Reglas: No vacío, formato de email válido con @
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("El correo electrónico es requerido")
            !Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
            ).matcher(email).matches() -> ValidationResult.Error("Correo electrónico inválido")
            else -> ValidationResult.Success
        }
    }
}

/**
 * Resultado de una validación
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

/**
 * Extension function para formatear RUT mientras se escribe
 */
fun String.formatRut(): String {
    val clean = this.replace(".", "").replace("-", "")
    if (clean.length <= 1) return clean

    val rut = clean.substring(0, clean.length - 1)
    val dv = clean.last()

    val formatted = StringBuilder()
    var count = 0
    
    for (i in rut.length - 1 downTo 0) {
        if (count == 3) {
            formatted.insert(0, ".")
            count = 0
        }
        formatted.insert(0, rut[i])
        count++
    }
    
    formatted.append("-")
    formatted.append(dv)
    
    return formatted.toString()
}
