package com.chaojishipin.sarrs.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.chaojishipin.sarrs.R;
import com.chaojishipin.sarrs.activity.ChaoJiShiPinMainActivity;
import com.chaojishipin.sarrs.activity.ChaoJiShiPinVideoDetailActivity;
import com.chaojishipin.sarrs.activity.ChaojishipinLivePlayActivity;
import com.chaojishipin.sarrs.activity.ChaojishipinRegisterActivity;
import com.chaojishipin.sarrs.activity.ChaojishipinSplashActivity;
import com.chaojishipin.sarrs.activity.PlayActivityFroWebView;
import com.chaojishipin.sarrs.activity.SearchActivity;
import com.chaojishipin.sarrs.adapter.LiveActivityChannelAdapter;
import com.chaojishipin.sarrs.adapter.MainActivityChannelAdapter2;
import com.chaojishipin.sarrs.bean.AddFavorite;
import com.chaojishipin.sarrs.bean.CancelFavorite;
import com.chaojishipin.sarrs.bean.CheckFavorite;
import com.chaojishipin.sarrs.bean.LiveDataEntity;
import com.chaojishipin.sarrs.bean.LiveDataInfo;
import com.chaojishipin.sarrs.bean.LivePlayData;
import com.chaojishipin.sarrs.bean.LiveStreamEntity;
import com.chaojishipin.sarrs.bean.LiveStreamInfo;
import com.chaojishipin.sarrs.bean.MainActivityAlbum;
import com.chaojishipin.sarrs.bean.MainActivityData;
import com.chaojishipin.sarrs.bean.MainMenuItem;
import com.chaojishipin.sarrs.bean.SlidingMenuLeft;
import com.chaojishipin.sarrs.bean.UploadRecord;
import com.chaojishipin.sarrs.bean.VideoDetailItem;
import com.chaojishipin.sarrs.feedback.DataReporter;
import com.chaojishipin.sarrs.http.volley.HttpApi;
import com.chaojishipin.sarrs.http.volley.HttpManager;
import com.chaojishipin.sarrs.http.volley.RequestListener;
import com.chaojishipin.sarrs.listener.onRetryListener;
import com.chaojishipin.sarrs.feedback.DataHttpApi;
import com.chaojishipin.sarrs.feedback.DataReportListener;
import com.chaojishipin.sarrs.feedback.DataReporter;
import com.chaojishipin.sarrs.manager.FavoriteManager;
import com.chaojishipin.sarrs.swipe.SwipeMenu;
import com.chaojishipin.sarrs.swipe.SwipeMenuCreator;
import com.chaojishipin.sarrs.swipe.SwipeMenuItem;
import com.chaojishipin.sarrs.thirdparty.LoginUtils;
import com.chaojishipin.sarrs.thirdparty.UserLoginState;
import com.chaojishipin.sarrs.thirdparty.share.ShareDataConfig;
import com.chaojishipin.sarrs.uploadstat.UploadStat;
import com.chaojishipin.sarrs.utils.ConstantUtils;
import com.chaojishipin.sarrs.utils.LogUtil;
import com.chaojishipin.sarrs.utils.NetWorkUtils;
import com.chaojishipin.sarrs.utils.ToastUtil;
import com.chaojishipin.sarrs.utils.Utils;
import com.chaojishipin.sarrs.widget.DeleteRelativelayout;
import com.chaojishipin.sarrs.widget.NetStateView;
import com.chaojishipin.sarrs.widget.PullToRefreshSwipeListView;
import com.chaojishipin.sarrs.widget.PullToRefreshSwipeMenuListView;
import com.chaojishipin.sarrs.widget.SarrsMainMenuView;
import com.chaojishipin.sarrs.widget.SarrsToast;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ibest.thirdparty.share.model.ShareData;
import com.ibest.thirdparty.share.view.ShareDialog;
import com.umeng.analytics.MobclickAgent;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by xll on 2015/6/17.
 */
public class MainChannelFragment extends MainBaseFragment implements  View.OnClickListener,
        AdapterView.OnItemClickListener {
    //    PullToRefreshSwipeListView.OnSwipeListener, PullToRefreshSwipeListView.OnMenuItemClickListener,
    public static String pageid = "00S002001";
    public MainActivityChannelAdapter2 mainActivityChannelAdapter;
    private String mCid;
    private int mSwipePosition = -1;

    private String mArea;
    private String mCurrentTitle = ConstantUtils.TITLE_SUGGEST;

    private final static int MESSAGE_DELAYED_TIME = 3000;

    private ArrayList<MainActivityAlbum> mAlbumLists=new ArrayList<MainActivityAlbum>();
    // mode ==0下拉刷新// mode==1 上拉刷新 // mode==2
    private int reQMode = 1;
    SlidingMenuLeft slidingMenuLeft;
    private List<String> alreadyupgvid = new ArrayList<String>();
    private ChaoJiShiPinMainActivity activity;
    //    public int firstvisiblecount = 2;
    // listView一屏幕可见最大item项
    private int max_visible_count = Integer.MIN_VALUE;
    /**
     * 构造 添加、是否存在、取消收藏统一参数
     */
    String id = "";
    String token = null;
    String type = "";
    String cid = "";
    String netType = NetWorkUtils.getNetInfo();
    // 上报参数
    String source = "";
    String bucket;
    String seid;
    VideoDetailItem detail = null;
    int mparentId = 0;

    // live相关
    private LiveActivityChannelAdapter mLiveactivitychanneladapter;
    private ArrayList<LiveDataEntity> mLiveItemLists = new ArrayList<LiveDataEntity>();
    private ArrayList<LiveStreamEntity> mLiveStreamLists = new ArrayList<LiveStreamEntity>();
    private ArrayList<String> mLivePlayStreams = new ArrayList<String>();
    private String mLivePlayUrl;

    @Override
    protected void init(){
        SarrsMainMenuView.listviewItemHeight = 350;
        SarrsMainMenuView.mode=ConstantUtils.SarrsMenuInitMode.MODE_DELETE_SAVE_SHARE;
        activity = (ChaoJiShiPinMainActivity) getActivity();

		if (!isLiveChannel()) {	
            mainActivityChannelAdapter = new MainActivityChannelAdapter2(getActivity());
            mXListView.setAdapter(mainActivityChannelAdapter);
        } else {
            mLiveactivitychanneladapter = new LiveActivityChannelAdapter(getActivity());
            mXListView.setAdapter(mLiveactivitychanneladapter);
        }
		
        mSearchIcon.setOnClickListener(this);
        mXListView.setOnItemClickListener(this);
        mXListView.setOnScrollListener(new AbsListView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
				if(i==AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    uploadstat(absListView);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (visibleItemCount > max_visible_count) {
                    max_visible_count = visibleItemCount;
                    LogUtil.e(Utils.LIVE_TAG, "%%%%%%%%%%%%%%%%%%%%%%%%%%%cur phone one screen visiable item num is " + max_visible_count);
                }
                int lastvisibleposition = view.getLastVisiblePosition();
                if (lastvisibleposition > (max_visible_count)) {
                    activity.setmTitleActionBarTitle(activity.getResources().getString(R.string.double_click2top));
                } else {
                    activity.ResetmTitleActionBarTitle();
                }
            }
        });

        if (NetWorkUtils.isNetAvailable()) {
            hideErrorView(mRootView);
        } else {
            showErrorView(mRootView);
        }
        mXListView.setMode(PullToRefreshSwipeListView.Mode.BOTH);

        setListViewMode();
        if (mAlbumLists != null) {
            mAlbumLists.clear();
        }
		if (null != mLiveItemLists)
            mLiveItemLists.clear();
        if(mainActivityChannelAdapter.menuStates!=null){
            mainActivityChannelAdapter.menuStates.clear();
        }
        mXListView.setOnRefreshListener(refreshListener2);

        slidingMenuLeft = ((ChaoJiShiPinMainActivity)getActivity()).getSlidingMenuLeft();
        if (slidingMenuLeft != null) {
            getNetData(slidingMenuLeft);
        }else{
            slidingMenuLeft = new SlidingMenuLeft();
            slidingMenuLeft.setCid("0");
            mCid = "0";
        }
		
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void handleInfo(Message msg) {
    }


    @Override
    public void onRetry() {
        LogUtil.e("main ", "reloading");
        reQMode = 0;
        // 刷新必须再次设置listView模式
        setListViewRefreshMode();
        if (isLiveChannel()) {
            requestLiveData();
        } else {
            requestChannelData(getActivity(), cid, ConstantUtils.MAINACTIVITY_REFRESH_AREA);
        }
    }
	
	

    @Override
    protected void requestData(){
        requestChannelData(getActivity(), mCid, ConstantUtils.MAINACTIVITY_REFRESH_AREA);
    }

    PullToRefreshBase.OnRefreshListener2 refreshListener2 = new PullToRefreshBase.OnRefreshListener2<ListView>() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            mXListView.setOnlyShowRefreshingHeader(false);
            reQMode = 0;
            if (!isLiveChannel()) {
                requestChannelData(getActivity(), mCid, ConstantUtils.MAINACTIVITY_REFRESH_AREA);
                //Umeng上拉刷新上报
                MobclickAgent.onEvent(getActivity(), ConstantUtils.FEED_UP_LOAD);
            } else {
                requestLiveData();
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            mXListView.setOnlyShowRefreshingHeader(false);
            reQMode = 1;
            requestChannelData(getActivity(), mCid, ConstantUtils.MAINACTIVITY_LOAD_AREA);
            MobclickAgent.onEvent(getActivity(), ConstantUtils.FEED_DOWN_LOAD);
        }
    };

    /**
     * 根据上啦下拉动作设置 刷新组件文案信息(除直播之外的频道)
     */
    private void setListViewMode() {
        mXListView.setMode(PullToRefreshSwipeListView.Mode.BOTH);
        mXListView.getLoadingLayoutProxy(true, false).setPullLabel(getActivity().getString(R.string.pull_to_refresh_pull_label));
        mXListView.getLoadingLayoutProxy(true, false).setRefreshingLabel(getActivity().getString(R.string.pull_to_refresh_refreshing_label));
        mXListView.getLoadingLayoutProxy(true, false).setReleaseLabel(getActivity().getString(R.string.pull_to_refresh_release_label));
        mXListView.getLoadingLayoutProxy(false, true).setPullLabel(getActivity().getString(R.string.pull_to_refresh_load_more_lable));
        mXListView.getLoadingLayoutProxy(false, true).setReleaseLabel(getActivity().getString(R.string.pull_to_refresh_load_more_release));
        mXListView.getLoadingLayoutProxy(false, true).setRefreshingLabel(getActivity().getString(R.string.pull_to_refresh_load_more_loading));
    }

    /**
     * 直播频道只有下拉刷新，没有上拉加载更多
     */
    private void setLiveListViewMode() {
        mXListView.setMode(PullToRefreshSwipeMenuListView.Mode.PULL_FROM_START);
        mXListView.getLoadingLayoutProxy(true, false).setPullLabel(this.getString(R.string.pull_to_refresh_pull_label));
        mXListView.getLoadingLayoutProxy(true, false).setRefreshingLabel(this.getString(R.string.pull_to_refresh_refreshing_label));
        mXListView.getLoadingLayoutProxy(true, false).setReleaseLabel(this.getString(R.string.pull_to_refresh_release_label));
    }

    /**
     * 请求具体的频道数据
     *
     * @param cid
     */
    public void requestChannelData(Context context, String cid, String area) {
        mCid = cid;
        mArea = area;
        //请求频道页数据
        HttpManager.getInstance().cancelByTag(ConstantUtils.REQUEST_MAINACTIVITY_DATA);
        HttpApi.
                getMainActivityDataRequest(context, cid, area)
                .start(new RequestChannelListener(), ConstantUtils.REQUEST_MAINACTIVITY_DATA);
    }

    @Override
    protected void reuse(){
        slidingMenuLeft = ((ChaoJiShiPinMainActivity)getActivity()).getSlidingMenuLeft();
        if(slidingMenuLeft != null)
            onEventMainThread(slidingMenuLeft);
    }

    public void onEventMainThread(SlidingMenuLeft slidingMenuLeft) {
        max_visible_count = Integer.MIN_VALUE; //必须重新初始化该值，live频道item项与其他频道item项不一样
        alreadyupgvid.clear();
        this.slidingMenuLeft = slidingMenuLeft;
        //精彩推荐
        if ("7".equals(slidingMenuLeft.getContent_type())) {
            if ("0".equals(slidingMenuLeft.getCid())) {
                pageid = "00S002001";
            } else if ("1".equals(slidingMenuLeft.getCid())) {
                pageid = "00S002001_2";
            } else if ("2".equals(slidingMenuLeft.getCid())) {
                pageid = "00S002001_1";
            } else if ("3".equals(slidingMenuLeft.getCid())) {
                pageid = "00S002001_3";
            } else if ("4".equals(slidingMenuLeft.getCid())) {
                pageid = "00S002001_4";
            } else if ("16".equals(slidingMenuLeft.getCid())) {
                pageid = "00S002001_8";
            }
        } else if ("6".equals(slidingMenuLeft.getContent_type())) {
            pageid = "00S002004";
        }
        if (!isLiveChannel()) {
            if (null == mainActivityChannelAdapter)
                mainActivityChannelAdapter = new MainActivityChannelAdapter2(getActivity());
            mXListView.setAdapter(mainActivityChannelAdapter);
        } else {
            if (null == mLiveactivitychanneladapter)
                mLiveactivitychanneladapter = new LiveActivityChannelAdapter(getActivity());
            mXListView.setAdapter(mLiveactivitychanneladapter);
        }
        getNetData(slidingMenuLeft);

    }

    public void getNetData(SlidingMenuLeft slidingMenuLeft) {
        if (ConstantUtils.TOPIC_CONTENT_TYPE.equals(slidingMenuLeft.getContent_type()) || ConstantUtils.RANKLIST_CONTENT_TYPE.equals(slidingMenuLeft.getContent_type())) {
            return;
        }
        String cid = null;
        String title = slidingMenuLeft.getTitle();
        if (ConstantUtils.TITLE_SUGGEST.equals(title)) {
            cid = "0";
        } else {
            cid = slidingMenuLeft.getCid();
        }
        if (mCurrentTitle.equals(title)) {
            reQMode = 3;

        } else {
            reQMode = 4;

            mCurrentTitle = title;
        }
        if (NetWorkUtils.isNetAvailable()) {
            setListViewRefreshMode();
            if (mAlbumLists != null) {
                mAlbumLists.clear();
            }
            if (null != mLiveItemLists)
                mLiveItemLists.clear();
            mXListView.setOnlyShowRefreshingHeader(true);
            mXListView.setRefreshing(true);
            if (isLiveChannel()) {
                // 直播 cid字段为空
                requestLiveData();
            } else {
                requestChannelData(getActivity(), cid, ConstantUtils.MAINACTIVITY_REFRESH_AREA);
            }
        } else {
            showErrorView(mRootView);
            mXListView.setRefreshing(true);
        }
    }
    /*
    *   swipe Menu
    * */

    // step 1. create a MenuCreator
    SwipeMenuCreator creator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {

            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(
                    getActivity());
            // set item background
            deleteItem.setBackground(R.drawable.selector_main_behiend_bg);
            // set item width
            deleteItem.setWidth(Utils.dip2px(75));
            // set a icon
            deleteItem.setIcon(R.drawable.selector_main_delete);
            deleteItem.setTitle(R.string.sarrrs_str_delete);
            deleteItem.setTitleSize(12);
            deleteItem.setTitleColor(Color.WHITE);
            // add to menu
            menu.addMenuItem(deleteItem);


            // create "open" item
            SwipeMenuItem openItem = new SwipeMenuItem(
                    getActivity());
            // set item background
            openItem.setBackground(R.drawable.selector_main_behiend_bg);
            // set item width
            openItem.setIcon(R.drawable.selector_main_collect);
            openItem.setWidth(Utils.dip2px(75));
            // set item title
            openItem.setTitle(R.string.sarrs_str_collect);
            // set item title fontsize
            openItem.setTitleSize(12);
            // set item title font color
            openItem.setTitleColor(Color.WHITE);
            // add to menu
            menu.addMenuItem(openItem);

            // create "delete" item
            SwipeMenuItem shareItem = new SwipeMenuItem(
                    getActivity());
            // set item background
            shareItem.setBackground(R.drawable.selector_main_behiend_bg);
            // set item width
            shareItem.setWidth(Utils.dip2px(75));
            // set a icon
            shareItem.setIcon(R.drawable.selector_main_share);
            shareItem.setTitle(R.string.sarrs_str_share);
            shareItem.setTitleSize(12);
            shareItem.setTitleColor(Color.WHITE);
            // add to menu
            menu.addMenuItem(shareItem);
        }
    };

    @Override
    public void onResume() {
        uploadstat(mXListView.getRefreshableView());
        super.onResume();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_icon:
                MobclickAgent.onEvent(getActivity(), ConstantUtils.SEARCH_BTN);
                buildDrawingCacheAndIntent();
                break;
            default:
                break;
        }
    }


    /**
     * 截屏，保存为Bitmap，提供给SearchAvtivity高斯模糊使用
     *
     * @auth daipei
     */
    public void buildDrawingCacheAndIntent() {
        SearchActivity.launch(getActivity(), pageid);
    }

//    @Override
//    public void onMenuItemClick(int position, SwipeMenu menu, int index) {
//        switch (index) {
//            case 0:
//                //ToastUtil.showShortToast(getActivity(), "click 1");
//                break;
//
//            case 1:
//                //ToastUtil.showShortToast(getActivity(),"click 0");
//                break;
//
//            case 2:
//                // ToastUtil.showShortToast(getActivity(),"click 2");
//                break;
//
//
//        }
//
//
//    }
//
//
//    @Override
//    public void onSwipeStart(int position) {
//        mSwipePosition = position;
//    }
//
//    @Override
//    public void onSwipeEnd(int position) {
//        mSwipePosition = position;
//    }
//
//
//    public int getSwipePosition() {
//        return mSwipePosition;
//    }
//
//    @Override
//    public void onItemClick(int position, View view, int parentId,ListAdapter adapter) {
//        mparentId=parentId-1;
//        // 构造上报参数
//        buidlParam();
//        switch(position){
//            //不喜欢
//            case 0:
//                LogUtil.e("xll", "");
//                mAlbumLists.remove(mparentId);
//                mainActivityChannelAdapter.notifyDataSetChanged();
//                // 负反馈上报
//                DataReporter.reportDislike(id, source, cid, type, token, netType, bucket, seid);
//                break;
//            //收藏
//            case 1:
//                if(UserLoginState.getInstance().isLogin()){
//                    checkSave();
//                   /*  if(adapter!=null&&adapter instanceof HeaderViewListAdapter){
//                         mainActivityChannelAdapter=(MainActivityChannelAdapter2)((HeaderViewListAdapter) adapter).getWrappedAdapter();
//                     }
//*/
//                }else{
//                    startActivity(new Intent(getActivity(), ChaojishipinRegisterActivity.class));
//                }
//                break;
//            //分享
//            case 2:
//                share(parentId);
//                // 分享上报
//                DataReporter.reportAddShare(id, source, cid, type, token, netType, bucket, seid);
//                break;
//        }
//    }

    private class RequestChannelListener implements RequestListener<MainActivityData> {

        @Override
        public void onResponse(MainActivityData result, boolean isCachedData) {
            mXListView.onRefreshComplete();
            hideErrorView(mRootView);
            mTopToast.setVisibility(View.VISIBLE);
            bucket = result.getBucket();
            seid = result.getReid();
            if (null != result) {
                int oldsize = 0;
                if (mAlbumLists != null) {
                    oldsize = mAlbumLists.size();
                }
                ArrayList<MainActivityAlbum> albums = result.getAlbumList();
                if (null != albums && albums.size() > 0) {
                    int albumsSize = albums.size();
                    int beforeSize = mAlbumLists.size();
                    // 上拉刷新
                    if (reQMode == 0) {
                        for (int i = 0; i < albumsSize; i++) {
                            if (!isContainItem(albums.get(i).getId())) {
                                mAlbumLists.add(0, albums.get(albumsSize - 1 - i));
                                MainMenuItem item = new MainMenuItem();
                                item.setIsDelete(false);
                                item.setIsSave(false);
                                item.setIsSare(false);
                                mainActivityChannelAdapter.menuStates.add(0, item);
                            }
                        }
                        //下拉加载
                    } else if (reQMode == 1) {
                        for (int i = 0; i < albumsSize; i++) {
                            if (!isContainItem(albums.get(i).getId())) {
                                int index = mAlbumLists.size();
                                mAlbumLists.add(index, albums.get(i));
                                MainMenuItem item = new MainMenuItem();
                                item.setIsDelete(false);
                                item.setIsSave(false);
                                item.setIsSare(false);
                                mainActivityChannelAdapter.menuStates.add(index, item);

                            }
                        }
                        // 切换频道
                    } else if (reQMode == 3 || reQMode == 4) {
                        mAlbumLists.clear();
//                        mAlbumLists = new ArrayList<MainActivityAlbum>();
                        for (int i = 0; i < albumsSize; i++) {
                            if (!isContainItem(albums.get(i).getId())) {
                                int index = mAlbumLists.size();
                                mAlbumLists.add(index, albums.get(i));
                                MainMenuItem item = new MainMenuItem();
                                item.setIsDelete(false);
                                item.setIsSave(false);
                                item.setIsSare(false);
                                mainActivityChannelAdapter.menuStates.add(index, item);
                            }
                        }
//                        ToastUtil.showShortToast(getActivity(), "切换频道");

                    }
                    int endSize = mAlbumLists.size();


                    if (albumsSize == 0 || beforeSize == endSize) {
                        if(isAdded()){
                            mTopToast.setText(getString(R.string.sarrs_toast_notice_no_result));
                            mTopToast.show(1000);
                        }
                    } else {
                        if (isAdded()) {
                            mTopToast.setText(getString(R.string.sarrs_toast_notice_normal_start) + albumsSize + getString(R.string.sarrs_toast_notice_normal_end));
                            mTopToast.show(1000);
                        }else{
                            mTopToast.setText(getString(R.string.sarrs_toast_notice_no_result) + albumsSize + getString(R.string.sarrs_toast_notice_normal_end));
                            mTopToast.show(1000);
                        }

                    }
                    if (null != mainActivityChannelAdapter) {
                        mainActivityChannelAdapter.setmAlbums(mAlbumLists);
                        mainActivityChannelAdapter.notifyDataSetChanged();
                    }

                    if (reQMode == 1) {
                        mXListView.getRefreshableView().setSelection(oldsize);
                    } else {
                        mXListView.getRefreshableView().setSelection(0);
                    }
                    mXListView.onRefreshComplete();
//                    firstvisiblecount = mXListView.getRefreshableView().getLastVisiblePosition()- mXListView.getRefreshableView().getFirstVisiblePosition()+1;
//                    if(mXListView.isHeadShowed()){
//                        firstvisiblecount = mXListView.getRefreshableView().getLastVisiblePosition()- mXListView.getRefreshableView().getFirstVisiblePosition()+1;
//                    }else{
//                    }
//                    LogUtil.e("wls","firstvisiblecount:"+firstvisiblecount);

                }else{
                    if (reQMode == 3 || reQMode == 4) {
                        mTopToast.setText(getString(R.string.sarrs_toast_notice_nodata));
                        mTopToast.show(1000);
                        if (null != mainActivityChannelAdapter) {
                            mAlbumLists.clear();
                            mainActivityChannelAdapter.setmAlbums(mAlbumLists);
                            mainActivityChannelAdapter.notifyDataSetChanged();
                        }
                    }
                }
//                if (reQMode == 3 || reQMode == 4) {
//                    mXListView.getRefreshableView().smoothScrollToPosition(0);
//                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        uploadstatfirst(mXListView.getRefreshableView());
                    }
                },1000);

            }else {
                if (reQMode == 3 || reQMode == 4) {
                    mTopToast.setText(getString(R.string.sarrs_toast_notice_nodata));
                    mTopToast.show(1000);
                    if (null != mainActivityChannelAdapter) {
                        mAlbumLists.clear();
                        mainActivityChannelAdapter.setmAlbums(mAlbumLists);
                        mainActivityChannelAdapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override
        public void netErr(int errorCode) {
            showErrorView(mRootView);
            mXListView.onRefreshComplete();
            LogUtil.e("error ", " net  error code " + errorCode);

        }

        @Override
        public void dataErr(int errorCode) {
            mXListView.onRefreshComplete();
            mTopToast.setText(getString(R.string.sarrs_toast_notice_no_result));
            mTopToast.show(1000);
            LogUtil.e("error ", " data null error code " + errorCode);
            LogUtil.e("error ", " net  error code " + errorCode);
        }
    }

//    public PullToRefreshSwipeMenuListView getPullSwiteView() {
//        return mXListView;
//    }

    public PullToRefreshListView getPullSwiteView(){
        return mXListView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LogUtil.e("onItemClick", "position0 " + position);
        if (!isLiveChannel()) {
            if (mAlbumLists != null && mAlbumLists.size() > 0) {
                MainActivityAlbum item = mAlbumLists.get(position - 1);
                UploadStat.uploadstat(item, "0", pageid, ChaojishipinSplashActivity.pageid, position + "", "-", "-", "-", "-", "-");
                LogUtil.e("wulianshu", "=====页面点击上报=====");
                VideoDetailItem videoDetailItem = new VideoDetailItem();
                videoDetailItem.setTitle(item.getTitle());
                videoDetailItem.setDescription(item.getDescription());
                videoDetailItem.setId(item.getId());
                videoDetailItem.setCategory_id(item.getCategory_id());
                videoDetailItem.setPlay_count(item.getPlay_count());
                videoDetailItem.setVideoItems(item.getVideos());
                // 视频来源
                videoDetailItem.setBucket(item.getBucket());
                videoDetailItem.setReid(item.getReId());
                videoDetailItem.setSource(item.getSource());
                videoDetailItem.setFromMainContentType(item.getContentType());
                videoDetailItem.setDetailImage(item.getImgage());

                if ("0".equals(ChaoJiShiPinMainActivity.isCheck) || "0".equals(ChaoJiShiPinMainActivity.lasttimeCheck)) {
                    Intent intent = new Intent(getActivity(), ChaoJiShiPinVideoDetailActivity.class);

                    intent.putExtra("videoDetailItem", videoDetailItem);
                    intent.putExtra("ref", pageid);
                    intent.putExtra("seid", seid);
                    //点击上报
                    startActivity(intent);
                } else {
                    Intent webintent = new Intent(getActivity(), PlayActivityFroWebView.class);
                    webintent.putExtra("url", item.getVideos().get(0).getPlay_url());
                    webintent.putExtra("title", item.getVideos().get(0).getTitle());
                    webintent.putExtra("site", item.getSource());
                    webintent.putExtra("videoDetailItem", videoDetailItem);
                    startActivity(webintent);
                }
            }
        } else {
            if (mLiveItemLists != null && mLiveItemLists.size() > 0) {
                LiveDataEntity item = mLiveItemLists.get(position - 1);
                if (null != item) {
                    LogUtil.e(Utils.LIVE_TAG, "channelName is " + item.getChannelName() + " and channelId is " + item.getChannelId());
                    requestLiveStreamData(item);
                }
            }
        }
    }


    private boolean isContainItem(String aid) {
       /* boolean isContainValue = false;
        if (null != mAlbumLists) {
            for (int i = 0; i < mAlbumLists.size(); i++) {
                if (mAlbumLists.get(i).getId().equals(aid)) {
                    isContainValue = true;
                    break;
                }
            }
        }
        return isContainValue;*/
        return false;
    }

    /**
     * 分享
     */
    void share(int position) {
        MainActivityAlbum item = mAlbumLists.get(position - 1);
        ShareDataConfig config = new ShareDataConfig(getActivity());
        ShareData shareData = config.configShareData(item.getId(),
                item.getTitle(),
                item.getImgage(),
                ShareDataConfig.ALBULM_SHARE,
                null);
        ShareDialog shareDialog = new ShareDialog(getActivity(), shareData, null);
        shareDialog.show();
    }

    void buidlParam() {
        token = UserLoginState.getInstance().getUserInfo().getToken();
        MainActivityAlbum item = mAlbumLists.get(mparentId);
        detail = new VideoDetailItem();
        detail.setTitle(item.getTitle());
        detail.setDescription(item.getDescription());
        detail.setId(item.getId());
        detail.setCategory_id(item.getCategory_id());
        detail.setPlay_count(item.getPlay_count());
        detail.setVideoItems(item.getVideos());
        // 视频来源
        detail.setSource(item.getSource());
        detail.setFromMainContentType(item.getContentType());
        detail.setDetailImage(item.getImgage());

        source = detail.getSource() + "";
        cid = detail.getCategory_id();
        if (TextUtils.isEmpty(detail.getId())) {
            id = detail.getVideoItems().get(0).getGvid();
            type = "2";
        } else {
            // 专辑
            id = detail.getId();
            type = "1";
        }
    }
    /**
     * 点击收藏按钮
     */
    void checkSave() {
        HttpManager.getInstance().cancelByTag(ConstantUtils.REQUEST_ISEXISTS_FAVORITE);
        HttpApi.checkFavorite(id, token, type).start(new RequestListener<CheckFavorite>() {
            @Override
            public void onResponse(CheckFavorite result, boolean isCachedData) {
                if (result != null && result.getCode() == 0 && result.isExists()) {
                    cancelSaveOnLine();
                } else {
                    doSaveOnLine();
                    //上报
//                    doReport();
                }
            }

            @Override
            public void netErr(int errorCode) {

            }

            @Override
            public void dataErr(int errorCode) {

            }
        });
    }

    /**
     * 上报 收藏
     */

    void doReport() {
        DataReporter.reportAddCollection(id, source, cid, type, token, netType);

    }

    /**
     * cancel save
     */
    void cancelSaveOnLine() {
        HttpManager.getInstance().cancelByTag(ConstantUtils.REQUEST_CANCEL_FAVORITE);
        HttpApi.cancelFavorite(id, token, type).start(new RequestListener<CancelFavorite>() {
            @Override
            public void onResponse(CancelFavorite result, boolean isCachedData) {
                if (result != null && result.getCode() == 0) {
                    //cancelSaveLocal();

                    if (mainActivityChannelAdapter.menuStates != null) {
                        mainActivityChannelAdapter.menuStates.get(mparentId).setIsDelete(false);
                        mainActivityChannelAdapter.menuStates.get(mparentId).setIsSave(false);
                        mainActivityChannelAdapter.menuStates.get(mparentId).setIsSare(false);
                        mainActivityChannelAdapter.notifyDataSetChanged();

                        LogUtil.e("xll", " main activity cancel " + mparentId);
                    }
                }
            }

            @Override
            public void netErr(int errorCode) {

            }

            @Override
            public void dataErr(int errorCode) {

            }
        });
    }

    /**
     * save onLine
     */
    void doSaveOnLine() {
        if (TextUtils.isEmpty(detail.getId())) {
            id = detail.getVideoItems().get(0).getGvid();
            type = "2";
        } else {
            // 专辑
            id = detail.getId();
            type = "1";
        }

        HttpManager.getInstance().cancelByTag(ConstantUtils.REQUEST_ADD_FAVORITE);
        HttpApi.addFavorite(id, token, type, cid, netType, source, bucket, seid).start(new RequestListener<AddFavorite>() {
            @Override
            public void onResponse(AddFavorite result, boolean isCachedData) {
                if (result != null && result.getCode() == 0) {
                    //doSaveLocal();
                    if (mainActivityChannelAdapter.menuStates != null) {
                        mainActivityChannelAdapter.menuStates.get(mparentId).setIsDelete(false);
                        mainActivityChannelAdapter.menuStates.get(mparentId).setIsSave(true);
                        mainActivityChannelAdapter.menuStates.get(mparentId).setIsSare(false);
                        mainActivityChannelAdapter.notifyDataSetChanged();
                        LogUtil.e("xll", "  main activity  save" + mparentId);

                    }

                }
            }

            @Override
            public void netErr(int errorCode) {

            }

            @Override
            public void dataErr(int errorCode) {

            }
        });
    }

    public void uploadstat(AbsListView absListView) {
        if (mAlbumLists == null || mAlbumLists.size() <= 0) {
            return;
        }
        int beginposition = absListView.getFirstVisiblePosition();
        int endposition = absListView.getLastVisiblePosition();
        if (beginposition <= 1) {
            beginposition = 1;
        }
        if (endposition > mAlbumLists.size()) {
            endposition = mAlbumLists.size();
        }
        String vid = "";
        String aid = "";
        String cid = "";
        for (int j = beginposition - 1; j <= endposition - 1; j++) {
            if (!alreadyupgvid.contains(mAlbumLists.get(j).getVideos().get(0).getGvid()) && j < mAlbumLists.size()) {
                if (j < 0) {
                    j = 0;
                }
                LogUtil.e("wulianshu", "上报的位置:" + j);
                alreadyupgvid.add(mAlbumLists.get(j).getVideos().get(0).getGvid());
                if (TextUtils.isEmpty(mAlbumLists.get(j).getVideos().get(0).getGvid())) {
                    vid += "-" + ",";
                    aid += "-" + ",";
                    cid += "-" + ",";
                } else {
                    vid += mAlbumLists.get(j).getVideos().get(0).getGvid() + ",";
                    aid += mAlbumLists.get(j).getId() + ",";
                    cid += mAlbumLists.get(j).getCategory_id() + ",";
                }
            }
        }
        if (!TextUtils.isEmpty(vid)) {
            vid = vid.substring(0, vid.length() - 1);
            aid = aid.substring(0, aid.length() - 1);
            cid = cid.substring(0, cid.length() - 1);
            MainActivityAlbum mainActivityAlbum = new MainActivityAlbum();
            mainActivityAlbum.setTitle(mAlbumLists.get(beginposition - 1).getTitle());
            mainActivityAlbum.setBucket(mAlbumLists.get(beginposition - 1).getBucket());
            mainActivityAlbum.setId(aid);
            mainActivityAlbum.setCategory_id(cid);
            mainActivityAlbum.setContentType(mAlbumLists.get(beginposition - 1).getContentType());
            mainActivityAlbum.setVideos(mAlbumLists.get(beginposition - 1).getVideos());
            mainActivityAlbum.setReId(mAlbumLists.get(beginposition - 1).getReId());
            UploadStat.uploadstat(mainActivityAlbum, "4", pageid, ChaojishipinSplashActivity.pageid, "-", "-", "-", "-", "-", vid);
        }
    }

    public void uploadstatfirst(AbsListView absListView) {
        int endposition = absListView.getLastVisiblePosition();
        int firstposition = absListView.getFirstVisiblePosition();
        String vid = "";
        String aid = "";
        String cid = "";
        for (int j = firstposition; j <= endposition - 1; j++) {
            if (j < mAlbumLists.size() && !alreadyupgvid.contains(mAlbumLists.get(j).getVideos().get(0).getGvid())) {
                if (j < 0) {
                    j = 0;
                }
                LogUtil.e("wulianshu", "上报的位置first:" + j);
                alreadyupgvid.add(mAlbumLists.get(j).getVideos().get(0).getGvid());
                if (TextUtils.isEmpty(mAlbumLists.get(j).getVideos().get(0).getGvid())) {
                    vid = vid + "-" + ",";
                } else {
                    vid = vid + mAlbumLists.get(j).getVideos().get(0).getGvid() + ",";
                }

                if (TextUtils.isEmpty(mAlbumLists.get(j).getId())) {
                    aid = aid + "-" + ",";
                } else {
                    aid = aid + mAlbumLists.get(j).getId() + ",";
                }
                if (TextUtils.isEmpty(mAlbumLists.get(j).getCategory_id())) {
                    cid = cid + "-" + ",";
                } else {
                    cid = cid + mAlbumLists.get(j).getCategory_id() + ",";
                }
//              UploadStat.uploadstat(mAlbumLists.get(j), "4", "00S002000_2", "00S002000_1", j + "", "-", "-", "-", "-");
            }
        }

        if (vid == null || vid.length() <= 1) {
            return;
        }
        vid = vid.substring(0, vid.length() - 1);
        aid = aid.substring(0, aid.length() - 1);
        cid = cid.substring(0, cid.length() - 1);
        MainActivityAlbum mainActivityAlbum = new MainActivityAlbum();;
        mainActivityAlbum.setTitle(mAlbumLists.get(0).getTitle());
        mainActivityAlbum.setBucket(mAlbumLists.get(0).getBucket());
        mainActivityAlbum.setId(aid);
        mainActivityAlbum.setCategory_id(cid);
        mainActivityAlbum.setContentType(mAlbumLists.get(0).getContentType());
        mainActivityAlbum.setVideos(mAlbumLists.get(0).getVideos());
        mainActivityAlbum.setReId(mAlbumLists.get(0).getReId());

        UploadStat.uploadstat(mainActivityAlbum, "4", pageid, ChaojishipinSplashActivity.pageid, "-", "-", "-", "-", "-", vid);
    }

    /**
     * 请求首页直播数据
     */
    private void requestLiveData() {
        HttpManager.getInstance().cancelByTag(ConstantUtils.REQUEST_LIVE_DATA_TAG);
        HttpApi.
                getLiveChannelDataRequest()
                .start(new RequestLiveListerer(), ConstantUtils.REQUEST_LIVE_DATA_TAG);
    }

    private class RequestLiveListerer implements RequestListener<LiveDataInfo> {
        @Override
        public void onResponse(LiveDataInfo result, boolean isCachedData) {
            LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!!!!!liveDataInfo is " + result);
            mXListView.onRefreshComplete();
            hideErrorView(mRootView);
            mTopToast.setVisibility(View.VISIBLE);
            if (null != result) {
                if (null != mLiveItemLists)
                    mLiveItemLists.clear();
                mLiveItemLists = (ArrayList) result.getRows();
                if (null != mLiveItemLists && mLiveItemLists.size() > 0) {
                    LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!!!!!!!!!!reQMode is " + reQMode);
                    if (reQMode == 0) {
                        //下拉刷新
                    } else if (reQMode == 3 || reQMode == 4) {
                        //切换频道
                    }
//                    if (isAdded()) {
//                        topToast.setText(getString(R.string.sarrs_toast_notice_normal_start) + mLiveItemLists.size() + getString(R.string.sarrs_toast_notice_normal_end));
//                        topToast.show(1000);
//                    }
                    if (null != mLiveactivitychanneladapter) {
                        mLiveactivitychanneladapter.setmLiveItemList(mLiveItemLists);
                        mLiveactivitychanneladapter.notifyDataSetChanged();
                    }
                } else {
//                    topToast.setText(getString(R.string.sarrs_toast_notice_nodata));
//                    topToast.show(1000);
                    if (null != mLiveactivitychanneladapter) {
                        mLiveItemLists.clear();
                        mLiveactivitychanneladapter.setmLiveItemList(mLiveItemLists);
                        mLiveactivitychanneladapter.notifyDataSetChanged();
                    }
                }
            } else {
//                topToast.setText(getString(R.string.sarrs_toast_notice_nodata));
//                topToast.show(1000);
                if (null != mLiveactivitychanneladapter) {
                    mLiveItemLists.clear();
                    mLiveactivitychanneladapter.setmLiveItemList(mLiveItemLists);
                    mLiveactivitychanneladapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void netErr(int errorCode) {
            LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!!!!!requestLiveData netErr and  errorCode is " + errorCode);
            mXListView.onRefreshComplete();
            showErrorView(mRootView);
        }

        @Override
        public void dataErr(int errorCode) {
            LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!!!!!requestLiveData dataErr and  errorCode is " + errorCode);
            mXListView.onRefreshComplete();
            mTopToast.setText(getString(R.string.sarrs_toast_notice_no_result));
            mTopToast.show(1000);
        }
    }

    /**
     * 判断是进入直播频道
     *
     * @return
     */
    private boolean isLiveChannel() {
        if (null != slidingMenuLeft)
            return ConstantUtils.LIVE_CONTENT_TYPE.equals(slidingMenuLeft.getContent_type());
        else {
            slidingMenuLeft = ((ChaoJiShiPinMainActivity) getActivity()).getSlidingMenuLeft();
            if (null != slidingMenuLeft)
                return ConstantUtils.LIVE_CONTENT_TYPE.equals(slidingMenuLeft.getContent_type());
        }
        return false;
    }

    /**
     * 设置listView刷新模式
     */
    private void setListViewRefreshMode() {
        if (!isLiveChannel())
            setListViewMode();
        else
            setLiveListViewMode();
    }

    /**
     * 根据频道id返回流地址
     *
     * @param item
     */
    private void requestLiveStreamData(LiveDataEntity item) {
        LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!!!! requestLiveStreamData called !!!!!!!!!!!!!!");
        HttpManager.getInstance().cancelByTag(ConstantUtils.REQUEST_LIVE_STREAM_DATA_TAG);
        HttpApi.getLiveStreamUrlRequest(item.getChannelId()).start(new RequestLiveStreamListener(item), ConstantUtils.REQUEST_LIVE_STREAM_DATA_TAG);
    }

    private class RequestLiveStreamListener implements RequestListener<LiveStreamInfo> {
        private LiveDataEntity item;

        public RequestLiveStreamListener(LiveDataEntity item) {
            this.item = item;
        }

        @Override
        public void onResponse(LiveStreamInfo result, boolean isCachedData) {
            LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!!!!liveStreamInfo is " + result);
            if (null != result) {
                if (null != result.getRows() && result.getRows().size() > 0) {
                    if (null != mLiveStreamLists)
                        mLiveStreamLists.clear();
                    setLivePlayStreamList(result.getRows());
                    String title = "";
                    if (null != item.getPrograms() && item.getPrograms().size() > 0) {
                        title = item.getPrograms().get(0).getTitle();
                    }
                    excuteLivePlayLogic(item,title, mLivePlayStreams);
                } else {
                    requestStreamDataFail(item);
                }
            } else {
                requestStreamDataFail(item);
            }
        }

        @Override
        public void netErr(int errorCode) {
            LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!requestLiveStreamData netErr and errorCode is " + errorCode);
            requestStreamDataFail(item);
        }

        @Override
        public void dataErr(int errorCode) {
            LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!requestLiveStreamData dataErr and errorCode is " + errorCode);
            requestStreamDataFail(item);
        }
    }

    /**
     * 流地址列表排序(升序)
     *
     * @param list
     */
    private List<LiveStreamEntity> sortLiveStreamList(List<LiveStreamEntity> list) {
        Collections.sort(list, new Comparator<LiveStreamEntity>() {
            @Override
            public int compare(LiveStreamEntity lhs, LiveStreamEntity rhs) {
                return Integer.valueOf(lhs.getRate()) >= Integer.valueOf(rhs.getRate()) ? 1 : -1;
            }
        });
        return list;
    }

    /**
     * 请求直播流地址失败
     *
     * @param item
     */
    private void requestStreamDataFail(LiveDataEntity item) {
        if (null != item) {
            if (null != item.getStreams() && item.getStreams().size() > 0) {
                if (null != mLiveStreamLists)
                    mLiveStreamLists.clear();
                setLivePlayStreamList(item.getStreams());
            } else {
                // 无需处理
            }
            String title = "";
            if (null != item.getPrograms() && item.getPrograms().size() > 0) {
                title = item.getPrograms().get(0).getTitle();
            }
            excuteLivePlayLogic(item,title, mLivePlayStreams);
        } else {
            // 无需处理
        }
    }

    /**
     * 设置直播播放url(忽略掉720P播放地址)
     */
    private void setLivePlayUrl(List<LiveStreamEntity> list) {
        mLiveStreamLists = (ArrayList) sortLiveStreamList(list);
        LogUtil.e(Utils.LIVE_TAG, "###############after sort mLiveStreamLists is " + mLiveStreamLists);
        LiveStreamEntity firstStreamEntity = mLiveStreamLists.get(0);
        if (mLiveStreamLists.size() > 1 && "1800".equalsIgnoreCase(firstStreamEntity.getRate())) {
            mLivePlayUrl = mLiveStreamLists.get(1).getStreamUrl();
            LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!!!!!mLiveStreamName is " + mLiveStreamLists.get(1).getStreamName());
            LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!!!!!mLivePlayUrl is " + mLivePlayUrl);
        } else {
            mLivePlayUrl = firstStreamEntity.getStreamUrl();
            LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!!!!!mLiveStreamName is " + firstStreamEntity.getStreamName());
            LogUtil.e(Utils.LIVE_TAG, "!!!!!!!!!!!!!!!mLivePlayUrl is " + mLivePlayUrl);
        }
    }

    /**
     * 直播流地址排序
     *
     * @param list
     */
    private void setLivePlayStreamList(List<LiveStreamEntity> list) {
        mLiveStreamLists = (ArrayList) sortLiveStreamList(list);
        LogUtil.e(Utils.LIVE_TAG, "###############after sort mLiveStreamLists is " + mLiveStreamLists);
        if (null != mLivePlayStreams)
            mLivePlayStreams.clear();
        for (LiveStreamEntity entity : mLiveStreamLists) {
            mLivePlayStreams.add(0, entity.getStreamUrl());
        }
    }

    /**
     * 直播
     */
    private void excuteLivePlayLogic(LiveDataEntity item,String title, List<String> streams) {
        LivePlayData playData = new LivePlayData();
        playData.setTitle(title);
        playData.setLiveStreams(streams);
        LogUtil.e(Utils.LIVE_TAG, "###############curPlay title is " + title);
        LogUtil.e(Utils.LIVE_TAG, "###############curPlayStreams size is " + streams.size() + " and streams is " + streams);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Utils.LIVE_PLAY_DATA, playData);
        Intent intent = new Intent();
        intent.putExtra("ref",pageid);
        intent.putExtra("livedataentity",item);
        intent.setClass(getActivity(), ChaojishipinLivePlayActivity.class);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }
}
