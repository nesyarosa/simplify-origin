package noid.simplify.ui.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T : Any, VB : ViewDataBinding>
    : RecyclerView.Adapter<BaseAdapter.Companion.BaseViewHolder<VB>>() {

    val items = mutableListOf<T>()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<T>?) {
        if (items != null) {
            this.items.clear()
            this.items.addAll(items)
            this.notifyDataSetChanged()
        }
    }

    fun addItems(newItems: List<T>) {
        val positionStart = this.items.size
        this.items.addAll(newItems)
        this.notifyItemRangeInserted(positionStart, newItems.size)
    }

    fun addItem(item: T) {
        this.items.add(item)
        this.notifyItemInserted(itemCount)
    }

    fun update(item: T, position: Int) {
        this.items[position] = item
        this.notifyItemChanged(position)
    }

    fun deleteItem(position: Int) {
        this.items.removeAt(position)
        this.notifyItemRemoved(position)
        this.notifyItemRangeChanged(position, items.size)
    }

    var listener: ((view: View, position: Int, item: T) -> Unit)? = null

    abstract fun getLayout(): Int

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BaseViewHolder<VB>(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            getLayout(),
            parent,
            false
        )
    )

    companion object {
        class BaseViewHolder<VB : ViewDataBinding>(val binding: VB) :
            RecyclerView.ViewHolder(binding.root)
    }
}