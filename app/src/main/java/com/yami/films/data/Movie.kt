package com.yami.films.data

/**
 * One movie, whether it came from TMDB (online) or from the built-in
 * offline list (assets/movies.csv).
 *
 * [text] is what the recommender reads: genres + overview + keywords.
 */
data class Movie(
    val id: Int,
    val title: String,
    val year: Int,
    val genres: String,
    val rating: Double,
    val overview: String,
    val keywords: String = "",
    val posterPath: String? = null
) {
    val text: String get() = "$genres $overview $keywords"

    fun posterUrl(): String? =
        posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }

    override fun toString(): String =
        if (year > 0) "$title ($year) ⭐ $rating" else "$title ⭐ $rating"
}
