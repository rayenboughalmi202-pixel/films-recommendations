package com.yami.films

import com.yami.films.data.Movie
import com.yami.films.recommender.Recommender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommenderTest {

    private fun movie(id: Int, title: String, genres: String, words: String) =
        Movie(id, title, 2000, genres, 8.0, words)

    private val pool = listOf(
        movie(1, "Dream Heist", "Sci-Fi Action", "dream heist mind reality thief"),
        movie(2, "Space Dreams", "Sci-Fi Drama", "space dream future astronaut reality"),
        movie(3, "Kitchen Laughs", "Comedy", "funny kitchen cooking friends comedy")
    )

    @Test
    fun neverRecommendsTheMovieItself() {
        val recs = Recommender.recommend(pool[0], pool, topN = 5)
        assertFalse(recs.any { it.movie.id == pool[0].id })
    }

    @Test
    fun mostSimilarMovieComesFirst() {
        val recs = Recommender.recommend(pool[0], pool, topN = 5)
        assertEquals("Space Dreams", recs.first().movie.title)
        val scores = recs.map { it.similarity }
        assertEquals(scores, scores.sortedDescending())
        assertTrue(scores.all { it in 0.0..1.0 })
    }
}
