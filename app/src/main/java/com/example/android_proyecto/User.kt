package com.example.android_proyecto

data class User(
    val id: String? = null,
    val username: String? = null,
    val password: String? = null,
    val email: String? = null,
    val userPosition: Position? = null,
    val userInventory: Inventory? = null
)

data class Position(
    val x: Double? = null,
    val y: Double? = null
)

data class Inventory(
    val coins: Int? = null,
    val items: List<String>? = null
)
