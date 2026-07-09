// Team5
// @author: Deng Yunke

package com.example.wellnessapp.ui.ai

data class PagedHistoryUiState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val page: Int = -1,
    val totalPages: Int = 0,
    val hasMore: Boolean = false
) {
    val isEmpty: Boolean
        get() = !isLoading && !isLoadingMore && errorMessage == null && items.isEmpty()
}
