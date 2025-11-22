package com.pantrychef.back.data.mapper

import com.pantrychef.back.data.local.entity.AlertEntity
import com.pantrychef.back.model.Alert
import com.pantrychef.back.model.enums.AlertType
import com.pantrychef.back.model.enums.Severity

object AlertMapper {
    fun entityToModel(entity: AlertEntity): Alert{
        return Alert(
            id = entity.id,
            productId = entity.productId,
            productName = entity.productName,
            alertType = AlertType.valueOf(entity.alertType),
            severity = Severity.valueOf(entity.severity),
            message = entity.message,
            createdAt = entity.createdAt,
            isResolved = entity.isResolved
        )
    }
    fun modelToEntity(model: Alert): AlertEntity{
        return AlertEntity(
            id = model.id,
            productId = model.productId,
            productName = model.productName,
            alertType = model.alertType.name,
            severity = model.severity.name,
            message = model.message,
            createdAt = model.createdAt,
            isResolved = model.isResolved
        )
    }
}