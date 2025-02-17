package cn.fjc920.composetest.viewModel

/**
 * Represents the result of a save operation.
 */
sealed class SaveResult {
    data class Success(val path: String) : SaveResult()
    data class Failure(val reason: String) : SaveResult()
}