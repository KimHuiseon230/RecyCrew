package com.piooda.domain.repository

import com.piooda.domain.model.DetailedImageData

interface ImageDataRepository {
    suspend fun fetchBasicImagesData(): Result<List<DetailedImageData>>
    suspend fun fetchDetailedImageData(): Result<List<DetailedImageData>>
}