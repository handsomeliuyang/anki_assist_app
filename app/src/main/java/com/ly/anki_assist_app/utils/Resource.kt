package com.ly.anki_assist_app.utils

enum class Status {
    IDLE,
    SUCCESS,
    ERROR,
    LOADING
}

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, "")
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(msg: String = "", data: T?): Resource<T> {
            return Resource(Status.LOADING, data, msg)
        }

        fun <T> idle(): Resource<T> {
            return Resource(Status.IDLE, null, "")
        }
    }
}