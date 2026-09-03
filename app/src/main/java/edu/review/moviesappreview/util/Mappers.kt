package edu.review.moviesappreview.util

import retrofit2.Response

/**
 * Maps the Remote Response to a Generic Result type.
 */
fun <T> Response<T>.toResult() : Result<T> {
    return if (this.isSuccessful) {
        val body = body()
        if (body != null) {
            Result.success(body)
        } else {
            Result.failure(Exception("Response body is null"))
        }
    } else {
        Result.failure(Exception("Request failed with code: ${code()}"))
    }
}