package com.pantrychef.back.model

data class Alert(
    val id: String,
    val productId: String,
    val productName: String,
    val alertType: AlertType,
    val severity: Severity,
    val message: String,
    val createdAt: Long,
    val isResolved: Boolean = false
)