package cn.kejin.android

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.MotionEventCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.kejin.android.views.ExRecyclerAdapter
import kotlinx.android.synthetic.main.activity_custom.*

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/4/5
 */
class CustomActivity : AppCompatActivity() {

    val itemTouchHelper by lazy { exRecycler.itemTouchHelper }

    val adapter by lazy { Adapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_custom)
        exRecycler.adapter = adapter
        exRecycler.layoutManager = LinearLayoutManager(this)
        exRecycler.setOnLoadMoreListener {
            exRecycler.postDelayed(
                    {
                        adapter.addAll(listOf("A1", "A2", "A3", "A4", "A5", "A6", "A7"));
                        exRecycler.endLoadMore();
                    }, 2000)

            true
        }

        exRecycler.itemTouchCallback = CustomItemTouchCallback()
    }

    /**
     * 注意 viewHolder的position, 需要减去 exRecycler.getHeaderSize()
     */
    inner class CustomItemTouchCallback : ItemTouchHelper.Callback() {
        fun getRealPos(viewHolder: RecyclerView.ViewHolder): Int {
            return viewHolder.adapterPosition - exRecycler.getHeaderSize()
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }

        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            if (viewHolder != null && target != null) {
                return adapter.move(getRealPos(viewHolder), getRealPos(target))
            }
            return false
        }

        override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
            var dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            var swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            when (recyclerView?.layoutManager) {
                is StaggeredGridLayoutManager,
                is GridLayoutManager -> {
                    dragFlags = dragFlags or ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
                    swipeFlags = 0
                }
            }

            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
            when (viewHolder) {
                is Adapter.VH -> {
                    viewHolder.onSwiped(direction, getRealPos(viewHolder))
                }
            }
        }

        override fun onChildDraw(c: Canvas?,
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder?,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean) {
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_SWIPE -> {
                    when (viewHolder) {
                        is Adapter.VH -> {
                            val alpha: Float = 1.0f - Math.abs(dX) / viewHolder.itemView.width;
                            viewHolder.showLayout.alpha = alpha
                            viewHolder.showLayout.translationX = dX
                        }
                    }
                    return;
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
            super.clearView(recyclerView, viewHolder)

            when (viewHolder) {
                is Adapter.VH -> {
                    viewHolder.onCleared(getRealPos(viewHolder))
                }
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                when (viewHolder) {
                    is Adapter.VH -> {
                        viewHolder.onSelected(getRealPos(viewHolder))
                    }
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

    }

    inner class Adapter(activity: Activity) : ExRecyclerAdapter<String, Adapter.VH>(activity) {
        override fun onBindViewHolder(holder: VH?, position: Int) {
            holder?.bindView(data[position], position)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH? {
            return VH(View.inflate(activity, R.layout.layout_item_custom, parent)!!)
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val actionLayout by lazy { itemView.findViewById(R.id.actionLayout) }
            val showLayout by lazy { itemView.findViewById(R.id.showLayout) }

            fun bindView(model: String, pos: Int) {

                val text = itemView.findViewById(R.id.text) as TextView
                text.text = model

                itemView.setOnTouchListener { view, motionEvent ->
                    if (MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                        itemTouchHelper.startDrag(this)
                    }
                    false
                }
            }

            fun onSwiped(direction: Int, pos: Int) {
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        removeAt(pos)
                    }
                    else -> {
                        showLayout.visibility = View.GONE

                        itemView.findViewById(R.id.delete).setOnClickListener { removeAt(pos) }
                    }
                }
            }

            fun onCleared(pos: Int) {
                showLayout.alpha = 1.0f
                showLayout.visibility = View.VISIBLE
                showLayout.setBackgroundColor(Color.WHITE)
            }

            fun onSelected(pos: Int) {
                showLayout.setBackgroundColor(Color.RED)
            }
        }
    }
}
