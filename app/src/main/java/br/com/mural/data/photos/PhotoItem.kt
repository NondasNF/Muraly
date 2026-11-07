package br.com.mural.data.photos

data class PhotoItem(
    val mediaId: Long,
    val contentUri: String,
    val takenAtMillis: Long,
    val width: Int,
    val height: Int
)
