package com.recipehub.app.data.model

import com.recipehub.app.data.local.CollectionEntity

data class Collection(
    val id: Long,
    val name: String,
)

fun CollectionEntity.toDomain(): Collection = Collection(id = id, name = name)
