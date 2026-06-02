package com.turkcell.data.mapper

import com.turkcell.core.domain.purchase.Purchase
import com.turkcell.core.domain.purchase.PurchaseItem
import com.turkcell.core.domain.purchase.PurchaseStatus
import com.turkcell.data.dto.purchase.PurchaseDto
import com.turkcell.data.dto.purchase.PurchaseItemDto

internal fun PurchaseDto.toDomain(): Purchase = Purchase(
    id = id,
    status = if (status == "PAID") PurchaseStatus.PAID else PurchaseStatus.PENDING,
    totalCents = totalCents,
    items = items.map { it.toDomain() },
    ticketIds = tickets.map { it.id },
)

internal fun PurchaseItemDto.toDomain(): PurchaseItem = PurchaseItem(
    ticketTypeId = ticketTypeId,
    quantity = quantity,
    unitPriceCents = unitPriceCents,
)