package com.piooda.data.repositoryImpl

import com.piooda.data.model.Content
import com.piooda.data.model.ContentDto
import java.util.Date

class ContentMapper {

    companion object {
        fun ContentDto.toDomain(): Content {
            fun generateSearchIndex(text: String): List<String> {
                val indexList = mutableSetOf<String>()
                val words = text.lowercase().split(" ")

                words.forEach { word ->
                    for (i in 1..word.length) {
                        indexList.add(word.substring(0, i)) // 🔥 부분 검색 가능하도록 저장
                    }
                }

                return indexList.toList()
            }
            return Content(
                id = this.id,
                title = this.title,
                content = this.content,
                createdDate = this.createdDate ?: Date(),
                favoriteCount = this.favoriteCount,
                imagePath = this.imagePath,
                commentCount = this.commentCount,
                viewCount = this.viewCount,
                favorites = this.favorites,
                searchIndex = generateSearchIndex("$title $content") // 🔥 자동 검색 인덱스 생성
            )

        }

        fun Content.toMap(): Map<String, Any> {
            return mapOf(
                "id" to (id ?: ""),
                "title" to title,
                "content" to content,
                "createdDate" to createdDate,
                "favoriteCount" to favoriteCount,
                "imagePath" to imagePath,
                "commentCount" to commentCount,
                "viewCount" to viewCount,
                "favorites" to favorites,
                "searchIndex" to searchIndex // 🔥 검색 인덱스 저장
            )
        }

        fun Content.toContentDto(): ContentDto {
            return ContentDto(
                id = this.id,
                title = this.title,
                content = this.content,
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
