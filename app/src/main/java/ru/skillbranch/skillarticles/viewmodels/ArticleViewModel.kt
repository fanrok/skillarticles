package ru.skillbranch.skillarticles.viewmodels


import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository.getAppSettings
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format

class ArticleViewModel(private val articleId: String): BaseViewModel<ArticleState>(ArticleState()) {
    private val repository = ArticleRepository
    private var menuIsShow: Boolean = false

    init {
        subscribeOnDataSource(getArticleData()){article, state ->
            article ?: return@subscribeOnDataSource null
            state.copy(
                shareLink = article.shareLink,
                title = article.title,
                author = article.author,
                category = article.category,
                categoryIcon = article.categoryIcon,
                date = article.date.format()
            )
        }
        subscribeOnDataSource(getArticleContent()){content, state->
            content ?: return@subscribeOnDataSource null
            state.copy(
                isLoadingContent = false,
                content = content
            )
        }
        subscribeOnDataSource(getArticlePersonalInfo()){info, state->
            info ?: return@subscribeOnDataSource null
            state.copy(
                isBookmark = info.isBookmark,
                isLike = info.isLike
            )
        }
        subscribeOnDataSource(getAppSettings()){settings, state->
            state.copy(
                isDarkMode = settings.isDarkMode,
                isBigText = settings.isBigText
            )
        }
    }

    private fun getArticleContent(): LiveData<List<Any>?> {
        return repository.loadArticleContent(articleId)
    }

    private fun getArticleData(): LiveData<ArticleData?>{
        return repository.getArticle(articleId)
    }

    private fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?>{
        return  repository.loadArticlePersonalInfo(articleId)
    }

    fun handleToggleMenu(){
        updateState { state->
            state.copy(isShowMenu = !state.isShowMenu).also{menuIsShow = !state.isShowMenu}
        }
    }

    fun handleNightMode(){
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    fun handleUpText(){
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    fun handleDownText(){
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    fun handleBookmark(){
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))

        val msg = if(currentState.isBookmark) "Add to bookmarks" else "Remove from bookmarks"
        notify(Notify.TextMessage(msg))
    }

   fun handleLike(){
       val isLiked = currentState.isLike
       val toogleLike = {
           val info = currentState.toArticlePersonalInfo()
           repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
       }
       toogleLike()
       val msg = if(!isLiked) Notify.TextMessage("Mark is liked")
       else{
           Notify.ActionMessage(
               "Don`t like it anymore",
               "No, still like it",
               toogleLike
           )
       }
       notify((msg))
   }

    fun handleShare(){
        val msg = "Share is not implemented"
        notify(Notify.ErrorMessage(msg, "OK", null))
    }

    fun hideMenu(){
        updateState { it.copy(isShowMenu = false) }
    }

    fun showMenu(){
        updateState { it.copy(isShowMenu = menuIsShow) }
    }

    fun handleSearchQuery(query: String?){
        updateState { it.copy(searchQuery = query) }
    }

    fun handleIsSearch(isSearch: Boolean){
        updateState { it.copy(isSearch = isSearch) }
    }
}

data class ArticleState(
    val isAuth: Boolean = false,
    val isLoadingContent: Boolean = true,
    val isLoadingReview: Boolean = true,
    val isLike: Boolean = false,
    val isBookmark: Boolean = false,
    val isShowMenu: Boolean = false,
    val isBigText: Boolean = false,
    val isDarkMode: Boolean = false,
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val searchResults: List<Pair<Int, Int>> = emptyList(),
    val searchPosition: Int = 0,
    val shareLink: String? = null,
    val title: String? = null,
    val category: String? = null,
    val categoryIcon: Any? = null,
    val date: String? = null,
    val author: Any? = null,
    val poster: String? = null,
    val content: List<Any> = emptyList(),
    val reviews: List<Any> = emptyList()
)