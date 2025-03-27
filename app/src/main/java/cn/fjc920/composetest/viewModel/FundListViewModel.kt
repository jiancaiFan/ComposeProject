package cn.fjc920.composetest.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FundListViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<String>>(emptyList())
    val items: StateFlow<List<String>> = _items

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError

    private var loadType = LoadType.FAILURE // Default load type

    init {
        loadInitialItems()
    }

    fun setLoadType(type: LoadType) {
        loadType = type
    }

    private fun loadInitialItems() {
        _items.value = List(15) { "Item ${it + 1}" }
    }

    fun loadMoreItems() {
        _isLoading.value = true
        _hasError.value = false
        viewModelScope.launch {
            delay(2000) // Simulate network delay
            if (loadType == LoadType.SUCCESS) {
                val newItems = _items.value + List(15) { "Item ${_items.value.size + it + 1}" }
                _items.value = newItems
                _isLoading.value = false
            } else {
                _isLoading.value = false
                _hasError.value = true
            }
        }
    }

    fun retryLoadMoreItems() {
        _isLoading.value = true
        _hasError.value = false // Hide error message during retry
        viewModelScope.launch {
            delay(2000) // Simulate network delay
            if (loadType == LoadType.SUCCESS) {
                val newItems = _items.value + List(15) { "Item ${_items.value.size + it + 1}" }
                _items.value = newItems
                _isLoading.value = false
                _hasError.value = false
            } else {
                _isLoading.value = false
                _hasError.value = true
            }
        }
    }
}

enum class LoadType {
    SUCCESS,
    FAILURE
}