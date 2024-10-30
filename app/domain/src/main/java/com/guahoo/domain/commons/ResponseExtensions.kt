package com.guahoo.domain.commons

sealed class ResultState<out T> {
    object PreAction : ResultState<Nothing>()
    class Loading(val message: String) : ResultState<Nothing>()
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error(val message: String) : ResultState<Nothing>()
}
