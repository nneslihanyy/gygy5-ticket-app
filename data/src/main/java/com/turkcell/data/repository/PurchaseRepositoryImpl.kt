package com.turkcell.data.repository

import com.turkcell.core.domain.purchase.Purchase
import com.turkcell.core.domain.purchase.PurchaseItem
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.data.dto.purchase.CreatePurchaseRequestDto
import com.turkcell.data.dto.purchase.PurchaseItemRequestDto
import com.turkcell.data.mapper.toDomain
import com.turkcell.data.remote.PurchaseApi
import com.turkcell.data.util.runCatchingApi

class PurchaseRepositoryImpl(
    private val purchaseApi: PurchaseApi,
) : PurchaseRepository {

    override suspend fun createPurchase(items: List<PurchaseItem>): Result<Purchase> =
        runCatchingApi {
            purchaseApi.createPurchase(
                CreatePurchaseRequestDto(
                    items = items.map { PurchaseItemRequestDto(it.ticketTypeId, it.quantity) }
                )
            )
        }.map { it.toDomain() }

    override suspend fun pay(purchaseId: String): Result<Purchase> =
        runCatchingApi { purchaseApi.pay(purchaseId) }.map { it.toDomain() }

    override suspend fun getPurchase(purchaseId: String): Result<Purchase> =
        runCatchingApi { purchaseApi.getPurchase(purchaseId) }.map { it.toDomain() }
}