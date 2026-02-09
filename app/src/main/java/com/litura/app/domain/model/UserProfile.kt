package com.litura.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileData(
    val userId: String,
    val createdAt: String,
    val identity: UserIdentity,
    val subscription: Subscription
)

@Serializable
data class UserIdentity(
    val displayName: String,
    val avatarId: String,
    val privacyMode: String
)

@Serializable
data class Subscription(
    val tier: String,
    val entitlements: Entitlements
)

@Serializable
data class Entitlements(
    val unlimitedHealth: Boolean,
    val unlimitedReviewText: Boolean
)

enum class PurchaseState {
    NOT_OWNED,
    OWNED_NOT_DOWNLOADED,
    OWNED_DOWNLOADED
}
