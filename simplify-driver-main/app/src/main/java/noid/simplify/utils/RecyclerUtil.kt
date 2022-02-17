package noid.simplify.utils

import android.content.Context
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object RecyclerUtil {

    fun setRecyclerLinear(
        context: Context?,
        recyclerView: RecyclerView?,
        viewAdapter: RecyclerView.Adapter<*>?,
        orientation: Int = RecyclerView.VERTICAL,
        isHasFixedSize: Boolean = true
    ) {
        recyclerView?.apply {
            setHasFixedSize(isHasFixedSize)
            layoutManager = LinearLayoutManager(context, orientation, false)
            itemAnimator = DefaultItemAnimator()
            adapter = viewAdapter
        }
    }
}