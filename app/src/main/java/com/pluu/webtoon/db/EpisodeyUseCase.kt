package com.pluu.webtoon.db

import com.pluu.support.impl.NAV_ITEM
import com.pluu.webtoon.model.REpisode

/**
 * Episode Section Use Case
 */
class EpisodeUseCase(
    private val realmHelper: RealmHelper,
    private val naviItem: NAV_ITEM
) {
    /**
     * 이미 읽은 Episode 취득
     * @param id Episode ID
     * @return Read List
     */
    fun getEpisode(id: String): List<REpisode> =
        realmHelper.getEpisode(naviItem, id)
}
