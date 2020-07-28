package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import ru.skillbranch.skillarticles.data.local.entities.Article
import ru.skillbranch.skillarticles.data.local.entities.ArticleFull
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
import ru.skillbranch.skillarticles.data.local.entities.ArticlePersonalInfo

@Dao
interface ArticlesDao: BaseDao<Article> {
    @Transaction
    fun upsert(list: List<Article>){
        insert(list)
                .mapIndexed { index, recordResult -> if (recordResult == -1L) list[index] else null }
                .filterNotNull()
                .also { if (it.isNotEmpty()) update(it) }
    }

    @Query("""
        select * from articles
    """)
    fun findArticles(): List<Article>

    @Query("""
        select * from articles
        where id = :id
    """)
    fun findArticleById(id: String): Article

    @Query("""
        select * from ArticleItem
    """)
    fun findArticleItems(): List<ArticleItem>

    @Query("""
        select  * from ArticleItem
        where category_id in (:categoryIds)
    """)
    fun findArticleItemsByCategoryIds(categoryIds: List<String>): List<ArticleItem>

    @Query("""
        select * from ArticleItem
        inner join article_tag_x_ref as refs on refs.a_id = id
        where refs.t_id = :tag
    """)
    fun findArticlesByTagId(tag: String):List<ArticleItem>

    @RawQuery(observedEntities = [ArticleItem::class])
    fun findArticlesByRaw(simpleSQLiteQuery: SimpleSQLiteQuery):DataSource.Factory<Int, ArticleItem>

    @Query("""
        select * from ArticleFull
        where id = :articleId
    """)
    fun findFullArticle(articleId: String):LiveData<ArticleFull>

}