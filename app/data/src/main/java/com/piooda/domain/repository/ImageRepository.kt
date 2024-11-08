package com.piooda.domain.repository

import com.piooda.domain.model.ImageData

interface ImageRepository {
    suspend fun fetchImageData(): Result<List<ImageData>>
}