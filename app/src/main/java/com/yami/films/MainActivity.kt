package com.yami.films

import android.app.Activity
import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.yami.films.data.Movie
import com.yami.films.data.OfflineMovies
import com.yami.films.data.TmdbClient
import com.yami.films.recommender.Recommender
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

/**
 * Yami_Films — main screen.
 *
 * Online mode (TMDB key saved in Settings):
 *   search any movie -> tap it -> the app downloads a pool of popular /
 *   top-rated movies from TMDB and ranks them with our own TF-IDF +
 *   cosine-similarity recommender, on the phone.
 *
 * Offline mode (no key or no connection):
 *   the same recommender runs on the built-in film list.
 *
 * Working buttons (v2):
 *  HOME resets the screen, SEARCH focuses the search box,
 *  MY LIST shows the movies saved on this phone (tap one to get its
 *  recommendations or remove it), SETTINGS opens TMDB key / Language /
 *  Appearance / About — all of them really work and are remembered.
 */
class MainActivity : Activity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var mainScroll: ScrollView
    private lateinit var headlineText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var resultsLabel: TextView
    private lateinit var recLabel: TextView
    private lateinit var navBar: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var statusText: TextView
    private lateinit var searchResults: ListView
    private lateinit var posterImage: ImageView
    private lateinit var recommendButton: Button
    private lateinit var favoriteButton: Button
    private lateinit var recommendationsText: TextView
    private lateinit var navHome: Button
    private lateinit var navSearch: Button
    private lateinit var navFavorites: Button
    private lateinit var navSettings: Button

    private var foundMovies: List<Movie> = emptyList()
    private var selected: Movie? = null
    private var onlineMode = false

    private val prefs by lazy { getSharedPreferences("yami_films", MODE_PRIVATE) }

    private fun apiKey(): String =
        prefs.getString("tmdb_api_key", "")?.ifBlank { BuildConfig.TMDB_API_KEY }
            ?: BuildConfig.TMDB_API_KEY

    // ---------------- Language ----------------

    private fun lang(): String = prefs.getString("language", "en") ?: "en"

    private val strings: Map<String, Map<String, String>> = mapOf(
        "en" to mapOf(
            "headline" to "Find your next favorite movie.",
            "subtitle" to "Search a film and let AI discover what you should watch next.",
            "hint" to "Search movie title…",
            "search" to "SEARCH", "results" to "SEARCH RESULTS",
            "recommendations" to "AI RECOMMENDATIONS",
            "find" to "✦  FIND SIMILAR MOVIES",
            "addList" to "♥  ADD TO MY LIST", "inList" to "♥  SAVED — TAP TO REMOVE",
            "home" to "⌂\nHOME", "navSearch" to "⌕\nSEARCH",
            "navList" to "♥\nMY LIST", "navSettings" to "⚙\nSETTINGS",
            "offlineStatus" to "Offline mode (built-in list). Add your free TMDB key in Settings for all movies online.",
            "onlineStatus" to "Online mode 🌐 — search any movie.",
            "settings" to "Settings", "language" to "Language",
            "appearance" to "Appearance", "about" to "About Yami Films",
            "dark" to "Dark", "light" to "Light",
            "myList" to "My List", "listEmpty" to "Your list is empty.\nSearch a movie, select it, then tap ADD TO MY LIST.",
            "langChanged" to "Language changed ✅",
            "added" to "Added to My List ♥", "removed" to "Removed from My List",
            "recFor" to "Recommend similar", "remove" to "Remove from My List",
            "aboutMsg" to "Movie discovery powered by TMDB and on-device TF-IDF + cosine similarity.\nYour saved movies and settings stay only on this phone."
        ),
        "fr" to mapOf(
            "headline" to "Trouve ton prochain film préféré.",
            "subtitle" to "Cherche un film et laisse l'IA découvrir ce que tu devrais regarder ensuite.",
            "hint" to "Chercher un film…",
            "search" to "CHERCHER", "results" to "RÉSULTATS",
            "recommendations" to "RECOMMANDATIONS IA",
            "find" to "✦  TROUVER DES FILMS SIMILAIRES",
            "addList" to "♥  AJOUTER À MA LISTE", "inList" to "♥  ENREGISTRÉ — APPUIE POUR RETIRER",
            "home" to "⌂\nACCUEIL", "navSearch" to "⌕\nCHERCHER",
            "navList" to "♥\nMA LISTE", "navSettings" to "⚙\nRÉGLAGES",
            "offlineStatus" to "Mode hors-ligne (liste intégrée). Ajoute ta clé TMDB gratuite dans Réglages pour tous les films en ligne.",
            "onlineStatus" to "Mode en ligne 🌐 — cherche n'importe quel film.",
            "settings" to "Réglages", "language" to "Langue",
            "appearance" to "Apparence", "about" to "À propos de Yami Films",
            "dark" to "Sombre", "light" to "Clair",
            "myList" to "Ma Liste", "listEmpty" to "Ta liste est vide.\nCherche un film, sélectionne-le, puis appuie sur AJOUTER À MA LISTE.",
            "langChanged" to "Langue changée ✅",
            "added" to "Ajouté à Ma Liste ♥", "removed" to "Retiré de Ma Liste",
            "recFor" to "Recommander des similaires", "remove" to "Retirer de Ma Liste",
            "aboutMsg" to "Découverte de films propulsée par TMDB et un algorithme TF-IDF + similarité cosinus qui tourne sur ton téléphone.\nTes films enregistrés et tes réglages restent uniquement sur ce téléphone."
        ),
        "ar" to mapOf(
            "headline" to "اعثر على فيلمك المفضل القادم.",
            "subtitle" to "ابحث عن فيلم ودع الذكاء الاصطناعي يكتشف ما يجب أن تشاهده بعد ذلك.",
            "hint" to "ابحث عن عنوان فيلم…",
            "search" to "بحث", "results" to "نتائج البحث",
            "recommendations" to "توصيات الذكاء الاصطناعي",
            "find" to "✦  اعثر على أفلام مشابهة",
            "addList" to "♥  أضف إلى قائمتي", "inList" to "♥  محفوظ — اضغط للإزالة",
            "home" to "⌂\nالرئيسية", "navSearch" to "⌕\nبحث",
            "navList" to "♥\nقائمتي", "navSettings" to "⚙\nالإعدادات",
            "offlineStatus" to "وضع عدم الاتصال (القائمة المدمجة). أضف مفتاح TMDB المجاني في الإعدادات لكل الأفلام عبر الإنترنت.",
            "onlineStatus" to "الوضع عبر الإنترنت 🌐 — ابحث عن أي فيلم.",
            "settings" to "الإعدادات", "language" to "اللغة",
            "appearance" to "المظهر", "about" to "حول Yami Films",
            "dark" to "داكن", "light" to "فاتح",
            "myList" to "قائمتي", "listEmpty" to "قائمتك فارغة.\nابحث عن فيلم، اختره، ثم اضغط أضف إلى قائمتي.",
            "langChanged" to "تم تغيير اللغة ✅",
            "added" to "أُضيف إلى قائمتي ♥", "removed" to "أُزيل من قائمتي",
            "recFor" to "اقترح أفلاماً مشابهة", "remove" to "إزالة من قائمتي",
            "aboutMsg" to "اكتشاف الأفلام مدعوم من TMDB وخوارزمية TF-IDF + تشابه جيب التمام تعمل على هاتفك.\nأفلامك المحفوظة وإعداداتك تبقى على هذا الهاتف فقط."
        )
    )

    private fun t(key: String): String = strings[lang()]?.get(key) ?: strings["en"]!![key] ?: key

    // ---------------- Favorites (My List) ----------------

    private fun loadFavorites(): MutableList<Movie> {
        val raw = prefs.getString("favorites_json", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<Movie>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list += Movie(
                id = o.getInt("id"),
                title = o.getString("title"),
                year = o.optInt("year"),
                genres = o.optString("genres"),
                rating = o.optDouble("rating"),
                overview = o.optString("overview"),
                keywords = o.optString("keywords"),
                posterPath = if (o.isNull("poster")) null else o.getString("poster")
            )
        }
        return list
    }

    private fun saveFavorites(list: List<Movie>) {
        val arr = JSONArray()
        for (m in list) {
            arr.put(JSONObject().apply {
                put("id", m.id); put("title", m.title); put("year", m.year)
                put("genres", m.genres); put("rating", m.rating)
                put("overview", m.overview); put("keywords", m.keywords)
                put("poster", m.posterPath ?: JSONObject.NULL)
            })
        }
        prefs.edit().putString("favorites_json", arr.toString()).apply()
    }

    private fun isFavorite(movie: Movie) = loadFavorites().any { it.id == movie.id }

    private fun toggleFavorite() {
        val movie = selected ?: return
        val favorites = loadFavorites()
        if (isFavorite(movie)) {
            saveFavorites(favorites.filter { it.id != movie.id })
            Toast.makeText(this, t("removed"), Toast.LENGTH_SHORT).show()
        } else {
            favorites.add(movie)
            saveFavorites(favorites)
            Toast.makeText(this, t("added"), Toast.LENGTH_SHORT).show()
        }
        updateFavoriteButton()
    }

    private fun updateFavoriteButton() {
        val movie = selected
        favoriteButton.isEnabled = movie != null
        favoriteButton.text = if (movie != null && isFavorite(movie)) t("inList") else t("addList")
    }

    private fun showFavorites() {
        val favorites = loadFavorites()
        if (favorites.isEmpty()) {
            AlertDialog.Builder(this).setTitle(t("myList"))
                .setMessage(t("listEmpty"))
                .setPositiveButton("OK", null).show()
            return
        }
        AlertDialog.Builder(this).setTitle("${t("myList")} (${favorites.size})")
            .setItems(favorites.map { it.toString() }.toTypedArray()) { _, index ->
                val movie = favorites[index]
                AlertDialog.Builder(this).setTitle(movie.title)
                    .setItems(arrayOf(t("recFor"), t("remove"))) { _, action ->
                        if (action == 0) {
                            selected = movie
                            onlineMode = movie.id > 0 && apiKey().isNotBlank()
                            recommendButton.isEnabled = true
                            updateFavoriteButton()
                            statusText.text = "Selected: $movie"
                            loadPoster(movie)
                            recommend()
                        } else {
                            saveFavorites(loadFavorites().filter { it.id != movie.id })
                            Toast.makeText(this, t("removed"), Toast.LENGTH_SHORT).show()
                            updateFavoriteButton()
                        }
                    }.show()
            }.show()
    }

    // ---------------- Screen setup ----------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.rootLayout)
        mainScroll = findViewById(R.id.mainScroll)
        headlineText = findViewById(R.id.headlineText)
        subtitleText = findViewById(R.id.subtitleText)
        resultsLabel = findViewById(R.id.resultsLabel)
        recLabel = findViewById(R.id.recLabel)
        navBar = findViewById(R.id.navBar)
        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        statusText = findViewById(R.id.statusText)
        searchResults = findViewById(R.id.searchResults)
        posterImage = findViewById(R.id.posterImage)
        recommendButton = findViewById(R.id.recommendButton)
        favoriteButton = findViewById(R.id.favoriteButton)
        recommendationsText = findViewById(R.id.recommendationsText)
        navHome = findViewById(R.id.navHome)
        navSearch = findViewById(R.id.navSearch)
        navFavorites = findViewById(R.id.navFavorites)
        navSettings = findViewById(R.id.navSettings)
        recommendationsText.movementMethod = ScrollingMovementMethod.getInstance()

        searchButton.setOnClickListener { search() }
        recommendButton.setOnClickListener { recommend() }
        favoriteButton.setOnClickListener { toggleFavorite() }
        navHome.setOnClickListener { goHome() }
        navSearch.setOnClickListener {
            searchInput.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
        }
        navFavorites.setOnClickListener { showFavorites() }
        navSettings.setOnClickListener { showSettingsDialog() }

        searchResults.setOnItemClickListener { _, _, position, _ ->
            selected = foundMovies.getOrNull(position)
            recommendButton.isEnabled = selected != null
            updateFavoriteButton()
            statusText.text = selected?.let { "Selected: $it — tap Recommend ✨" } ?: ""
            loadPoster(selected)
        }

        applyLanguage()
        applyAppearance()
        resetStatus()
    }

    private fun resetStatus() {
        statusText.text = if (apiKey().isBlank()) t("offlineStatus") else t("onlineStatus")
    }

    /** HOME: back to a clean start screen. */
    private fun goHome() {
        searchInput.text.clear()
        searchInput.clearFocus()
        foundMovies = emptyList()
        searchResults.adapter = null
        selected = null
        recommendButton.isEnabled = false
        updateFavoriteButton()
        recommendationsText.text = ""
        posterImage.visibility = View.GONE
        resetStatus()
        mainScroll.fullScroll(View.FOCUS_UP)
    }

    private fun applyLanguage() {
        headlineText.text = t("headline")
        subtitleText.text = t("subtitle")
        searchInput.hint = t("hint")
        searchButton.text = t("search")
        resultsLabel.text = t("results")
        recLabel.text = t("recommendations")
        recommendButton.text = t("find")
        navHome.text = t("home")
        navSearch.text = t("navSearch")
        navFavorites.text = t("navList")
        navSettings.text = t("navSettings")
        updateFavoriteButton()
    }

    private fun applyAppearance() {
        val light = prefs.getString("appearance", "dark") == "light"
        if (light) {
            rootLayout.setBackgroundColor(Color.parseColor("#F5F5F5"))
            navBar.setBackgroundColor(Color.parseColor("#EEEEEE"))
            headlineText.setTextColor(Color.parseColor("#111111"))
            subtitleText.setTextColor(Color.parseColor("#555555"))
            resultsLabel.setTextColor(Color.parseColor("#111111"))
            recLabel.setTextColor(Color.parseColor("#111111"))
            statusText.setTextColor(Color.parseColor("#444444"))
            searchInput.setTextColor(Color.parseColor("#111111"))
            recommendationsText.setBackgroundColor(Color.parseColor("#FFFFFF"))
            recommendationsText.setTextColor(Color.parseColor("#222222"))
            searchResults.setBackgroundColor(Color.parseColor("#FFFFFF"))
        } else {
            rootLayout.setBackgroundColor(Color.parseColor("#111111"))
            navBar.setBackgroundColor(Color.parseColor("#080808"))
            headlineText.setTextColor(Color.WHITE)
            subtitleText.setTextColor(Color.parseColor("#AAAAAA"))
            resultsLabel.setTextColor(Color.WHITE)
            recLabel.setTextColor(Color.WHITE)
            statusText.setTextColor(Color.parseColor("#BBBBBB"))
            searchInput.setTextColor(Color.WHITE)
            recommendationsText.setBackgroundColor(Color.parseColor("#1C1C1C"))
            recommendationsText.setTextColor(Color.parseColor("#EEEEEE"))
            searchResults.setBackgroundColor(Color.parseColor("#1C1C1C"))
        }
    }

    // ---------------- Settings ----------------

    private fun showSettingsDialog() {
        val items = arrayOf("TMDB API key", t("language"), t("appearance"), t("about"))
        AlertDialog.Builder(this)
            .setTitle(t("settings"))
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showKeyDialog()
                    1 -> showLanguageDialog()
                    2 -> showAppearanceDialog()
                    3 -> AlertDialog.Builder(this).setTitle("Yami Films")
                        .setMessage(t("aboutMsg"))
                        .setPositiveButton("OK", null).show()
                }
            }.show()
    }

    private fun showLanguageDialog() {
        val names = arrayOf("English", "Français", "العربية")
        val codes = arrayOf("en", "fr", "ar")
        AlertDialog.Builder(this).setTitle(t("language"))
            .setItems(names) { _, index ->
                prefs.edit().putString("language", codes[index]).apply()
                applyLanguage()
                resetStatus()
                Toast.makeText(this, t("langChanged"), Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun showAppearanceDialog() {
        AlertDialog.Builder(this).setTitle(t("appearance"))
            .setItems(arrayOf(t("dark"), t("light"))) { _, index ->
                prefs.edit().putString("appearance", if (index == 0) "dark" else "light").apply()
                applyAppearance()
            }.show()
    }

    private fun showKeyDialog() {
        val input = EditText(this).apply {
            hint = "Paste your free TMDB API key here"
            setText(apiKey())
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("TMDB API key")
            .setMessage("Free key: themoviedb.org → create an account → Settings → API.\nThe key stays only on this phone.")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit().putString("tmdb_api_key", input.text.toString().trim()).apply()
                Toast.makeText(this, "Key saved ✅", Toast.LENGTH_SHORT).show()
                resetStatus()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------- Search & recommend (unchanged engine) ----------------

    private fun search() {
        val query = searchInput.text.toString().trim()
        if (query.isEmpty()) return
        selected = null
        recommendButton.isEnabled = false
        updateFavoriteButton()
        recommendationsText.text = ""

        val key = apiKey()
        if (key.isBlank()) {
            onlineMode = false
            foundMovies = OfflineMovies.search(OfflineMovies.load(this), query)
            showResults("Offline mode — ${foundMovies.size} result(s) in the built-in list.")
            return
        }

        statusText.text = "Searching TMDB…"
        thread {
            try {
                val results = TmdbClient(key).searchMovie(query)
                runOnUiThread {
                    onlineMode = true
                    foundMovies = results
                    showResults("Online 🌐 — ${results.size} result(s). Tap the right movie.")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    onlineMode = false
                    foundMovies = OfflineMovies.search(OfflineMovies.load(this), query)
                    showResults("No connection / key problem — offline mode instead (${foundMovies.size} result(s)).")
                }
            }
        }
    }

    private fun showResults(message: String) {
        statusText.text = message
        searchResults.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, foundMovies.map { it.toString() }
        )
    }

    private fun loadPoster(movie: Movie?) {
        val url = movie?.posterUrl()
        if (url == null) {
            posterImage.visibility = View.GONE
            return
        }
        thread {
            try {
                val bitmap = URL(url).openStream().use { BitmapFactory.decodeStream(it) }
                runOnUiThread {
                    posterImage.setImageBitmap(bitmap)
                    posterImage.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                runOnUiThread { posterImage.visibility = View.GONE }
            }
        }
    }

    private fun recommend() {
        val target = selected ?: return
        recommendationsText.text = "Computing recommendations…"

        if (!onlineMode) {
            val pool = OfflineMovies.load(this)
            printRecommendations(Recommender.recommend(target, pool, topN = 5))
            return
        }

        val key = apiKey()
        thread {
            try {
                val client = TmdbClient(key)
                val detailed = client.movieDetails(target)
                    .copy(keywords = client.movieKeywords(target.id))
                val pool = client.candidatePool(pages = 2)
                val recs = Recommender.recommend(detailed, pool, topN = 5)
                runOnUiThread { printRecommendations(recs) }
            } catch (e: Exception) {
                runOnUiThread {
                    recommendationsText.text =
                        "Online recommendation failed (${e.message}). Check your connection and your TMDB key."
                }
            }
        }
    }

    private fun printRecommendations(recs: List<Recommender.ScoredMovie>) {
        if (recs.isEmpty()) {
            recommendationsText.text = "No recommendations found."
            return
        }
        recommendationsText.text = buildString {
            appendLine("🎬 If you liked ${selected?.title}, try:")
            appendLine()
            recs.forEachIndexed { i, r ->
                appendLine("${i + 1}. ${r.movie.title} (${r.movie.year})")
                appendLine("    ${r.movie.genres} • ⭐ ${r.movie.rating}")
                appendLine("    Similarity: ${(r.similarity * 100).toInt()}%")
                appendLine()
            }
        }
    }
}
