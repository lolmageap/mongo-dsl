package com.example.kotlinmongo.collection

data class Receipt(
    val date: String,
    val card: String,
    val price: Long,
) {
    companion object {
        fun of(
            date: String,
            card: String,
            price: Long,
        ) = Receipt(
            date = date,
            card = card,
            price = price,
        )
    }
}