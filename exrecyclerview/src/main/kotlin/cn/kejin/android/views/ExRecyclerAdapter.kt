package cn.kejin.android.views

import android.app.Activity
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.view.ViewGroup
import cn.kejin.android.views.ItemActionListener
import java.util.*

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/3/16
 */

/**
 * ExRecyclerAdapter 内置类 List<Model> 的数据类型, 并实现对数据操作时的notify
 */
abstract class ExRecyclerAdapter<Model, Holder: RecyclerView.ViewHolder>(val activity : Activity)
                            : RecyclerView.Adapter<Holder>(), ItemActionListener
{
    protected var data : MutableList<Model> = mutableListOf()
        private set

    override fun getItemCount() = data.size

    private fun isValidPosition(pos: Int) = pos in 0..data.size-1

    fun get(pos: Int): Model? {
        return if (isValidPosition(pos)) data[pos] else null
    }

    /**
     * 设置单个数据
     * @param pos 位置
     * @param model 数据
     * @return Boolean 设置结果
     */
    fun set(pos: Int, model: Model, notify: Boolean=true) : Boolean {
        if (isValidPosition(pos)) {
            data[pos] = model
            if (notify) {
                notifyItemChanged(pos)
            }
            return true;
        }

        return false
    }

    /**
     * 重新设置所有的数据
     * @param list
     */
    fun set(list : Collection<Model>, notify: Boolean=true) {
        data.clear()
        data.addAll(list)
        if (notify) {
            notifyDataSetChanged()
        }
    }

    /**
     * 移动一个数据
     * @param from
     * @param to
     * @return Boolean
     */
    fun move(from: Int, to: Int, notify: Boolean=true): Boolean {
        if (from != to && isValidPosition(from) && isValidPosition(to)) {
            Collections.swap(data, from, to)
            if (notify) {
                notifyItemMoved(from, to)
            }
            return true;
        }
        return false
    }

    /**
     * 在 index 位置加入一个数据, 即 insert
     */
    fun add(index : Int, model: Model, notify: Boolean=true) {
        data.add(index, model)
        if (notify) {
            notifyItemInserted(index)
        }
    }

    /**
     * 追加一个数据
     */
    fun add(model: Model, notify: Boolean=true) {
        val index = data.size
        data.add(index, model)
        if (notify) {
            notifyItemInserted(index)
        }
    }

    /**
     * 追加所有的数据
     */
    fun addAll(list: Collection<Model>, notify: Boolean=true) {
        val insertPos = data.size
        data.addAll(list)
        if (notify) {
            notifyItemRangeInserted(insertPos, list.size)
        }
    }

    /**
     * 移除指定位置的数据
     */
    fun removeAt(index: Int, notify: Boolean=true): Boolean {
        if (isValidPosition(index)) {
            data.removeAt(index)
            if (notify) {
                notifyItemRemoved(index)
            }
            return true
        }
        return false
    }

    /**
     * 移除指定数据, 注意如果有多个相同的数据，则只会移除第一个
     */
    fun remove(model:Model, notify: Boolean=true): Boolean {
        val index = data.indexOf(model)
        if (isValidPosition(index)) {
            data.removeAt(index)
            if (notify) {
                notifyItemRemoved(index)
            }

            return true
        }
        return false
    }

    /**
     * 清除所有的数据
     */
    fun clear(notify: Boolean=true) {
        data.clear()
        if (notify) {
            notifyDataSetChanged()
        }
    }

//    var itemTouchHelper : ItemTouchHelper? = null

    override fun onItemMove(from: Int, to: Int): Boolean {
        return move(from, to)
    }

    override fun onItemSwiped(pos: Int) {
        removeAt(pos)
    }

    override fun onItemSelected(holder: RecyclerView.ViewHolder, pos: Int) {
        holder.itemView?.setBackgroundColor(Color.LTGRAY)
    }

    override fun onItemCleared(holder: RecyclerView.ViewHolder, pos: Int) {
        holder.itemView?.setBackgroundColor(0)
    }

    fun inflateView(id : Int, parent: ViewGroup? = null)
            = activity.layoutInflater.inflate(id, parent, false)

//    fun startDrag(holder: Holder) {
//        itemTouchHelper?.startDrag(holder)
//    }
//
//    fun startSwipe(holder: Holder) {
//        itemTouchHelper?.startSwipe(holder)
//    }

//    override fun onBindViewHolder(holder: Holder?, pos: Int) {
//        if (holder != null) {
//            if (holder is ExViewHolder<*>) {
//                holder.bindView(data[pos], pos)
//            }
//        }
//    }

    abstract class ExViewHolder<Model>(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        fun findView(id : Int) = itemView.findViewById(id)

        abstract fun bindView(model : Model, pos: Int)
    }
}