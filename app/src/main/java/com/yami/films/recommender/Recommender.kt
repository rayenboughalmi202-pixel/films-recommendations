package com.yami.films.recommender

import com.yami.films.data.Movie
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Content-based movie recommender — the "AI" of Yami_Films.
 *
 * How it works:
 * 1. TF-IDF turns each movie's text (genres + overview + keywords) into a
 *    vector of numbers. Words that are important in one movie but rare in
 *    the others get more weight.
 * 2. Cosine similarity compares the chosen movie's vector with every other
 *    movie's vector: 1.0 = same themes, 0.0 = nothing in common.
 * 3. The most similar movies (excluding the chosen one) are recommended.
 *
 * The exact same algorithm is verified in Python by
 * tools/verify_recommender.py on the built-in dataset.
 */
object Recommender {

    private val stopWords = setOf(
        "the", "a", "an", "and", "or", "of", "to", "in", "on", "for", "with",
        "is", "are", "was", "were", "his", "her", "their", "its", "it", "as",
        "at", "by", "from", "who", "when", "after", "before", "into", "over"
    )

    fun tokenize(text: String): List<String> =
        text.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 && it !in stopWords }

    /** TF-IDF vector for every movie, as term -> weight maps. */
    fun tfidfVectors(movies: List<Movie>): List<Map<String, Double>> {
        val docs = movies.map { tokenize(it.text) }
        val n = docs.size.coerceAtLeast(1)

        // Document frequency: in how many movies does each word appear?
        val df = mutableMapOf<String, Int>()
        for (doc in docs) for (word in doc.toSet()) df[word] = (df[word] ?: 0) + 1

        return docs.map { doc ->
            if (doc.isEmpty()) return@map emptyMap()
            val counts = doc.groupingBy { it }.eachCount()
            counts.keys.associateWith { word ->
                val tf = counts[word]!!.toDouble() / doc.size
                val idf = ln((1.0 + n) / (1.0 + (df[word] ?: 0))) + 1.0
                tf * idf
            }
        }
    }

    fun cosineSimilarity(a: Map<String, Double>, b: Map<String, Double>): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        var dot = 0.0
        for ((word, wa) in a) dot += wa * (b[word] ?: 0.0)
        val normA = sqrt(a.values.sumOf { it * it })
        val normB = sqrt(b.values.sumOf { it * it })
        return if (normA == 0.0 || normB == 0.0) 0.0 else dot / (normA * normB)
    }

    data class ScoredMovie(val movie: Movie, val similarity: Double)

    /**
     * The [topN] movies most similar to [target] inside [pool].
     * [target] itself (same id) is never recommended.
     */
    fun recommend(target: Movie, pool: List<Movie>, topN: Int = 5): List<ScoredMovie> {
        val all = (listOf(target) + pool.filter { it.id != target.id }).distinctBy { it.id }
        val vectors = tfidfVectors(all)
        val targetVector = vectors[0]
        return all.drop(1).mapIndexed { i, movie ->
            ScoredMovie(movie, cosineSimilarity(targetVector, vectors[i + 1]))
        }.sortedByDescending { it.similarity }.take(topN)
    }
}
