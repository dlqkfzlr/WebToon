package com.pluu.webtoon.ui.weekly

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import com.pluu.core.utils.lazyNone
import com.pluu.utils.getThemeColor
import com.pluu.webtoon.Const
import com.pluu.webtoon.R
import com.pluu.webtoon.model.ServiceConst
import com.pluu.webtoon.model.Session
import com.pluu.webtoon.model.UI_NAV_ITEM
import com.pluu.webtoon.model.toUiType
import javax.inject.Inject

/**
 * Base ActionBar Activity
 * Created by pluu on 2017-05-07.
 */
abstract class BaseNavActivity(
    @LayoutRes layoutId: Int
) : AppCompatActivity(layoutId) {

    // Primary toolbar and drawer toggle
    private val mActionBarToolbar by lazyNone {
        findViewById<Toolbar>(R.id.toolbar)
    }

    // Navigation drawer:
    private val mDrawerLayout by lazyNone {
        findViewById<DrawerLayout>(R.id.drawer_layout)
    }

    @Inject
    lateinit var session: Session

    private var selfNavDrawerItem
        get() = session.navi.toUiType()
        set(value) {
            session.navi = value.getCoreType()
        }

    private val mHandler by lazyNone { Handler(Looper.getMainLooper()) }

    // list of navdrawer items that were actually added to the navdrawer, in order
    private val mNavDrawerItems = ArrayList<UI_NAV_ITEM>()

    // views that correspond to each navdrawer item, null if not yet created
    private var mNavDrawerItemViews: MutableList<View>? = null

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setupNavDrawer()
    }

    private fun setupNavDrawer() {
        mDrawerLayout.setStatusBarBackgroundColor(
            ContextCompat.getColor(this, R.color.theme_primary_variant)
        )

        ActionBarDrawerToggle(
            this, /* host Activity */
            mDrawerLayout, /* DrawerLayout object */
            mActionBarToolbar, /* nav drawer image to replace 'Up' caret */
            R.string.app_name, /* "open drawer" description for accessibility */
            R.string.app_name /* "close drawer" description for accessibility */
        ).apply {
            isDrawerIndicatorEnabled = true
            mDrawerLayout.addDrawerListener(this)
            syncState()
        }
        populateNavDrawer()
    }

    private fun populateNavDrawer() {
        mNavDrawerItems.clear()
        UI_NAV_ITEM.values().filterTo(mNavDrawerItems) { it.isSelect }
        createNavDrawerItems()
    }

    private fun createNavDrawerItems() {
        val mDrawerItemsListContainer: ViewGroup? = findViewById(R.id.navdrawer_items_list)
        mDrawerItemsListContainer?.let {
            mNavDrawerItemViews = mutableListOf<View>().apply {
                mDrawerItemsListContainer.removeAllViews()

                for ((index, value) in mNavDrawerItems.withIndex()) {
                    add(makeNavDrawerItem(value, mDrawerItemsListContainer))
                    mDrawerItemsListContainer.addView(get(index))
                }
            }
        }
    }

    private fun makeNavDrawerItem(item: UI_NAV_ITEM, container: ViewGroup): View {
        val selected = selfNavDrawerItem == item
        val layoutToInflate = if (item == UI_NAV_ITEM.SEPARATOR) {
            R.layout.navdrawer_separator
        } else {
            R.layout.navdrawer_item
        }
        val view = layoutInflater.inflate(layoutToInflate, container, false)

        if (isSeparator(item)) {
            // we are done
            return view
        }

        val iconView: ImageView = view.findViewById(R.id.icon)
        val titleView: TextView = view.findViewById(R.id.title)
        val idx = item.ordinal
        val iconId = ServiceConst.NAVDRAWER_ICON_RES_ID[idx]
        val titleId = ServiceConst.NAVDRAWER_TITLE_RES_ID[idx]

        // set icon and text
        iconView.visibility = if (iconId > 0) View.VISIBLE else View.GONE
        if (iconId > 0) {
            iconView.setImageResource(iconId)
        }
        titleView.text = getString(titleId)

        formatNavDrawerItem(view, item, selected)

        view.setOnClickListener { onNavDrawerItemClicked(item) }

        return view
    }

    private fun formatNavDrawerItem(view: View, item: UI_NAV_ITEM, selected: Boolean) {
        if (isSeparator(item)) {
            // not applicable
            return
        }

        val iconView: ImageView = view.findViewById(R.id.icon)
        val titleView: TextView = view.findViewById(R.id.title)

        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(
            if (selected) {
                ContextCompat.getColor(this, item.color)
            } else {
                getThemeColor(R.attr.colorOnSurface)
            }
        )
        iconView.setColorFilter(
            if (selected) {
                ContextCompat.getColor(this, item.bgColor)
            } else {
                getThemeColor(R.attr.colorOnSurface)
            }
        )
    }

    private fun isSeparator(item: UI_NAV_ITEM): Boolean {
        return item == UI_NAV_ITEM.SEPARATOR
    }

    private val isNavDrawerOpen: Boolean
        get() = mDrawerLayout.isDrawerOpen(GravityCompat.START)

    protected fun closeNavDrawer() = mDrawerLayout.closeDrawer(GravityCompat.START)

    private fun onNavDrawerItemClicked(item: UI_NAV_ITEM) {
        if (item == selfNavDrawerItem) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
            return
        }

        // launch the target Activity after a short delay, to allow the close animation to play
        mHandler.postDelayed(NAVDRAWER_LAUNCH_DELAY.toLong()) {
            goToNavDrawerItem(item)
        }

        // change the active item on the list so the user can see the item changed
        setSelectedNavDrawerItem(item)

        mDrawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun goToNavDrawerItem(item: UI_NAV_ITEM?) {
        item ?: return

        selfNavDrawerItem = item

        supportFragmentManager.commit {
            supportFragmentManager.findFragmentByTag(Const.MAIN_FRAG_TAG)?.let {
                remove(it)
            }
            replace(R.id.container, MainFragment.newInstance(), Const.MAIN_FRAG_TAG)
        }
    }

    /**
     * Sets up the given navdrawer item's appearance to the selected state. Note: this could
     * also be accomplished (perhaps more cleanly) with state-based layouts.
     * @param item Select Navi Item
     */
    private fun setSelectedNavDrawerItem(item: UI_NAV_ITEM) {
        mNavDrawerItemViews?.apply {
            for ((index, value) in mNavDrawerItems.withIndex()) {
                if (index < mNavDrawerItems.size) {
                    formatNavDrawerItem(get(index), value, item == value)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isNavDrawerOpen) {
            closeNavDrawer()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        // delay to launch nav drawer item, to allow close animation to play
        private const val NAVDRAWER_LAUNCH_DELAY = 250
    }
}
