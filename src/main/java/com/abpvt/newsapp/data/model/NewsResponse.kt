package com.abpvt.newsapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Top-level Guardian API response wrapper.
 *
 * JSON shape:
 * {
 *   "response": {
 *     "status": "ok",
 *     "total": 123,
 *     "results": [ ...Article... ]
 *   }
 * }
 */
data class NewsResponse(
    @SerializedName("response") val response: GuardianResponse
) {
    data class GuardianResponse(
        @SerializedName("status")  val status: String,
        @SerializedName("total")   val total: Int,
        @SerializedName("results") val results: List<Article>
    )

    // Convenience getter so repository code stays the same
    val articles: List<Article> get() = response.results
    val status: String          get() = response.status
}
