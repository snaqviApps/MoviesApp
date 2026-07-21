package edu.review.moviesappreview.data.movies

import com.google.gson.annotations.SerializedName

data class Result(
    val adult: Boolean?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("genre_ids") val genreIds: List<Int>?,
    val id: Int?,
    val title: String? = null,
    @SerializedName("original_name") val originCountry : List<String>? = null,
    @SerializedName("original_language") val originalLanguage: String?,
    @SerializedName("original_title") val originalTitle: String?,
    val overview: String?,
    val popularity: Double?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    val softcore : Boolean,
    val video: Boolean?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("vote_count") val voteCount: Int?
)