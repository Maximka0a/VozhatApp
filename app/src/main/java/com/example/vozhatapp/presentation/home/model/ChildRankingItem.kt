package com.example.vozhatapp.presentation.home.model

data class ChildRankingItem(
    val id: Long,
    val name: String,
    val lastName: String,
    val squadName: String,
    val points: Int,
    val photoUrl: String? = null
)