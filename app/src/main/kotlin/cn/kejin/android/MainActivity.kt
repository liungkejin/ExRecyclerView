package cn.kejin.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import cn.kejin.exrecyclerview.ExRecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    val header: View
        get() {
            val view = View.inflate(this, R.layout.layout_header, null)
            val header = view.findViewById(R.id.header) as TextView
            header.text = "Header2"

            return view
        }

    val footer: View
        get() {
            val view = View.inflate(this, R.layout.layout_footer, null)
            view.findViewById(R.id.progress).visibility = View.GONE
            val footer = view.findViewById(R.id.footer) as TextView
            footer.text = "Footer2"

            return view
        }

    val adapter : Adapter by lazy { Adapter(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var items: MutableList<String> = mutableListOf()
        for (i in 0..10) {
            items.add(i.toString())
        }
        adapter.set(items)

        val headerCode = exRecycler.addHeader(header)
        val footerCode = exRecycler.addFooter(footer)

        exRecycler.adapter = adapter
        exRecycler.layoutManager = LinearLayoutManager(this)

        exRecycler.itemActionListener = adapter
        adapter.enableDragAndSwipe()

        exRecycler.setOnLoadMoreListener {
            exRecycler.postDelayed(
                    {
                        adapter.addAll(listOf("----", "A1", "A2", "A3", "A4", "A5", "A6", "A7"));
                        exRecycler.endLoadMore();
                    }, 2000)

            true
        }

        Toast.makeText(this, "Delete Header2 and Footer2 After 3 seconds", Toast.LENGTH_SHORT).show()
        exRecycler.postDelayed({
            exRecycler.removeHeader(headerCode)
            exRecycler.removeFooter(footerCode)
                               }, 3000)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.customItemTouch -> {
                startActivity(Intent(this, CustomActivity::class.java))
                return true;
            }
        }
        return super.onContextItemSelected(item)
    }

    /**
     * 这是使用了 ExRecyclerAdapter, 内置了 List 集合, 并实现了 ItemActionListener 接口
     */
    inner class Adapter(activity: Activity) : ExRecyclerAdapter<String, Adapter.VH>(activity) {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH? {
            return VH(inflateView(R.layout.layout_item, parent)!!)
        }

        override fun onBindViewHolder(holder: VH?, position: Int) {
            holder?.bindView(data[position], position)
        }

        inner class VH(view: View) : ExRecyclerAdapter.ExViewHolder<String>(view) {
            override fun bindView(model: String, pos: Int) {
                val text = findView(R.id.text) as TextView
                text.text = model
            }
        }
    }
}
