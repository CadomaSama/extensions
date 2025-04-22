package eu.kanade.tachiyomi.extension.en.kolbook

import eu.kanade.tachiyomi.multisrc.MultiSourceAdapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SChapter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class KolBook : MultiSourceAdapter("KolBook", "https://kolbook.xyz") {
    private val client = OkHttpClient()

    // للحصول على قائمة الروايات
    override fun fetchMangaList(page: Int): Observable<List<SManga>> {
        val url = "https://kolbook.xyz/az-list/?show=F&page=$page"
        val request = Request.Builder().url(url).build()
        return client.newCall(request).execute().use { response ->
            val document = response.body?.string()?.let { Jsoup.parse(it) }
            val mangas = document?.select("div.bsx a")?.mapNotNull {
                SManga.create().apply {
                    title = it.attr("title")
                    url = it.absUrl("href")
                }
            } ?: emptyList()
            Observable.just(mangas)
        }
    }

    // للحصول على فصول الرواية
    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        val request = Request.Builder().url(manga.url).build()
        return client.newCall(request).execute().use { response ->
            val document = response.body?.string()?.let { Jsoup.parse(it) }
            val chapters = document?.select("div.chapter a")?.mapNotNull {
                SChapter.create().apply {
                    name = it.text()
                    url = it.absUrl("href")
                }
            } ?: emptyList()
            Observable.just(chapters)
        }
    }

    // للحصول على التفاصيل الخاصة بالرواية
    override fun fetchMangaDetails(manga: SManga): Observable<SManga> {
        val request = Request.Builder().url(manga.url).build()
        return client.newCall(request).execute().use { response ->
            val document = response.body?.string()?.let { Jsoup.parse(it) }
            manga.description = document?.select("div.summary p")?.text()
            Observable.just(manga)
        }
    }
}
