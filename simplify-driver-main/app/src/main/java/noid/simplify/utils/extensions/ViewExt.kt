package noid.simplify.utils.extensions

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import noid.simplify.R
import noid.simplify.utils.GlideApp

fun TabLayout.setMarginEachItems() {
    for (i in 0 until this.tabCount) {
        val tab = (this.getChildAt(0) as ViewGroup).getChildAt(i)
        val p = tab.layoutParams as MarginLayoutParams
        p.setMargins(0, 0, 24, 0)
        tab.requestLayout()
    }
}

fun View?.visible(isVisible: Boolean) {
    if (this == null) return
    if (isVisible) {
        if (!this.isVisible) this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}

fun View?.visibleOrInvisible(isVisible: Boolean) {
    if (this == null) return
    if (isVisible) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.INVISIBLE
    }
}

fun DrawerLayout.openOrClose() {
    if (this.isDrawerOpen(GravityCompat.START)) {
        this.closeDrawer(GravityCompat.START)
    } else {
        this.openDrawer(GravityCompat.START)
    }
}

fun ImageView.showImage(image: Any?) {
    val drawHolder = ContextCompat.getDrawable(this.context, R.drawable.logo)
    drawHolder.setColorFilter(ContextCompat.getColor(this.context, R.color.white))
    GlideApp.with(this.context.applicationContext)
            .load(image)
            .placeholder(drawHolder)
            .error(drawHolder)
            .centerCrop()
            .into(this)
}

fun RecyclerView.toLinearVertical(
    viewAdapter: RecyclerView.Adapter<*>?,
    isHasFixedSize: Boolean = true
) {
    this.apply {
        setHasFixedSize(isHasFixedSize)
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        itemAnimator = DefaultItemAnimator()
        adapter = viewAdapter
    }
}