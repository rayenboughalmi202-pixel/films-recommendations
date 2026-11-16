package com.yami.films.data

import android.content.Context

/**
 * Offline fallback / demo mode.
 * Loads the built-in film list from assets/movies.csv
 * (format: title;year;genres;rating;keywords — one movie per line).
 * Used when there is no TMDB key or no internet connection.
 */
object OfflineMovies {

    fun load(context: Context): List<Movie> {
        val movies = mutableListOf<Movie>()
        context.assets.open("movies.csv").bufferedReader().useLines { lines ->
            lines.drop(1) // header
                .filter { it.isNotBlank() }
                .forEachIndexed { index, line ->
                    val p = line.split(";")
                    if (p.size >= 5) {
                        movies += Movie(
                            id = -(index + 1), // negative ids = offline movies
                            title = p[0].trim(),
                            year = p[1].trim().toIntOrNull() ?: 0,
                            genres = p[2].trim(),
                            rating = p[3].trim().toDoubleOrNull() ?: 0.0,
                            overview = p[4].trim()
                        )
                    }
                }
        }
        return movies
    }

    fun search(movies: List<Movie>, query: String): List<Movie> =
        movies.filter { it.title.contains(query, ignoreCase = true) }
}
