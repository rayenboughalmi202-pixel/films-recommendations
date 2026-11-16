package com.yami.films.data

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Very small TMDB client using only the Android/Java standard library
 * (HttpURLConnection + org.json) — no extra libraries to install.
 *
 * The API key is the user's own free TMDB key. It is never stored in code:
 * it comes from the in-app Settings field (SharedPreferences) or, at build
 * time, from local.properties (see app/build.gradle.kts).
 *
 * Flow used by the app:
 *  searchMovie()       -> find almost any movie by title
 *  movieKeywords()     -> extra words that describe the chosen movie
 *  candidatePool()     -> popular + top-rated movies to compare against
 *  Then Recommender ranks that pool with TF-IDF + cosine similarity,
 *  on the phone — we do NOT just copy TMDB's own ordering.
 */
class TmdbClient(private val apiKey: String) {

    private fun getJson(url: String): JSONObject {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        conn.setRequestProperty("Accept", "application/json")
        conn.inputStream.bufferedReader().use { reader ->
            return JSONObject(reader.readText())
        }
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")

    fun searchMovie(query: String): List<Movie> {
        val url = "https://api.themoviedb.org/3/search/movie" +
            "?api_key=$apiKey&query=${enc(query)}&include_adult=false"
        val results = getJson(url).getJSONArray("results")
        return (0 until results.length()).map { i ->
            val o = results.getJSONObject(i)
            Movie(
                id = o.getInt("id"),
                title = o.optString("title", "Unknown"),
                year = o.optString("release_date", "").take(4).toIntOrNull() ?: 0,
                genres = "", // filled in by movieDetails()
                rating = o.optDouble("vote_average", 0.0),
                overview = o.optString("overview", ""),
                posterPath = if (o.isNull("poster_path")) null else o.getString("poster_path")
            )
        }
    }

    /** Adds the real genre names to a movie found by search. */
    fun movieDetails(movie: Movie): Movie {
        val url = "https://api.themoviedb.org/3/movie/${movie.id}?api_key=$apiKey"
        val o = getJson(url)
        val genres = o.optJSONArray("genres")?.let { arr ->
            (0 until arr.length()).joinToString(" ") { arr.getJSONObject(it).getString("name") }
        } ?: ""
        return movie.copy(genres = genres)
    }

    fun movieKeywords(movieId: Int): String {
        val url = "https://api.themoviedb.org/3/movie/$movieId/keywords?api_key=$apiKey"
        val arr = getJson(url).optJSONArray("keywords") ?: return ""
        return (0 until arr.length()).joinToString(" ") { arr.getJSONObject(it).getString("name") }
    }

    /** Popular + top-rated movies (with genres resolved), deduplicated. */
    fun candidatePool(pages: Int = 2): List<Movie> {
        val genreMap = genreMap()
        val seen = mutableSetOf<Int>()
        val pool = mutableListOf<Movie>()
        for (endpoint in listOf("popular", "top_rated")) {
            for (page in 1..pages) {
                val url = "https://api.themoviedb.org/3/movie/$endpoint?api_key=$apiKey&page=$page"
                val results = getJson(url).getJSONArray("results")
                for (i in 0 until results.length()) {
                    val o = results.getJSONObject(i)
                    val id = o.getInt("id")
                    if (!seen.add(id)) continue
                    val genreIds = o.optJSONArray("genre_ids")
                    val genres = if (genreIds == null) "" else (0 until genreIds.length())
                        .joinToString(" ") { genreMap[genreIds.getInt(it)] ?: "" }
                    pool += Movie(
                        id = id,
                        title = o.optString("title", "Unknown"),
                        year = o.optString("release_date", "").take(4).toIntOrNull() ?: 0,
                        genres = genres,
                        rating = o.optDouble("vote_average", 0.0),
                        overview = o.optString("overview", ""),
                        posterPath = if (o.isNull("poster_path")) null else o.getString("poster_path")
                    )
                }
            }
        }
        return pool
    }

    private fun genreMap(): Map<Int, String> {
        val url = "https://api.themoviedb.org/3/genre/movie/list?api_key=$apiKey"
        val arr = getJson(url).getJSONArray("genres")
        return (0 until arr.length()).associate {
            val o = arr.getJSONObject(it); o.getInt("id") to o.getString("name")
        }
    }
}
