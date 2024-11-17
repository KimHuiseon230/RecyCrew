package com.piooda.data.repository

import com.piooda.data.model.DetailedImageData

interface ImageDataRepository {
    suspend fun fetchBasicImagesData(): Result<List<DetailedImageData>>
    suspend fun fetchDetailedImageData(): Result<List<DetailedImageData>>
}