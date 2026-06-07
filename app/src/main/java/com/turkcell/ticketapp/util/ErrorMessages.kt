package com.turkcell.ticketapp.util

import com.turkcell.data.network.ApiException
import com.turkcell.data.network.NetworkException

fun Throwable.toUserMessage(): String = when (this) {
    is ApiException -> when {
        code == 401 -> "Oturum süresi doldu, tekrar giriş yapın."
        code == 403 && (errorMessage?.contains("not_purchase_owner") == true) ->
            "Bu işlem size ait değil."
        code == 403 && (errorMessage?.contains("not_assigned") == true) ->
            "Bu etkinliğe atanmamışsınız."
        code == 404 && (errorMessage?.contains("ticket_not_found") == true) ->
            "Bilet bulunamadı veya QR kod geçersiz."
        code == 409 && (errorMessage?.contains("email_taken") == true) ->
            "Bu email zaten kayıtlı."
        code == 409 && (errorMessage?.contains("capacity_exceeded") == true) ->
            "Stok yetersiz, etkinliği yenileyin."
        code == 409 && (errorMessage?.contains("already_paid") == true) ->
            "Bu satın alma zaten ödenmiş."
        code == 409 && (errorMessage?.contains("already_used") == true) ->
            "Bu bilet daha önce kullanılmış."
        code in 500..599 -> "Sunucu şu anda cevap veremiyor."
        else -> errorMessage ?: "Beklenmeyen bir hata oluştu."
    }
    is NetworkException -> "İnternet bağlantısı yok."
    else -> message ?: "Bilinmeyen bir hata oluştu."
}

