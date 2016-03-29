package com.koolearn.android.kooreader.netbook;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.koolearn.android.kooreader.libraryService.BookCollectionShadow;
import com.koolearn.android.kooreader.netbook.adapter.NetBookListAdapter;
import com.koolearn.android.kooreader.netbook.entity.NetBook;
import com.koolearn.android.kooreader.netbook.entity.NetBookWraper;
import com.koolearn.android.kooreader.util.HttpClientUtils;
import com.koolearn.android.util.LogUtil;
import com.koolearn.android.volley.FastJSONRequest;
import com.koolearn.android.volley.FastResponse;
import com.koolearn.android.volley.VolleyManager;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.kooreader.Paths;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 网络书籍列表 展示
 */
public class NetBookMainActivity extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener {

    private static final int MSG_NOTIFIY_DATA = 0x1001;
    private static final int MSG_REFRESH_DATA = 0x1002;

    private List<NetBook> bookshelf = new ArrayList<NetBook>();
    private NetBookListAdapter mBookAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;

    private RequestQueue mQueue;
    private final BookCollectionShadow myCollection = new BookCollectionShadow();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_NOTIFIY_DATA:
                    displayBook();
                    break;
                case MSG_REFRESH_DATA:
                    refreshLayout.setRefreshing(false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_book_main);

        init();
        mQueue = Volley.newRequestQueue(this);
        mQueue.start();
        getBooks();
    }

    // 初始化相关 获取网络书籍信息
    private void init(){
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.progressBarBlue, R.color.progressBarBgWhiteOrange);
        refreshLayout.setProgressBackgroundColor(R.color.progressBarBgGreen);
    }

    @Override
    public void onRefresh() {
        handler.sendEmptyMessage(MSG_REFRESH_DATA);
    }

    // 获取网络书籍信息
    private void getBooks(){
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("page", "1");
        params.put("categoryid", "1");
        params.put("pageSize", "1");
        FastJSONRequest request = new FastJSONRequest("http://192.168.102.84:8080" +
                "/VYReader/api/getBook?page=1&pageSize=1&categoryid=1", "", new FastResponse.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj, String executeMethod, String flag, boolean dialogFlag) {
                if (obj != null) {
                    NetBookWraper wraper = JSON.parseObject(obj.toString(), NetBookWraper.class);
                    if (wraper != null && wraper.getStatus() == 1) {
                        bookshelf = wraper.getData();
                        handler.sendEmptyMessage(MSG_NOTIFIY_DATA);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                LogUtil.i("get Books error");
            }
        });
        VolleyManager.getInstance().beginSubmitRequest(mQueue, request);
    }

    private void displayBook() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        mBookAdapter = new NetBookListAdapter(this, bookshelf);
        recyclerView.setAdapter(mBookAdapter);
        mBookAdapter.setOnItemClickListener(new NetBookListAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, NetBook data) {
                // 先实时下载书籍 然后打开
//                KooReader.openBookActivity(NetBookMainActivity.this, data, null);
                if (data.getSrcUrl() != null && !"".equals(data.getSrcUrl())) { // 下载书籍
                    downloadBook(data.getSrcUrl(), Paths.internalTempDirectoryValue(NetBookMainActivity.this));
                } else {
                    // 书籍地址有误
                    ///storage/emulated/0/Android/data/com.koolearn.klibrary.ui.android/cache//134514.epub
                }
                overridePendingTransition(R.anim.tran_fade_in, R.anim.tran_fade_out);
            }
        });
    }

    /**
     * 下载书籍
     * @param url
     */
    private void downloadBook(final String url,final String bookPath){
        new Thread(){
            public void run(){
                // 待完善
                String bookName = url.substring(url.lastIndexOf("/")+1, url.length());
                LogUtil.i(bookName);
                // 需添加进度显示
                String path = HttpClientUtils.downloadAndSaveToFile(url,bookPath + "/" + bookName);
                LogUtil.i("成功下载"+path);
            }
        }.start();

    }


    public String getFilePath(String epubName,String data){
        final String fileName = Paths.internalTempDirectoryValue(this) + "/" + epubName;
            File file = new File(fileName);
            if (file.exists()) {
                return "";
            }
        return fileName;
    }
}
