package com.wedo8020.app.mainui.home.chatAndNotification.adapter

import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.enumClasses.ChatType

abstract class BaseChatRecyclerAdapter<R> :
    RecyclerView.Adapter<BaseChatRecyclerAdapter<R>.ViewHolder>() {

    private var clickListeners = SparseArray<((position: Int, item: R) -> Unit)?>()

    var list: ArrayList<R> = ArrayList()

    abstract val layoutId: HashMap<ChatType, Int>

    abstract fun bind(holder: ViewHolder, item: R, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val type = layoutId.keys.find { k -> k.ordinal == viewType } ?: ChatType.MESSAGE_SENDER
        val id = layoutId[type] ?: layoutId[ChatType.MESSAGE_SENDER]
        Log.e("jjngsdfg",id.toString())
        Log.e("jjngsdfg",type.toString())
        Log.e("jjngsdfg",inflater.toString())
        Log.e("jjngsdfg",parent.toString())
        val t: ViewDataBinding =
            DataBindingUtil
                .inflate(
                    inflater,
                    id!!,
                    parent,
                    false)
        return ViewHolder(t, type)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = list[position]
        holder.preBind(current)
        bind(holder, current, position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addClickEventWithView(viewId: Int, onClickLambda: ((position: Int, item: R) -> Unit)?) {
        clickListeners.put(viewId, onClickLambda)
    }

    //Multiple viewHolder
    inner class ViewHolder(val binding: ViewDataBinding, val chatType: ChatType) :
        RecyclerView.ViewHolder(binding.root) {
        fun preBind(element: R) {
            clickListeners.forEach { key, value ->
                binding.root.findViewById<View>(key)
                    .setOnClickListener { value?.invoke(adapterPosition, element) }
            }
        }
    }

    //list methods
    fun updateItem(newItem: R, predicate: (R) -> Boolean) {
        val targetPos = list.indexOfFirst(predicate)
        if (targetPos != -1) {
            list[targetPos] = newItem
            notifyItemChanged(targetPos)
        }
    }

    fun updateItem(newItem: R, position: Int) {
        if (position != -1) {
            list[position] = newItem
            notifyItemChanged(position)
            notifyItemRangeChanged(position, list.size)
        }
    }

    fun moveItemUp(item: R, position: Int) {
        position.takeIf { it > 0 }?.also {
            list.removeAt(position).also {
                list.add(position - 1, it)
            }
            notifyItemMoved(position, position - 1)
            notifyItemRangeChanged(position - 1, list.size)

        }
    }

    fun moveItemDown(item: R, position: Int) {
        position.takeIf { it < list.size - 1 }?.also {
            list.removeAt(position).also {
                list.add(position + 1, it)
            }
            notifyItemMoved(position, position + 1)
            notifyItemRangeChanged(position, list.size)
        }
    }

    fun removeItem(predicate: (R) -> Boolean) {
        val targetPos = list.indexOfFirst(predicate)
        if (targetPos != -1) {
            list.removeAt(targetPos)
            notifyItemRemoved(targetPos)
            notifyItemRangeChanged(targetPos, list.size)
        }
    }

    fun removeItem(index: Int) {
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, list.size)
        }
    }

    fun setNewItems(newItems: List<R>) {
        list.clear()
        list.addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateList(newItems: ArrayList<R>) {
        list = newItems
        notifyDataSetChanged()
    }

    fun setNewItems(newItems: ArrayList<R>) {
        list.clear()
        list.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addNewItems(newItems: List<R>) {
        val oldPos = list.size + 1
        list.addAll(newItems)
        notifyItemRangeInserted(oldPos, newItems.size)
    }

    fun addNewItem(newItems: R) {
        list.add(newItems)
        notifyItemInserted(list.size - 1)
        onNewItemAdded()
    }


    //This fun using request on edit text field
    open fun onNewItemAdded() {


    }

    fun addNewItems(newItems: List<R>, targetPos: Int) {
        list.addAll(targetPos, newItems)
        notifyItemRangeInserted(targetPos, newItems.size)
        notifyItemRangeChanged(targetPos, newItems.size)
    }

    fun clearData() {
        list.clear()
        notifyDataSetChanged()
    }

    fun clearOnlyList() {
        list.clear()
    }


}