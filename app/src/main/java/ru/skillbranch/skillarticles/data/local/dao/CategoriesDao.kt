package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ru.skillbranch.skillarticles.data.local.entities.Category
import ru.skillbranch.skillarticles.data.local.entities.CategoryData

@Dao
interface CategoriesDao:BaseDao<Category> {

    @Query("""
        select category.title as title, category.icon, category.category_id as category_id, count(article.category_id) as articles_count
        from article_categories as category
        inner join articles as article on category.category_id = article.category_id
        group by category.category_id
        order by articles_count desc
    """)
    fun findAllCategoriesData():LiveData<List<CategoryData>>
}