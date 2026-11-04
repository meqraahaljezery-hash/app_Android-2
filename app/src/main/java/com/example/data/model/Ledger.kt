package com.example.data.model

enum class LedgerEntryType {
    DEPOSIT_PAYMENT,
    DEDUCTION_SESSION,
    MANUAL_ADJUSTMENT_ADMIN,
    WELCOME_BONUS,
    HONOR_AWARD
}

data class MinuteLedgerEntry(
    val id: String,
    val studentId: String,
    val studentName: String,
    val changeAmount: Int, // e.g. +200 or -15
    val balanceBefore: Int,
    val balanceAfter: Int,
    val type: LedgerEntryType,
    val reason: String,
    val timestamp: String,
    val actorName: String = "النظام"
)

enum class PaymentStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class PaymentRequest(
    val id: String,
    val studentId: String,
    val studentName: String,
    val studentCode: String = "JZR-1042",
    val senderFullName: String,
    val transactionRef: String,
    val senderPhoneNumber: String,
    val targetBank: String, // CBE, BOA, Telebirr, M-Pesa
    val packageMinutes: Int,
    val amountPaidEtb: Int,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val submittedAt: String,
    val reviewedByAdmin: String? = null,
    val rejectionReason: String? = null,
    val receiptUri: String? = null,
    val receiptLocalPath: String? = null,
    val isDownloaded: Boolean = false,
    val isArchived: Boolean = false
)

data class MinutePackage(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val minutes: Int,
    val priceEtb: Int,
    val isPopular: Boolean = false,
    val features: List<String> = emptyList()
)

data class BankAccount(
    val id: String,
    val bankCode: String,
    val bankNameAr: String,
    val accountNumber: String,
    val accountHolder: String = "Kalid Ahmed Mustefa",
    val instructions: String = "يرجى كتابة اسمك في وصف التحويل ونسخ رقم الإشعار"
)
