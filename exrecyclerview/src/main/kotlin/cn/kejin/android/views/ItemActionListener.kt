package cn.kejin.android.views

import android.support.v7.widget.RecyclerView

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/4/4
 */

interface ItemActionListener {
    fun onItemMove(from: Int, to: Int): Boolean

    fun onItemSwiped(pos: Int)

    fun onItemSelected(holder: RecyclerView.ViewHolder, pos: Int)

    fun onItemCleared(holder: RecyclerView.ViewHolder, pos: Int)
}