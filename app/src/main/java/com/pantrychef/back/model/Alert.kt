package com.pantrychef.back.model

import com.pantrychef.back.model.enums.AlertType
import com.pantrychef.back.model.enums.Severity

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