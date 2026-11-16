"""Verify the Yami_Films recommender outside Android.

Runs the SAME algorithm as the Kotlin app
(app/src/main/java/com/yami/films/recommender/Recommender.kt):
TF-IDF over genres + keywords, cosine similarity, Top 5 —
on the SAME dataset (app/src/main/assets/movies.csv).

Usage:  python3 tools/verify_recommender.py
No third-party packages needed.
"""
import csv
import math
import re
from collections import Counter
from pathlib import Path

STOP = {
    "the", "a", "an", "and", "or", "of", "to", "in", "on", "for", "with",
    "is", "are", "was", "were", "his", "her", "their", "its", "it", "as",
    "at", "by", "from", "who", "when", "after", "before", "into", "over",
}

DATA = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "assets" / "movies.csv"


def tokenize(text):
    words = re.sub(r"[^a-z0-9 ]", " ", text.lower()).split()
    return [w for w in words if len(w) > 2 and w not in STOP]


def load_movies():
    with DATA.open(newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f, delimiter=";"))
    movies = []
    for i, r in enumerate(rows):
        movies.append({
            "id": -(i + 1),
            "title": r["title"],
            "year": int(r["year"]),
            "genres": r["genres"],
            "rating": float(r["rating"]),
            "text": f"{r['genres']} {r['keywords']}",
        })
    return movies


def tfidf(movies):
    docs = [tokenize(m["text"]) for m in movies]
    n = len(docs)
    df = Counter()
    for doc in docs:
        for w in set(doc):
            df[w] += 1
    vectors = []
    for doc in docs:
        counts = Counter(doc)
        vec = {}
        for w, c in counts.items():
            tf = c / len(doc)
            idf = math.log((1 + n) / (1 + df[w])) + 1.0
            vec[w] = tf * idf
        vectors.append(vec)
    return vectors


def cosine(a, b):
    dot = sum(wa * b.get(w, 0.0) for w, wa in a.items())
    na = math.sqrt(sum(v * v for v in a.values()))
    nb = math.sqrt(sum(v * v for v in b.values()))
    return dot / (na * nb) if na and nb else 0.0


def recommend(movies, vectors, title, top_n=5):
    idx = next(i for i, m in enumerate(movies) if m["title"] == title)
    scored = [
        (cosine(vectors[idx], vectors[j]), m)
        for j, m in enumerate(movies) if j != idx
    ]
    scored.sort(key=lambda x: x[0], reverse=True)
    return scored[:top_n]


def main():
    movies = load_movies()
    vectors = tfidf(movies)
    print(f"Loaded {len(movies)} movies from {DATA.name}\n")
    for target in ["Inception", "The Hangover"]:
        print(f"If you liked {target}:")
        for score, m in recommend(movies, vectors, target):
            print(f"  - {m['title']} ({m['year']}) - {m['genres']} - "
                  f"rating {m['rating']} - similarity {score * 100:.2f}%")
        print()

    # Simple checks, mirroring the Kotlin unit tests
    top1 = recommend(movies, vectors, "Inception")[0][1]["title"]
    assert top1 != "Inception", "the movie itself must never be recommended"
    scores = [s for s, _ in recommend(movies, vectors, "Inception")]
    assert scores == sorted(scores, reverse=True), "results must be sorted"
    assert all(0.0 <= s <= 1.0 for s in scores), "similarity must be in [0, 1]"
    print("All checks passed ✅")


if __name__ == "__main__":
    main()
