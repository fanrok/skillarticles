package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.skillbranch.skillarticles.data.local.entities.ArticleTagXRef
import ru.skillbranch.skillarticles.data.local.entities.Tag

@Dao
interface TagsDao:BaseDao<Tag> {

    @Query("""
        select tag from article_tags
        order by use_count desc
    """)
    fun findTags(): LiveData<List<String>>

    @Query("""
        update article_tags set use_count = use_count+1
        where tag = :tag
        """)
    fun incrementTagUseCount(tag :String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRefs(refs: List<ArticleTagXRef>):List<Long>
}