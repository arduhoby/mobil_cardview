package com.kartview.app.data.model

enum class DataType {
    Photo,
    Note,
    Location,
}

data class DemoItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val type: DataType,
    val imageUrl: String?,
)