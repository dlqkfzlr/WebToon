package com.pluu.support.nate

import com.pluu.support.impl.AbstractWeekApi
import com.pluu.support.impl.REQUEST_METHOD
import com.pluu.webtoon.di.NetworkUseCase
import com.pluu.webtoon.item.WebToonInfo
import org.jsoup.Jsoup

/**
 * 네이트 웹툰 Week API
 * Created by pluu on 2017-04-26.
 */
class NateWeekApi(
    networkUseCase: NetworkUseCase
) : AbstractWeekApi(networkUseCase, NateWeekApi.TITLE) {

    override val url = "http://m.comics.nate.com/main/index"

    override fun parseMain(position: Int): List<WebToonInfo> {

        val doc = try {
            Jsoup.parse(requestApi())
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }

        val list = mutableListOf<WebToonInfo>()
        val pattern = "(?<=btno=)\\d+".toRegex()
        doc.select(".wkTypeAll_$position").forEach {
            pattern.find(it.attr("href"))?.apply {
                WebToonInfo(value).apply {
                    title = it.select(".wtl_title").text()
                    image = it.select(".wtl_img img").first().attr("src")
                    writer = it.select(".wtl_author").text()
                    list.add(this)
                }
            }
        }
        return list
    }

    override val method: REQUEST_METHOD = REQUEST_METHOD.GET

    companion object {
        private val TITLE = arrayOf("월", "화", "수", "목", "금", "토", "일")
    }
}
