package cn.kejin.android.views

import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.kejin.android.views.R

import kotlin.collections.mutableListOf

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/3/22
 */
class ExRecyclerView: RecyclerView {
    companion object {
        val TAG = "ExRecyclerView"
    }

    /**
     * define in xml layout
     */
    var xmlHeader = 0
        private set

    protected val headerViews: MutableList<View> = mutableListOf()

    var xmlFooter = 0
        private set

    protected val footerViews: MutableList<View> = mutableListOf()

    protected val wrapper = AdapterWrapper()

    protected var wrappedAdapter: Adapter<ViewHolder>? = null;

    constructor(context: Context?) : this(context, null, 0)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {

        super.setAdapter(wrapper)
        if (context != null && attrs != null) {
            val attr = context.obtainStyledAttributes(attrs, R.styleable.ExRecyclerView, defStyle, 0)
            val headerId = attr.getResourceId(R.styleable.ExRecyclerView_header, 0)
            if (headerId != 0) {
                val header = View.inflate(context, headerId, null)
                if (header != null) {
                    xmlHeader = addHeader(header)
                }
            }

            val footerId = attr.getResourceId(R.styleable.ExRecyclerView_footer, 0)
            if (footerId != 0) {
                val footer = View.inflate(context, footerId, null)
                if (footer != null) {
                    xmlFooter = addFooter(footer)
                }
            }

            attr.recycle()

            this.itemAnimator
        }

        itemTouchHelper.attachToRecyclerView(this)
    }

    /**
     * get the header view that define in xml layout
     * @return view or null
     */
    fun getHeader() : View?
            = if (xmlHeader > 0) { getHeader(xmlHeader) } else {null}

    /**
     * remove the header view that define in xml layout
     */
    fun removeHeader() {
        if (xmlHeader > 0) {
            removeHeader(xmlHeader)
        }
    }

    /**
     * get header size
     * @return header views count
     */
    fun getHeaderSize() = headerViews.size

    /**
     * Whether has this header view
     * @param view View
     * @return Boolean
     */
    fun hasHeader(view: View) : Boolean
            = headerViews.contains(view)

    /**
     * find header view by hashcode
     * @param code view's hashcode
     * @return View or null
     */
    fun getHeader(code: Int) : View? {
        return headerViews.firstOrNull { it.hashCode() == code }
    }

    /**
     * add a header view,
     * @param view header view
     * @return Int the view hashcode, can use this code find the header view
     */
    fun addHeader(view: View) : Int {
        if (!headerViews.contains(view)) {
            headerViews.add(view)
            setFullSpan(view)

            wrapper.notifyItemInserted(getHeaderSize()-1)
        }

        return view.hashCode()
    }

    /**
     * remove a header view by hashcode
     * @param code view's hashcode
     */
    fun removeHeader(code: Int) {
        val index = headerViews.indexOfFirst { it.hashCode() == code }
        if (index >= 0) {
            if (code == xmlHeader) {
                xmlHeader = 0
            }

            headerViews.removeAt(index)
            wrapper.notifyItemRemoved(index)
        }
    }

    /**
     * remove a header view
     * @param view header view
     */
    fun removeHeader(view: View) {
        val index = headerViews.indexOf(view)
        if (index >= 0) {
            if (view.hashCode() == xmlHeader) {
                xmlHeader = 0
            }

            headerViews.removeAt(index)
            wrapper.notifyItemRemoved(index)
        }
    }

    ///////////////////////////////////Footers///////////////////////////////////////
    /**
     * get footer view that defined in xml layout
     * @return view or null
     */
    fun getFooter() : View?
            = if (xmlFooter > 0) { getFooter(xmlFooter) } else {null}

    /**
     * remove footer view that defined in xml layout
     */
    fun removeFooter() {
        if (xmlFooter != 0) {
            removeFooter(xmlFooter)
        }
    }

    /**
     * @return Int footer views count
     */
    fun getFooterSize() = footerViews.size

    /**
     * whether has a footer view
     * @param view
     * @return Boolean
     */
    fun hasFooter(view : View) : Boolean
            = footerViews.contains(view)

    /**
     * find the footer view by hashcode
     * @param code view's hashcode
     * @return view or null (not find)
     */
    fun getFooter(code: Int) : View? {
        return footerViews.firstOrNull { it.hashCode() == code }
    }

    /**
     * add a footer view
     * @param view footer view
     * @return Int view's hashcode
     */
    fun addFooter(view: View) : Int {
        if (!footerViews.contains(view)) {
            footerViews.add(view)
            setFullSpan(view)

            wrapper.notifyItemInserted(wrapper.itemCount)
        }

        return view.hashCode()
    }

    /**
     * remove a footer view
     * @param code view's hashcode
     */
    fun removeFooter(code: Int) {
        val index = footerViews.indexOfFirst { it.hashCode() == code }
        if (index >= 0) {
            if (code == xmlFooter) {
                xmlFooter = 0
            }
            footerViews.removeAt(index)
            wrapper.notifyItemRemoved(index)
        }
    }

    /**
     * remove a footer view
     * @param view footer view
     */
    fun removeFooter(view: View) {
        val index = footerViews.indexOf(view)
        if (index >= 0) {
            if (view.hashCode() == xmlFooter) {
                xmlFooter = 0
            }

            footerViews.removeAt(index)
            wrapper.notifyItemRemoved(getHeaderSize() + (wrappedAdapter?.itemCount ?: 0) + index)
        }
    }

    /**
     * get all item count, include headers and footers
     */
    fun getItemCount(): Int {
        return getHeaderSize() + getFooterSize() + getWrapItemCount()
    }

    private fun getWrapItemCount()
            = wrappedAdapter?.itemCount?:0

    override fun getAdapter(): Adapter<*>? {
        return wrappedAdapter
    }

    @Suppress("UNCHECKED_CAST")
    override fun setAdapter(wrapAdapter: Adapter<*>?) {
        wrappedAdapter?.unregisterAdapterDataObserver(adapterDataObserver)
        if (wrapAdapter != null) {
            wrappedAdapter = wrapAdapter as Adapter<ViewHolder>
            wrappedAdapter?.registerAdapterDataObserver(adapterDataObserver)

            wrapAdapter.notifyDataSetChanged()
        }
        else {
            wrappedAdapter = null
        }

        adapterDataObserver.onChanged()
    }

    override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)

        if (layout != null && layout is GridLayoutManager) {
            configGridLayoutManager(layout)
        }

        headerViews.forEach { setFullSpan(it) }
        footerViews.forEach { setFullSpan(it) }
    }

    private fun configGridLayoutManager(layoutManager: GridLayoutManager) {
        val oldLookup = layoutManager.spanSizeLookup;
        val spanCount = layoutManager.spanCount;
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (isHeaderOrFooterPos(position)) {
                    return spanCount
                }
                return oldLookup.getSpanSize(position - getHeaderSize())
            }
        }
    }

    /**
     * 设置 LayoutParams, 如果不设置, 在 LinearLayoutManager 时有时会出现不能填满宽度的情况
     */
    private fun setFullSpan(view: View?) {
        if (view == null || layoutManager == null) {
            return
        }

        if (layoutManager is StaggeredGridLayoutManager) {
            val layoutParams = StaggeredGridLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.isFullSpan = true
            view.layoutParams = layoutParams
        }
        else {
            val layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.layoutParams = layoutParams
        }
    }

    private fun isHeaderOrFooterPos(pos: Int): Boolean {
        return (pos < getHeaderSize() || pos >= getHeaderSize() + (wrappedAdapter?.itemCount ?: 0))
    }

    protected inner class AdapterWrapper : Adapter<ViewHolder>(){

        override fun getItemViewType(position: Int): Int {
            if (isHeaderOrFooterPos(position)) {
                return -position - 1;
            }

            val pos = position - getHeaderSize()
            return wrappedAdapter?.getItemViewType(pos) ?: 0;
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            if (!isHeaderOrFooterPos(position)) {
                wrappedAdapter?.onBindViewHolder(holder, position-getHeaderSize())
            }
        }

        override fun getItemCount(): Int {
            return getHeaderSize() + getFooterSize() + getWrapItemCount()
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
            if (viewType < 0) {
                var pos = -(viewType + 1)
                if (pos >= 0 && pos < getHeaderSize()) {
                    val header = headerViews[pos]
                    val pView = header.parent
                    if (pView == parent) {
                        pView?.removeView(header)
                    }
                    return WrapperViewHolder(header)
                }

                pos -= getHeaderSize() + getWrapItemCount()
                if (pos >= 0 && pos < getFooterSize()) {
                    val footer = footerViews[pos]
                    val fView = footer.parent
                    if (fView == parent) {
                        fView?.removeView(footer)
                    }

                    return WrapperViewHolder(footer)
                }
                return null;
            }

            return wrappedAdapter?.onCreateViewHolder(parent, viewType)
        }

        inner class WrapperViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
            init {
                setIsRecyclable(false)
            }
        }
    }

    /**
     * Data observer
     */
    var lastWrappedAdapterSize = getWrapItemCount()
    private fun onWrappedDataSizeChanged() {
        lastWrappedAdapterSize = getWrapItemCount()
    }

    private val adapterDataObserver = object : AdapterDataObserver() {
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            wrapper.notifyItemMoved(getHeaderSize() + fromPosition, getHeaderSize() + toPosition)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onWrappedDataSizeChanged()

            wrapper.notifyItemRangeInserted(getHeaderSize() + positionStart, itemCount)
//            wrapper.notifyItemRangeChanged(positionStart + getHeaderSize() + itemCount,
//                    wrapper.itemCount - (getHeaderSize() + positionStart + itemCount) - getFooterSize())
        }

        override fun onChanged() {
            val curItemCount = getWrapItemCount()
            var deltaCount = lastWrappedAdapterSize-curItemCount
            if (deltaCount > 0) {
                wrapper.notifyItemRangeRemoved(getHeaderSize()+curItemCount, deltaCount)
            }
            else if (deltaCount < 0) {
                deltaCount = -deltaCount
                wrapper.notifyItemRangeInserted(getHeaderSize()+lastWrappedAdapterSize, deltaCount)
            }

            wrapper.notifyItemRangeChanged(getHeaderSize(), curItemCount)
            lastWrappedAdapterSize = curItemCount
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onWrappedDataSizeChanged()
            wrapper.notifyItemRangeRemoved(getHeaderSize() + positionStart, itemCount)
//            wrapper.notifyItemRangeChanged(getHeaderSize() + positionStart,
//                    wrapper.itemCount - getHeaderSize() - positionStart - getFooterSize())
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            wrapper.notifyItemRangeChanged(getHeaderSize() + positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            wrapper.notifyItemRangeChanged(getHeaderSize() + positionStart, itemCount, payload)
        }
    }

    /******************************************** Load More Function ******************************/

    var isLoadingMore = false
        private set

    var loadMoreListener : OnLoadMoreListener? = null

    fun setOnLoadMoreListener(r : ()->Boolean) {
        loadMoreListener = object : OnLoadMoreListener {
            override fun onLoadMore() : Boolean {
                return r()
            }
        }
    }

    fun endLoadMore() {
        isLoadingMore = false
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        val wrapperSize = wrappedAdapter?.itemCount?:0
        if (!isLoadingMore && loadMoreListener != null && wrapperSize > 0) {
            val visPos = getVisiblePos()
            if (visPos.second+1 >= getHeaderSize()+wrapperSize) {
                isLoadingMore = loadMoreListener?.onLoadMore()?:false
            }
        }
    }

    /**
     * get first and last visible item position
     */
    private fun getVisiblePos() : Pair<Int, Int> {
        if (layoutManager == null) {
            return Pair(0, 0)
        }

        var first = 0
        var last = 0
        when (layoutManager) {
            is GridLayoutManager -> {
                first = (layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
                last = (layoutManager as GridLayoutManager).findLastVisibleItemPosition()
            }

            is LinearLayoutManager -> {
                first = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition();
                last = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            }

            is StaggeredGridLayoutManager -> {
                var firstPos : IntArray = IntArray(2, {0});
                (layoutManager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(firstPos)

                var lastPos : IntArray = IntArray(2, {0});
                (layoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(lastPos)

                first = firstPos.min()?:0
                last = lastPos.max()?:0
            }
        }

        return Pair(first, last)
    }

    interface OnLoadMoreListener {
        fun onLoadMore() : Boolean
    }


    /***************************************** Drag & Swipe Dismiss Function *********************/

    //TODO 支持 swipe dismiss header or footer
    val itemTouchHelper = ItemTouchHelper(ItemTouchCallbackDocker())

    /**
     * 自定义的ItemTouchCallback
     * 注意: itemTouchCallback 里面的 viewHolder的adapterPosition 都是包含了 headerSize 的position
     * 所以需要在使用的时候, 减去 exRecycler.getHeaderSize()
     */
    var itemTouchCallback : ItemTouchHelper.Callback? = null;

    /**
     *  longPressDragEnable
     *  itemViewSwipeEnable
     *  itemActionListener
     *  都只有在没有自定义的 ItemTouchHelper.Callback 的情况下才可用的
     *  即 itemTouchCallback != null 的情况下
     *  默认 Drag 和 Swipe 都是关闭的
     */
    var longPressDragEnable = false
    var itemViewSwipeEnable = false

    fun enableDragAndSwipe() {
        longPressDragEnable = true
        itemViewSwipeEnable = true
    }

    fun disableDragAndSwipe() {
        longPressDragEnable = false
        itemViewSwipeEnable = false
    }

    var itemActionListener : ItemActionListener = object : ItemActionListener {
        override fun onItemMove(from: Int, to: Int): Boolean {
            return false
        }

        override fun onItemSwiped(pos: Int) {
            //
        }

        override fun onItemSelected(holder: ViewHolder, pos: Int) {
            //
        }

        override fun onItemCleared(holder: ViewHolder, pos: Int) {
            //
        }
    }

    /**
     * 封装 Callback 保证 header 和 footer 不会被移动
     */
    protected inner class ItemTouchCallbackDocker : ItemTouchHelper.Callback() {

        override fun getMoveThreshold(viewHolder: ViewHolder?): Float {
            return itemTouchCallback?.getMoveThreshold(viewHolder)?:super.getMoveThreshold(viewHolder)
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return itemTouchCallback?.isItemViewSwipeEnabled?:isItemViewSwipeEnabled
        }

        override fun isLongPressDragEnabled(): Boolean {
            return itemTouchCallback?.isLongPressDragEnabled?:isLongPressDragEnabled
        }

        override fun canDropOver(recyclerView: RecyclerView?, current: ViewHolder?, target: ViewHolder?): Boolean {
            return itemTouchCallback?.canDropOver(recyclerView, current, target)?:super.canDropOver(recyclerView, current, target)
        }

        override fun chooseDropTarget(selected: ViewHolder?, dropTargets: MutableList<ViewHolder>?, curX: Int, curY: Int): ViewHolder? {
            return itemTouchCallback?.chooseDropTarget(selected, dropTargets, curX, curY)?:super.chooseDropTarget(selected, dropTargets, curX, curY)
        }

        override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            itemTouchCallback?.onChildDraw(c, recyclerView, viewHolder, dX,dY, actionState, isCurrentlyActive)
                    ?: defaultChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        /**
         * 默认的 swipe 效果
         */
        fun defaultChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            if (viewHolder != null) {
                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_SWIPE -> {
                        val alpha: Float = 1.0f - Math.abs(dX) / viewHolder.itemView.width;
                        viewHolder.itemView.alpha = alpha;
                        viewHolder.itemView.translationX = dX;
                        return;
                    }
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        override fun clearView(recyclerView: RecyclerView?, viewHolder: ViewHolder?) {
            itemTouchCallback?.clearView(recyclerView, viewHolder)?:defaultClearView(recyclerView, viewHolder)
        }

        /**
         * 默认的clear view
         */
        fun defaultClearView(recyclerView: RecyclerView?, viewHolder: ViewHolder?) {
            super.clearView(recyclerView, viewHolder)

            if (viewHolder != null) {
                viewHolder.itemView.alpha = 1.0f
                itemActionListener.onItemCleared(
                        viewHolder, viewHolder.adapterPosition-getHeaderSize())
            }
        }

        override fun onMoved(recyclerView: RecyclerView?, viewHolder: ViewHolder?, fromPos: Int, target: ViewHolder?, toPos: Int, x: Int, y: Int) {
            itemTouchCallback?.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
                    ?:super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        }

        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            itemTouchCallback?.onSelectedChanged(viewHolder, actionState)
                    ?:defaultOnSelectedChanged(viewHolder, actionState)
        }

        /**
         * default
         */
        fun defaultOnSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            if (viewHolder != null &&
                    actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                itemActionListener.onItemSelected(
                        viewHolder, viewHolder.adapterPosition-getHeaderSize())
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun getAnimationDuration(recyclerView: RecyclerView?, animationType: Int, animateDx: Float, animateDy: Float): Long {
            return itemTouchCallback?.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
                    ?:super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
        }

        override fun getBoundingBoxMargin(): Int {
            return itemTouchCallback?.boundingBoxMargin?:super.getBoundingBoxMargin()
        }

        override fun onChildDrawOver(c: Canvas?, recyclerView: RecyclerView?, viewHolder: ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            itemTouchCallback?.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    ?:super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        override fun interpolateOutOfBoundsScroll(recyclerView: RecyclerView?, viewSize: Int, viewSizeOutOfBounds: Int, totalSize: Int, msSinceStartScroll: Long): Int {
            return itemTouchCallback?.interpolateOutOfBoundsScroll(recyclerView, viewSize, viewSizeOutOfBounds, totalSize, msSinceStartScroll)
                    ?:super.interpolateOutOfBoundsScroll(recyclerView, viewSize, viewSizeOutOfBounds, totalSize, msSinceStartScroll)
        }

        override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
            return itemTouchCallback?.getSwipeEscapeVelocity(defaultValue)
                    ?:super.getSwipeEscapeVelocity(defaultValue)
        }

        override fun getSwipeThreshold(viewHolder: ViewHolder?): Float {
            return itemTouchCallback?.getSwipeThreshold(viewHolder)?:super.getSwipeThreshold(viewHolder)
        }

        override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
            return itemTouchCallback?.convertToAbsoluteDirection(flags, layoutDirection)
                    ?:super.convertToAbsoluteDirection(flags, layoutDirection)
        }

        override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
            return itemTouchCallback?.getSwipeVelocityThreshold(defaultValue)
                    ?:super.getSwipeVelocityThreshold(defaultValue)
        }

        override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: ViewHolder?): Int {
            /**
             * 禁止移动header 或者 footer
             */
            if (viewHolder != null && isHeaderOrFooterPos(viewHolder.adapterPosition)) {
                return makeMovementFlags(0, 0)
            }

            return itemTouchCallback?.getMovementFlags(recyclerView, viewHolder)?:defaultGetMovementFlags(recyclerView, viewHolder)
        }

        /**
         * 默认Drag 和 swipe的移动flags
         */
        fun defaultGetMovementFlags(recyclerView: RecyclerView?, viewHolder: ViewHolder?): Int {
            var dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            var swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END

            when (recyclerView?.layoutManager) {
                is StaggeredGridLayoutManager,
                is GridLayoutManager -> {
                    dragFlags = dragFlags or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    swipeFlags = 0
                }
            }

            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(recyclerView: RecyclerView?, viewHolder: ViewHolder?, target: ViewHolder?): Boolean {
            return itemTouchCallback?.onMove(recyclerView, viewHolder, target)
                    ?:defaultOnMove(recyclerView, viewHolder, target);
        }

        /**
         * 默认的move交给 ItemActionListener
         */
        fun defaultOnMove(recyclerView: RecyclerView?,
                          viewHolder: ViewHolder?,
                          target: ViewHolder?): Boolean {
            if (viewHolder != null && target != null) {
                return itemActionListener.onItemMove(
                        viewHolder.adapterPosition-getHeaderSize(),
                        target.adapterPosition-getHeaderSize())
            }

            return false
        }

        override fun onSwiped(viewHolder: ViewHolder?, direction: Int) {
            itemTouchCallback?.onSwiped(viewHolder, direction)?:defaultOnSwiped(viewHolder, direction)
        }

        /**
         * 默认交给 ItemActionListener 处理
         */
        fun defaultOnSwiped(viewHolder: ViewHolder?, direction: Int) {
            if (viewHolder != null) {
                itemActionListener.onItemSwiped(viewHolder.adapterPosition-getHeaderSize())
            }
        }
    }
//
//    protected  val itemTouchCallback2 = object : ItemTouchHelper.Callback() {
//        override fun isItemViewSwipeEnabled(): Boolean {
//            return itemViewSwipeEnable
//        }
//
//        override fun isLongPressDragEnabled(): Boolean {
//            return longPressDragEnable
//        }
//
//        override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: ViewHolder?): Int {
//            var dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
//            var swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
//            if (viewHolder != null && isHeaderOrFooterPos(viewHolder.adapterPosition)) {
//                dragFlags = 0
//                swipeFlags = 0
//            }
//            else {
//                when (recyclerView?.layoutManager) {
//                    is StaggeredGridLayoutManager,
//                    is GridLayoutManager -> {
//                        dragFlags = dragFlags or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//                        swipeFlags = 0
//                    }
//                }
//            }
//
//            return makeMovementFlags(dragFlags, swipeFlags)
//        }
//
//        override fun onMove(recyclerView: RecyclerView?,
//                            viewHolder: ViewHolder?,
//                            target: ViewHolder?): Boolean {
//            if (viewHolder != null && target != null) {
//                return itemActionListener.onItemMove(
//                        viewHolder.adapterPosition-getHeaderSize(),
//                        target.adapterPosition-getHeaderSize())
//            }
//
//            return false
//        }
//
//        override fun onSwiped(viewHolder: ViewHolder?, direction: Int) {
//            if (viewHolder != null) {
//                itemActionListener.onItemSwiped(viewHolder.adapterPosition-getHeaderSize())
//            }
//        }
//
//        override fun onChildDraw(c: Canvas?,
//                                 recyclerView: RecyclerView?,
//                                 viewHolder: ViewHolder?,
//                                 dX: Float,
//                                 dY: Float,
//                                 actionState: Int,
//                                 isCurrentlyActive: Boolean) {
//            if (viewHolder != null) {
//                when (actionState) {
//                    ItemTouchHelper.ACTION_STATE_SWIPE -> {
//                        val alpha: Float = 1.0f - Math.abs(dX) / viewHolder.itemView.width;
//                        viewHolder.itemView.alpha = alpha;
//                        viewHolder.itemView.translationX = dX;
//                        return;
//                    }
//                }
//            }
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//        }
//
//        override fun clearView(recyclerView: RecyclerView?, viewHolder: ViewHolder?) {
//            super.clearView(recyclerView, viewHolder)
//
//            if (viewHolder != null) {
//                viewHolder.itemView.alpha = 1.0f
//                itemActionListener.onItemCleared(
//                        viewHolder, viewHolder.adapterPosition-getHeaderSize())
//            }
//        }
//
//        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
//            if (viewHolder != null &&
//                    actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//                itemActionListener.onItemSelected(
//                        viewHolder, viewHolder.adapterPosition-getHeaderSize())
//            }
//            super.onSelectedChanged(viewHolder, actionState)
//        }
//    }
//
//    protected val defItemActionListener = object : ItemActionListener {
//        override fun onItemMove(from: Int, to: Int): Boolean {
//            return false
//        }
//
//        override fun onItemSwiped(pos: Int) {
//            //
//        }
//
//        override fun onItemSelected(holder: ViewHolder, pos: Int) {
//            //
//        }
//
//        override fun onItemCleared(holder: ViewHolder, pos: Int) {
//            //
//        }
//    }

}