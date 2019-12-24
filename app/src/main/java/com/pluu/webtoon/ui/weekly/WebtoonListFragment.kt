package com.pluu.webtoon.ui.weekly

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.pluu.event.EventBus
import com.pluu.kotlin.getCompatColor
import com.pluu.kotlin.toast
import com.pluu.webtoon.R
import com.pluu.webtoon.adapter.MainListAdapter
import com.pluu.webtoon.common.Const
import com.pluu.webtoon.databinding.FragmentWebtoonListBinding
import com.pluu.webtoon.domain.moel.ToonInfo
import com.pluu.webtoon.event.MainEpisodeLoadedEvent
import com.pluu.webtoon.event.MainEpisodeStartEvent
import com.pluu.webtoon.model.FavoriteResult
import com.pluu.webtoon.ui.episode.EpisodesActivity
import com.pluu.webtoon.ui.listener.WebToonSelectListener
import com.pluu.webtoon.utils.lazyNone
import com.pluu.webtoon.utils.observeNonNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Main EpisodeResult List Fragment
 * Created by pluu on 2017-05-07.
 */
class WebtoonListFragment : Fragment(), WebToonSelectListener {

    private val viewModel: WeekyViewModel by viewModel {
        parametersOf(
            arguments?.getInt(Const.EXTRA_POS) ?: 0
        )
    }

    private lateinit var binding: FragmentWebtoonListBinding

    private val toonLayoutManager: GridLayoutManager by lazyNone {
        GridLayoutManager(context, resources.getInteger(R.integer.webtoon_column_count))
    }

    private val REQUEST_DETAIL = 1000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWebtoonListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerView) {
            layoutManager = toonLayoutManager
        }
    }

    @ExperimentalCoroutinesApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.listEvent.observeNonNull(this) { list ->
            binding.recyclerView.adapter = MainListAdapter(requireContext(), list, this)
            binding.emptyView.isVisible = list.isEmpty()
        }
        viewModel.event.observeNonNull(this) { event ->
            when (event) {
                WeeklyEvent.START -> {
                    EventBus.send(MainEpisodeStartEvent())
                }
                WeeklyEvent.LOADED -> {
                    EventBus.send(MainEpisodeLoadedEvent())
                }
                is WeeklyEvent.ERROR -> {
                    toast(event.message)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_DETAIL) {
            // 즐겨찾기 변경 처리 > 다른 ViewPager의 Fragment도 수신받기위해 Referrer
            parentFragmentManager.findFragmentByTag(Const.MAIN_FRAG_TAG)
                ?.onActivityResult(REQUEST_DETAIL_REFERRER, resultCode, data)
        } else if (requestCode == REQUEST_DETAIL_REFERRER) {
            // ViewPager 로부터 전달받은 Referrer
            data?.getParcelableExtra<FavoriteResult>(Const.EXTRA_FAVORITE_EPISODE)?.apply {
                favoriteUpdate(this)
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // WebToonSelectListener
    ///////////////////////////////////////////////////////////////////////////

    override fun selectLockItem() {
        toast(R.string.msg_not_support)
    }

    override fun selectSuccess(view: ImageView, item: ToonInfo) {
        fun asyncPalette(bitmap: Bitmap, block: (Pair<Int, Int>) -> Unit) {
            val context = context ?: return
            Palette.from(bitmap).generate { p ->
                val bgColor = p?.getDarkVibrantColor(Color.BLACK) ?: Color.BLACK
                val statusColor =
                    p?.getDarkMutedColor(context.getCompatColor(R.color.theme_primary_dark))
                        ?: context.getCompatColor(R.color.theme_primary_dark)
                block(bgColor to statusColor)
            }
        }

        fun loadPalette(view: ImageView, block: (Pair<Int, Int>) -> Unit) {
            view.palletBitmap?.let {
                asyncPalette(it, block)
            }
        }

        loadPalette(view) { colors ->
            moveEpisode(item, colors.first, colors.second)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Function
    ///////////////////////////////////////////////////////////////////////////

    private fun favoriteUpdate(info: FavoriteResult) {
        (binding.recyclerView.adapter as? MainListAdapter)
            ?.modifyInfo(info.id, info.isFavorite)
    }

    private fun moveEpisode(item: ToonInfo, bgColor: Int, statusColor: Int) {
        startActivityForResult(Intent(activity, EpisodesActivity::class.java).apply {
            putExtra(Const.EXTRA_EPISODE, item)
            putExtra(Const.EXTRA_MAIN_COLOR, bgColor)
            putExtra(Const.EXTRA_STATUS_COLOR, statusColor)
        }, REQUEST_DETAIL)
    }

    private fun updateSpanCount() {
        toonLayoutManager.spanCount = resources.getInteger(R.integer.webtoon_column_count)
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        newConfig.takeIf {
            it.orientation == Configuration.ORIENTATION_LANDSCAPE ||
                    it.orientation == Configuration.ORIENTATION_PORTRAIT
        }.run {
            updateSpanCount()
        }
    }

    companion object {
        const val REQUEST_DETAIL_REFERRER = 1001
    }
}

private val ImageView.palletBitmap: Bitmap?
    get() {
        return when (val innerDrawable = drawable) {
            is BitmapDrawable -> innerDrawable.bitmap
            is GifDrawable -> innerDrawable.firstFrame
            else -> null
        }
    }
