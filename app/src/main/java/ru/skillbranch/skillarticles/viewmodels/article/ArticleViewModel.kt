package ru.skillbranch.skillarticles.viewmodels.article

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.models.CommentItemData
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.data.repositories.CommentsDataFactory
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.data.repositories.clearContent
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.extensions.shortFormat
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class ArticleViewModel(
        handle: SavedStateHandle,
        private val articleId: String
) : BaseViewModel<ArticleState>(handle, ArticleState()), IArticleViewModel {

    private val repository = ArticleRepository
    private var clearContent: String? = null

    private val listConfig by lazy {
        PagedList.Config.Builder()
                .setEnablePlaceholders(true) // мы знаем общее число комментов к статье
                .setPageSize(5)
                .build()
    }

    //
    private val commentsListData = Transformations.switchMap(
            repository.findArticleCommentCount(articleId)
    ) {
        // Текущее количество комментариев у статьи известно заранее
        buildPageList(repository.loadAllComments(articleId, it))
    }

    // subscribe on mutable data
    init {
        subscribeOnDataSource(repository.findArticle(articleId)) { article, state ->
            if (article.content == null) fetchContent()
            state.copy(
                    shareLink = article.shareLink,
                    title = article.title,
                    category = article.category.title,
                    categoryIcon = article.category.icon,
                    date = article.date.shortFormat(),
                    author = article.author,
                    isBookmark = article.isBookmark,
                    isLike = article.isLike,
                    content = article.content ?: emptyList(),
                    isLoadingContent = article.content == null,
                    source = article.source,
                    tags = article.tags
            )
        }
        subscribeOnDataSource(repository.getAppSettings()) { settings, state ->
            state.copy(
                    isDarkMode = settings.isDarkMode,
                    isBigText = settings.isBigText
            )
        }
        subscribeOnDataSource(repository.isAuth()) { isAuth, state ->
            state.copy(isAuth = isAuth)
        }
    }

    private fun fetchContent() {
        viewModelScope.launch(Dispatchers.IO) { repository.fetchArticleContent(articleId) }
    }

    // personal article info
    override fun handleLike() {
        val isLiked = currentState.isLike
        val msg = if (!isLiked) Notify.TextMessage("Mark is liked")
        else Notify.ActionMessage(
                "Don`t like it anymore", // snackbar message
                "No, still like it" // action btn on snackbar
        ) {
            handleLike()
        } // handler, if action btn will be pressed

        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleLike(articleId)
            if (!isLiked) repository.incrementLike(articleId)
            else repository.decrementLike(articleId)
            withContext(Dispatchers.Main) {
                notify(msg)
            }
        }
    }

    // personal article info
    override fun handleBookmark() {
        val msg = if (!currentState.isBookmark) "Add to bookmarks"
        else "Remove from bookmarks"
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(articleId)
            withContext(Dispatchers.Main) { notify(Notify.TextMessage(msg)) }
        }
    }

    override fun handleShare() {
        val msg = "Share is not implemented"
        notify(Notify.ErrorMessage(msg, "OK", null))
    }

    // session state
    override fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }

    override fun handleUpTextSize() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
        updateState { it.copy(isBigText = true) }
    }

    override fun handleDownTextSize() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
        updateState { it.copy(isBigText = false) }
    }

    // app settings
    override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        val newSettings = settings.copy(isDarkMode = !settings.isDarkMode)
        repository.updateSettings(newSettings)
        updateState { it.copy(isDarkMode = newSettings.isDarkMode) }
    }

    override fun handleIsSearch(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch, isShowMenu = false, searchPosition = 0) }
    }

    override fun handleSearchQuery(query: String?) {
        query ?: return
        if (clearContent == null && currentState.content.isNotEmpty())
            clearContent = currentState.content.clearContent()

        val results = clearContent
                .indexesOf(query)
                .map {
                    it to it + query.length
                }
        updateState {
            it.copy(searchQuery = query, searchResults = results, searchPosition = 0)
        }
    }

    fun handleUpResult() {
        updateState {
            //            val newPos = when {
//                it.searchPosition.dec() < 0 -> it.searchResults.lastIndex
//                else -> it.searchPosition.dec()
//            }
//            it.copy(searchPosition = newPos)
            it.copy(searchPosition = it.searchPosition.dec())
        }
    }

    fun handleDownResult() {
        updateState {
            //            val newPos = when {
//                it.searchPosition.inc() > it.searchResults.lastIndex -> 0
//                else -> it.searchPosition.inc()
//            }
//            it.copy(searchPosition = newPos)
            it.copy(searchPosition = it.searchPosition.inc())
        }
    }

    fun handleCopyCode() {
        notify(Notify.TextMessage("Code copy to clipboard"))
    }

    fun handleSendComment(comment: String?) {
        if (comment.isNullOrBlank()) {
            notify(Notify.TextMessage("Comment must not be empty"))
            return
        }
        updateState { it.copy(comment = comment) }
        if (!currentState.isAuth) {
            navigate(NavigationCommand.StartLogin())
        } else viewModelScope.launch(Dispatchers.IO) {
            repository.sendMessage(articleId, comment, currentState.answerToSlug)
            withContext(Dispatchers.Main) {
                updateState {
                    it.copy(
                            answerTo = null,
                            answerToSlug = null, comment = null
                    )
                }
            }
        }
    }

    fun observeCommentList(
            owner: LifecycleOwner,
            onChange: (list: PagedList<CommentItemData>) -> Unit
    ) {
        commentsListData.observe(owner, Observer { onChange(it) })
    }

    private fun buildPageList(
            dataFactory: CommentsDataFactory
    ): LiveData<PagedList<CommentItemData>> {
        return LivePagedListBuilder<String, CommentItemData>(
                dataFactory,
                listConfig
        ).setFetchExecutor(Executors.newSingleThreadExecutor()).build()
    }

    fun handleCommentFocus(hasFocus: Boolean) {
        updateState { it.copy(showBottombar = !hasFocus) }
    }

    fun handleClearComment() {
        updateState { it.copy(answerTo = null, answerToSlug = null) }
    }

    fun handleReplyTo(slug: String, name: String) {
        updateState { it.copy(answerTo = "Reply to $name", answerToSlug = slug) }
    }

    fun saveComment(comment: String) {
        updateState { it.copy(comment = comment) }
    }
}

//============================================================================

data class ArticleState(
        val isAuth: Boolean = false, // пользователь авторизован
        val isLoadingContent: Boolean = true, // контент загружается
        val isLoadingReviews: Boolean = true, // отзывы загружаются
        val isLike: Boolean = false, // отмечено как Like
        val isBookmark: Boolean = false, // в закладках
        val isShowMenu: Boolean = false, // отображается меню
        val isBigText: Boolean = false, // шрифт увеличен
        val isDarkMode: Boolean = false, // темный режим
        val isSearch: Boolean = false, // режим поиска
        val searchQuery: String? = null, // поисковый запрос
        // результаты поиска (список стартовых/конечных позиций фрагментов)
        val searchResults: List<Pair<Int, Int>> = emptyList(),
        // текущая индексная (zero-based) позиция найденного результата
        val searchPosition: Int = 0,
        val shareLink: String? = null, // ссылка Share
        val title: String? = null, // заголовок статьи
        val category: String? = null, // категория
        val categoryIcon: Any? = null, // иконка категории ::signature is verified
        val date: String? = null, // дата публикации
        val author: Any? = null, // автор статьи
        val poster: String? = null, // обложка статьи
        val content: List<MarkdownElement> = emptyList(), // контент
        val commentsCount: Int = 0, // число комментариев к статье
        /** Подсказка, появляющаяся в редактируемом поле комментария. Если
         * юзер отвечает на комментарий юзера [name], то подсказка будет вида -
         * Reply to [name]. Если юзер комментирует саму статью, то подсказка
         * будет вида - Comment */
        val answerTo: String? = null,
        /** Текстовый ключ (slug) коммента, в ответ на который в данный момент
         * (ArticleState в момент редактирования комментария) юзер набирает
         * свой комментарий. Если юзер комментирует саму статью, то данное
         * значение - null */
        val answerToSlug: String? = null,
        val showBottombar: Boolean = true, // при написании коммента боттомбар д/б скрыт
        val comment: String? = null,
        val source: String? = null,
        val tags: List<String> = emptyList()
) : IViewModelState {
    override fun save(outState: SavedStateHandle) {
        outState.set("isSearch", isSearch)
        outState.set("searchQuery", searchQuery)
        outState.set("searchResults", searchResults)
        outState.set("searchPosition", searchPosition)
    }

    @Suppress("UNCHECKED_CAST")
    override fun restore(savedState: SavedStateHandle): ArticleState {
        return copy(
                isSearch = savedState["isSearch"] ?: false,
                searchQuery = savedState["searchQuery"],
                searchResults = savedState["searchResults"] ?: emptyList(),
                searchPosition = savedState["searchPosition"] ?: 0
        )
    }
}

