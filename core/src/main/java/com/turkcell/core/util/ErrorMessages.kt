package com.turkcell.core.util

 fun Throwable.toUserMessage(): String {
    // Sınıf adını string olarak kontrol ediyoruz, böylece import etmeye gerek kalmıyor
    val exceptionName = this::class.java.simpleName
    val errorMsg = this.message ?: ""

    return when {
        // Data katmanındaki ApiException'ı ismi üzerinden yakalıyoruz
        exceptionName == "ApiException" -> {
            // Yansıma (Reflection) veya Throwable'ın içindeki alanı dolaylı yoldan okuma yöntemi.
            // Eğer ApiException içinde kod alanı varsa ve bunu Throwable üzerinden alamıyorsak,
            // mesaj metninin içindeki HTTP kodlarını veya string içeriklerini kontrol edebiliriz:
            when {
                errorMsg.contains("401") -> "Email veya şifre hatalı"
                errorMsg.contains("403") || errorMsg.contains("not_purchase_owner") -> "Bu satın alım işleminin sahibi siz değilsiniz."
                errorMsg.contains("409") || errorMsg.contains("capacity_exceeded") -> "Stok yetersiz, yenile"
                errorMsg.contains("already_paid") -> "Bu işlem için zaten ödeme yapılmış."
                errorMsg.contains("500") || errorMsg.contains("503") -> "Sunucu şu anda cevap veremiyor"
                else -> "Beklenmeyen bir hata oluştu"
            }
        }
        // Data katmanındaki NetworkException'ı ismi üzerinden yakalıyoruz
        exceptionName == "NetworkException" -> {
            "İnternet bağlantısı yok"
        }
        else -> {
            // Eğer üsttekiler tutmazsa ham hata mesajlarında spesifik anahtar kelimeleri arıyoruz
            when {
                errorMsg.contains("capacity_exceeded") -> "Stok yetersiz, yenile"
                errorMsg.contains("already_paid") -> "Bu işlem için zaten ödeme yapılmış."
                errorMsg.contains("not_purchase_owner") -> "Bu satın alım işleminin sahibi siz değilsiniz."
                else -> errorMsg.ifBlank { "Bilinmeyen bir hata oluştu." }
            }
        }
    }
}