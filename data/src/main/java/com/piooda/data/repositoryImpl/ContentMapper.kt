package com.piooda.data.repositoryImpl

import com.piooda.data.model.Content
import com.piooda.data.model.ContentDto
import java.util.Date

class ContentMapper {
    companion object {
        fun ContentDto.toDomain(): Content {
            return Content(
                id = this.id,
                title = this.title,
                content = this.content,
                category = this.category,
                createdDate = this.createdDate ?: Date(),
                favoriteCount = this.favoriteCount,
                imagePath = this.imagePath,
                commentCount = this.commentCount,
                viewCount = this.viewCount,
                favorites = this.favorites
            )
        }
        fun ContentDto.toContent(): Content {
            return Content(
                id = this.id,
                title = this.title,
                content = this.content,
                category = this.category,
                createdDate = this.createdDate ?: Date(),
                favoriteCount = this.favoriteCount,
                imagePath = this.imagePath,
                commentCount = this.commentCount,
                viewCount = this.viewCount,
                favorites = this.favorites
            )
        }
        fun Content.toMap(): Map<String, Any> {
            return mapOf(
                "id" to (id ?: ""),
                "title" to title,
                "content" to content,
                "category" to category,
                "createdDate" to createdDate,
                "favoriteCount" to favoriteCount,
                "imagePath" to imagePath,
                "commentCount" to commentCount,
                "viewCount" to viewCount,
                "favorites" to favorites
            )
        }

        fun Content.toContentDto(): ContentDto {
            return ContentDto(
                id = this.id,
                title = this.title,
                content = this.content,
                category = this.category,
                createdDate = this.createdDate,
                favoriteCount = this.favoriteCount,
                imagePath = this.imagePath,
                commentCount = this.commentCount,
                viewCount = this.viewCount,
                favorites = this.favorites
            )
        }
        fun ContentDto.Comment.toDomainComment(): Content.Comment {
            return Content.Comment(
                author = this.author,
                content = this.content,
                timestamp = this.timestamp
            )
        }
    }
}