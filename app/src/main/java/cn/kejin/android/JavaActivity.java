package cn.kejin.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

import cn.kejin.exrecyclerview.ExRecyclerAdapter;
import cn.kejin.exrecyclerview.ExRecyclerView;


/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/4/14
 */
public class JavaActivity extends AppCompatActivity
{

    private ExRecyclerView exRecyclerView;
    private Adapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        exRecyclerView = (ExRecyclerView) findViewById(R.id.exRecycler);

        adapter = new Adapter(this);

        exRecyclerView.setAdapter(adapter);

        exRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        /**
         * 因为 Adapter 实现了一个基本的 ItemActionListener
         */
        exRecyclerView.setItemActionListener(adapter);
        adapter.enableDragAndSwipe();

        exRecyclerView.setOnLoadMoreListener(new ExRecyclerView.OnLoadMoreListener(){

            @Override
            public boolean onLoadMore()
            {
                exRecyclerView.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String [] str = new String[] {
                                "----", "A1", "A2", "A3", "A4", "A5", "A6", "A7"
                        };
                        ArrayList<String> data = new ArrayList<String>();
                        Collections.addAll(data, str);
                        /**
                         * 第二个参数为是否 notify
                         */
                        adapter.addAll(data, true);

                        /**
                         * 主动调用 endLoadMore()
                         */
                        exRecyclerView.endLoadMore();
                    }
                }, 2000);
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.customItemTouch:
                Intent intent = new Intent(this, CustomActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class Adapter extends ExRecyclerAdapter<String, Adapter.ViewHolder>
    {


        public Adapter(@NotNull Activity activity)
        {
            super(activity);
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new ViewHolder(inflateView(R.layout.layout_item, parent));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position)
        {
            if (holder != null) {
                holder.bindView(get(position), position);
            }
        }


        public class ViewHolder extends ExRecyclerAdapter.ExViewHolder<String> {

            public ViewHolder(@NotNull View itemView)
            {
                super(itemView);
            }

            @Override
            public void bindView(String s, int i)
            {
                TextView textView = (TextView) findView(R.id.text);
                textView.setText(s);
            }
        }
    }
}
