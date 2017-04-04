package com.hc.myrecycler;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dl7.player.media.IjkPlayerView;

public class MainActivity extends AppCompatActivity {



    RecyclerView recyclerview;
    PullToRefreshAdapter pullToRefreshAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private IjkPlayerView mPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerview = (RecyclerView)findViewById(R.id.rv_list);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefreshAdapter.setEnableLoadMore(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshAdapter.setNewData(DataServer.getSampleData(PAGE_SIZE));
                        isErr = false;
                        mCurrentCounter = PAGE_SIZE;
                        mSwipeRefreshLayout.setRefreshing(false);
                        pullToRefreshAdapter.setEnableLoadMore(true);
                    }
                }, delayMillis);
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(Color.rgb(47, 223, 189));

        initAdapter();

        initPlayer();
//        Toast.makeText(MainActivity.this,"======"+pullToRefreshAdapter.getData().size(),Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayerView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayerView.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPlayerView.configurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPlayerView.handleVolumeKey(keyCode)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (mPlayerView.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    private static final String VIDEO_URL = "http://flv2.bn.netease.com/videolib3/1611/28/GbgsL3639/SD/movie_index.m3u8";
    private static final String VIDEO_HD_URL = "http://flv2.bn.netease.com/videolib3/1611/28/GbgsL3639/HD/movie_index.m3u8";
    private static final String IMAGE_URL = "http://vimg2.ws.126.net/image/snapshot/2016/11/I/M/VC62HMUIM.jpg";

    public void initPlayer(){
        mPlayerView = (IjkPlayerView) findViewById(R.id.player_view);
        Glide.with(this).load(IMAGE_URL).fitCenter().into(mPlayerView.mPlayerThumb); // Show the thumb before play
        mPlayerView.init()              // Initialize, the first to use
                .setTitle("Title")  	// set title
                .setSkipTip(1000*60*1)  // set the position you want to skip
                .enableOrientation()    // enable orientation
                //      .setVideoPath(VIDEO_URL)    // set video url
                .setVideoSource(null, VIDEO_URL, VIDEO_URL, VIDEO_URL, null) // set multiple video url
                .setMediaQuality(IjkPlayerView.MEDIA_QUALITY_HIGH);  // set the initial video url
//                .enableDanmaku()        // enable Danmaku
//                .setDanmakuSource(getResources().openRawResource(R.raw.comments)) // add Danmaku source, you need to use enableDanmaku() first
//                .start();   // Start playing
    }

    private static final int TOTAL_COUNTER = 18;

    private static final int PAGE_SIZE = 6;

    private int delayMillis = 1000;

    private int mCurrentCounter = 0;

    private boolean isErr;
    private boolean mLoadMoreEndGone = false;



    private void initAdapter() {
        pullToRefreshAdapter = new PullToRefreshAdapter();
        pullToRefreshAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener(){

            @Override
            public void onLoadMoreRequested() {
                mSwipeRefreshLayout.setEnabled(false);
                recyclerview.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (pullToRefreshAdapter.getData().size() < PAGE_SIZE) {
                            pullToRefreshAdapter.loadMoreEnd(true);
                        } else {
                            if (mCurrentCounter >= TOTAL_COUNTER) {
//                    pullToRefreshAdapter.loadMoreEnd();//default visible
                                pullToRefreshAdapter.loadMoreEnd(mLoadMoreEndGone);//true is gone,false is visible
                            } else {
                                if (isErr) {
                                    pullToRefreshAdapter.addData(DataServer.getSampleData(PAGE_SIZE));
                                    mCurrentCounter = pullToRefreshAdapter.getData().size();
                                    pullToRefreshAdapter.loadMoreComplete();
                                } else {
                                    isErr = true;
                                    Toast.makeText(MainActivity.this, R.string.network_err, Toast.LENGTH_LONG).show();
                                    pullToRefreshAdapter.loadMoreFail();

                                }
                            }
                            mSwipeRefreshLayout.setEnabled(true);
                        }
                    }

                }, delayMillis);
                Toast.makeText(MainActivity.this,"======",Toast.LENGTH_LONG).show();
            }
        },recyclerview);
        pullToRefreshAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
        recyclerview.setAdapter(pullToRefreshAdapter);

        pullToRefreshAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public boolean onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                Toast.makeText(MainActivity.this, view.getId()+" onItemChildClick " + position, Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        recyclerview.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(final BaseQuickAdapter adapter, final View view, final int position) {
                Toast.makeText(MainActivity.this, Integer.toString(position), Toast.LENGTH_LONG).show();
                mPlayerView.start();
            }
        });
    }

}
