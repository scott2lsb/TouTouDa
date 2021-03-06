package com.quanliren.quan_two.activity.user;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.a.me.maxwin.view.XXListView;
import com.a.me.maxwin.view.XXListView.IXListViewListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.quanliren.quan_two.activity.R;
import com.quanliren.quan_two.activity.base.BaseActivity;
import com.quanliren.quan_two.activity.group.DongTaiDetailActivity_;
import com.quanliren.quan_two.activity.group.date.DateDetailActivity_;
import com.quanliren.quan_two.activity.image.ImageBrowserActivity_;
import com.quanliren.quan_two.activity.shop.ShopVipDetail_;
import com.quanliren.quan_two.adapter.MessageAdapter;
import com.quanliren.quan_two.application.AM;
import com.quanliren.quan_two.bean.ChatListBean;
import com.quanliren.quan_two.bean.DateBean;
import com.quanliren.quan_two.bean.DfMessage;
import com.quanliren.quan_two.bean.DfMessage.OtherMessage;
import com.quanliren.quan_two.bean.DongTaiBean;
import com.quanliren.quan_two.bean.DongTaiReplyBean;
import com.quanliren.quan_two.bean.ImageBean;
import com.quanliren.quan_two.bean.MessageListBean;
import com.quanliren.quan_two.bean.User;
import com.quanliren.quan_two.bean.UserTable;
import com.quanliren.quan_two.bean.emoticon.EmoticonActivityListBean.EmoticonZip.EmoticonImageBean;
import com.quanliren.quan_two.bean.emoticon.EmoticonActivityListBean.EmoticonZip.EmoticonJsonBean;
import com.quanliren.quan_two.custom.CustomRelativeLayout;
import com.quanliren.quan_two.custom.CustomRelativeLayout.OnSizeChangedListener;
import com.quanliren.quan_two.custom.emoji.EmoteGridView.EmoticonListener;
import com.quanliren.quan_two.custom.emoji.EmoteView;
import com.quanliren.quan_two.custom.emoji.EmoticonsEditText;
import com.quanliren.quan_two.db.DBHelper;
import com.quanliren.quan_two.fragment.custom.AddPicFragment;
import com.quanliren.quan_two.fragment.message.MyLeaveMessageFragment;
import com.quanliren.quan_two.radio.AmrEncodSender;
import com.quanliren.quan_two.radio.AmrEngine;
import com.quanliren.quan_two.radio.MicRealTimeListener;
import com.quanliren.quan_two.service.SocketManage;
import com.quanliren.quan_two.util.BroadcastUtil;
import com.quanliren.quan_two.util.ImageUtil;
import com.quanliren.quan_two.util.StaticFactory;
import com.quanliren.quan_two.util.URL;
import com.quanliren.quan_two.util.Util;
import com.quanliren.quan_two.util.http.JsonHttpResponseHandler;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.SdkVersionHelper;
import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@EActivity(R.layout.chat)
@OptionsMenu(R.menu.chat_menu)
public class ChatActivity extends BaseActivity implements IXListViewListener,
        OnFocusChangeListener, OnEditorActionListener, OnTouchListener,
        OnSizeChangedListener, SensorEventListener, EmoticonListener {
    private static final String TAG = "ChatActivity";
    public static final String ADDMSG = "com.quanliren.quan_two.ChatActivity.ADDMSG";
    public static final String CHANGESEND = "com.quanliren.quan_two.ChatActivity.CHANGESEND";
    @ViewById
    EmoticonsEditText text;
    @FragmentById(R.id.chat_eiv_inputview)
    EmoteView gridview;
    @ViewById
    View chat_voice_btn, chat_add_btn, chat_radio_panel, chat_face_btn,
            edit_ll, chat_layout_emote, chat_borad_btn, loading, text_ll;
    @ViewById
    Button chat_radio_btn, send_btn;
    @ViewById(R.id.list)
    XXListView listview;
    @ViewById
    ImageView voicesize, delete;
    MessageAdapter adapter;
    @ViewById
    CustomRelativeLayout rlayout;
    @Extra
    User friend;
    User user;
    String maxid = "0";
    RequestParams ap;
    String filename;
    @SystemService
    NotificationManager nm;
    AudioManager audioManager;
    SensorManager mSensorManager;
    Sensor mSensor;

    @OrmLiteDao(helper = DBHelper.class, model = DfMessage.class)
    public Dao<DfMessage, Integer> messageDao;
    @OrmLiteDao(helper = DBHelper.class, model = ChatListBean.class)
    public Dao<ChatListBean, Integer> chatListBeanDao;

    int keyBoardHeight;

    @AfterViews
    void initView() {
        user = getHelper().getUserInfo();
        if(user==null){
            finish();
            return;
        }
        adapter = new MessageAdapter(this, new ArrayList<MessageListBean>(),
                friend, itemHandler);
        listview.setAdapter(adapter);
        listview.setXListViewListener(this);
        listview.setPullLoadEnable(false);

        try {
            UserTable temp = userTableDao.queryForId(friend.getId());
            if (temp != null) {
                friend = temp.getUser();
            } else {
                ac.finalHttp.post(URL.GET_USER_INFO,
                        getAjaxParams("otherid", friend.getId()), callBack);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        getSupportActionBar().setTitle(friend.getNickname());

        text.setOnFocusChangeListener(this);

        text.setOnEditorActionListener(this);

        text.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                chat_layout_emote.setVisibility(View.GONE);
            }
        });

        chat_radio_btn.setOnTouchListener(this);


        listview.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View arg0, MotionEvent arg1) {
                closeInput();
                chat_layout_emote.setVisibility(View.GONE);
                return false;
            }
        });

        gridview.setEditText(text);
        gridview.setListener(this);

        audioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (user != null) {
            Util.setAlarmTime(this, System.currentTimeMillis(),
                    BroadcastUtil.ACTION_CHECKCONNECT,
                    BroadcastUtil.CHECKCONNECT);

            nm.cancel((friend.getId() + friend.getNickname()).hashCode());
        }

        rlayout.setOnSizeChangedListener(this);
        gridview.addMoreEmote();
    }

    @Receiver(actions = {ADDMSG, CHANGESEND})
    public void receiver(Intent i) {
        receiverUI(i);
    }

    @UiThread
    void receiverUI(Intent i) {
        try {
            String action = i.getAction();
            if (action.equals(ADDMSG)) {
                DfMessage bean = (DfMessage) i.getExtras().getSerializable("bean");
                if (bean.getSendUid().equals(friend.getId())) {
                    bean.setIsRead(1);
                    try {
                        messageDao.update(bean);
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent broad = new Intent(MyLeaveMessageFragment.REFEREMSGCOUNT);
                    broad.putExtra("id", friend.getId());
                    sendBroadcast(broad);

                    adapter.addNewsItem(bean);
                    adapter.notifyDataSetChanged();

                    listview.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

                    if (bean.getMsgtype() == 2) {
                        ac.finalHttp.get(bean.getContent(), new fileDownload(bean,
                                StaticFactory.APKCardPathChat
                                        + bean.getContent().hashCode()));
                    }
                    cancelNm();
                }
            } else if (action.equals(CHANGESEND)) {
                DfMessage bean = (DfMessage) i.getExtras().getSerializable("bean");
                List<DfMessage> list = adapter.getList();
                for (DfMessage dfMessage : list) {
                    if (dfMessage.getId() == bean.getId()) {
                        dfMessage.setDownload(bean.getDownload());
                    }
                }
                adapter.notifyDataSetChanged();

                if(i.getExtras().containsKey(SocketManage.TYPE)){
                    int type=i.getIntExtra(SocketManage.TYPE,0);
                    switch (type){
                        case SocketManage.ERROR_TYPE_MORE:
                            showMoreMsg();
                            break;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMoreMsg(){
        new AlertDialog.Builder(this)
                .setMessage("您今天已经向20位陌生人打招呼了，如果想继续与其他陌生人打招呼，请立刻成为会员吧~")
                .setTitle("提示")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                })
                .setPositiveButton("成为会员",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                ShopVipDetail_.intent(ChatActivity.this).start();
                            }
                        }).create().show();
    }

    @UiThread(delay = 1000)
    public void cancelNm() {
        nm.cancel((friend.getId() + friend.getNickname()).hashCode());
    }

    @Click
    void send_btn() {
        if (isFastDoubleClick()) {
            showCustomToast("太快了，休息一下~");
            return;
        }
        String t = Util.FilterEmoji(text.getText().toString());
        if (Util.isStrNotNull(t)) {
            new Thread(new sendTextThread(t)).start();
        } else {
            showCustomToast("请输入内容");
        }
    }

    @AfterTextChange
    void text(Editable text) {
        if (text.toString().trim().length() > 0) {
            send_btn.setVisibility(View.VISIBLE);
            chat_add_btn.setVisibility(View.GONE);
        } else {
            send_btn.setVisibility(View.GONE);
            chat_add_btn.setVisibility(View.VISIBLE);
        }
    }

    JsonHttpResponseHandler callBack = new JsonHttpResponseHandler() {

        public void onSuccess(JSONObject jo) {
            try {
                int status = jo.getInt(URL.STATUS);
                switch (status) {
                    case 0:
                        User temp = new Gson().fromJson(jo.getString(URL.RESPONSE),
                                User.class);
                        if (temp != null) {
                            friend = temp;
                            UserTable dbUser = new UserTable(temp);
                            userTableDao.deleteById(dbUser.getId());
                            userTableDao.create(dbUser);
                            adapter.setFriend(friend);
                            adapter.notifyDataSetChanged();
                            getSupportActionBar().setTitle(friend.getNickname());
                        }
                        break;
                    default:
                        showFailInfo(jo);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ;
    };

    @OptionsItem
    void ziliao() {
        if (friend == null) {
            return;
        }
        AM.getActivityManager().popActivity(UserOtherInfoActivity_.class.getName());
        UserOtherInfoActivity_.intent(this).userId(friend.getId()).start();
    }

    @Override
    public void onRefresh() {
        if (friend == null) {
            return;
        }
        listview.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    int maxid = -1;
                    if (adapter.getList().size() > 0) {
                        maxid = ((DfMessage) (adapter.getList().get(0)))
                                .getId();
                    }

                    QueryBuilder<DfMessage, Integer> qb = messageDao
                            .queryBuilder();
                    Where<DfMessage, Integer> where = qb.where();
                    where.and(
                            where.eq("userid", user.getId()),
                            where.or(where.eq("sendUid", friend.getId()),
                                    where.eq("receiverUid", friend.getId())));
                    if (maxid > -1) {
                        where.and().lt("id", maxid);
                    }
                    qb.limit(15l);
                    qb.orderBy("id", false);

                    final List<DfMessage> list = messageDao.query(qb.prepare());
                    final List<DfMessage> downlist = new ArrayList<DfMessage>();
                    List<Integer> ids = new ArrayList<Integer>();
                    for (DfMessage dfMessage : list) {
                        if (dfMessage.getIsRead() == 0) {
                            ids.add(dfMessage.getId());
                            dfMessage.setIsRead(1);
                        }
                        if (dfMessage.getMsgtype() == 2
                                && (dfMessage.getDownload() == SocketManage.D_nodownload || dfMessage
                                .getDownload() == SocketManage.D_downloading)) {
                            downlist.add(dfMessage);
                        }
                    }
                    if (ids.size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (Integer integer : ids) {
                            sb.append(integer + ",");
                        }
                        sb.deleteCharAt(sb.length() - 1);
                        messageDao.update(messageDao.updateBuilder()
                                .updateColumnValue("isRead", 1).where()
                                .in("id", sb.toString()).prepareUpdate());
                        updateSecondTitle();
                    }
                    if (list.size() > 0) {
                        try {
                            for (int i = list.size() - 1; i >= 0; i--) {
                                if (i < list.size() - 1 && i > 0) {
                                    if (Util.fmtDateTime.parse(
                                            list.get(i).getCtime()).getTime() - 60 * 1000 > Util.fmtDateTime
                                            .parse(list.get(i - 1).getCtime())
                                            .getTime()) {
                                        list.get(i).setShowTime(true);
                                    }
                                } else {
                                    list.get(i).setShowTime(true);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Intent broad = new Intent(
                            MyLeaveMessageFragment.REFEREMSGCOUNT);
                    broad.putExtra("id", friend.getId());
                    sendBroadcast(broad);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            for (DfMessage dfMessage : list) {
                                adapter.addFirstItem(dfMessage);
                            }
                            adapter.notifyDataSetChanged();
                            listview.stop();
                            if (list.size() > 0) {
                                listview.setSelection(adapter.getList()
                                        .indexOf(list.get(0)) + 1);
                            }
                            for (DfMessage dfMessage : downlist) {
                                ac.finalHttp.get(dfMessage.getContent(),
                                        new fileDownload(dfMessage,
                                                StaticFactory.APKCardPathChat
                                                        + dfMessage
                                                        .getContent()
                                                        .hashCode()));
                            }
                        }
                    });
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void updateSecondTitle(){
        try {
            QueryBuilder<DfMessage, Integer> qb = messageDao.queryBuilder();
            Where where = qb.where();
            where.and(where.eq("userid", user.getId()), where.eq("receiverUid", user.getId()), where.eq("sendUid", friend.getId()), where.eq("isRead", 0));
            int count=(int) qb.countOf();
            if(count>0){
                setSubTitle("您还有"+count+"条未读消息，向下拖动即可阅读");
            }else{
                setSubTitle(null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @UiThread
    public void setSubTitle(String str){
        getSupportActionBar().setSubtitle(str);
    }

    @Override
    public void onLoadMore() {
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AddPicFragment.Album:
                if (data == null) {
                    return;
                }
                ContentResolver resolver = getContentResolver();
                Uri imgUri = data.getData();
                try {
                    Cursor cursor = resolver.query(imgUri, null, null, null, null);
                    cursor.moveToFirst();
                    filename = cursor.getString(1);
                    ImageUtil.downsize(
                            filename,
                            filename = StaticFactory.APKCardPath
                                    + new Date().getTime(), this);
                    new sendFile(new File(filename), 1).run();
                    ;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case AddPicFragment.Camera:
                if (filename != null) {
                    File fi = new File(filename);
                    if (fi != null && fi.exists()) {
                        ImageUtil.downsize(filename, filename, this);
                        new sendFile(fi, 1).run();
                    }
                    fi = null;
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    ;

    class sendFile {
        private File file;
        private int msgtype;

        public sendFile(File file, int msgtype) {
            this.file = file;
            this.msgtype = msgtype;
        }

        public void run() {
            try {
                JSONObject msg = DfMessage.getMessage(user, file.getPath(),
                        friend, msgtype, (int) recodeTime);

                JSONObject jo = new JSONObject();
                jo.put(SocketManage.ORDER, SocketManage.ORDER_SENDMESSAGE);
                jo.put(SocketManage.SEND_USER_ID, user.getId());
                jo.put(SocketManage.RECEIVER_USER_ID, friend.getId());
                jo.put(SocketManage.MESSAGE, msg);
                jo.put(SocketManage.MESSAGE_ID,
                        msg.getString(SocketManage.MESSAGE_ID));

                RequestParams ap = getAjaxParams();
                ap.put("file", file);
                ap.put("msgattr", jo.toString());
                ap.put("clientType", "android");

                ac.finalHttp.post(
                        URL.SENDFILE,
                        ap,
                        new sendfileCallBack((DfMessage) new Gson().fromJson(
                                msg.toString(), new TypeToken<DfMessage>() {
                                }.getType())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class sendfileCallBack extends JsonHttpResponseHandler {
        DfMessage msg;

        public sendfileCallBack(DfMessage msg) {
            this.msg = msg;
        }

        @Override
        public void onStart() {
            try {
                msg.setDownload(SocketManage.D_downloading);

                messageDao.create(msg);

                chatListBeanDao.delete(chatListBeanDao.deleteBuilder().where()
                        .eq("userid", user.getId()).and()
                        .eq("friendid", friend.getId()).prepareDelete());
                ChatListBean cb = new ChatListBean(user, msg, friend);
                chatListBeanDao.create(cb);

                Intent broad = new Intent(MyLeaveMessageFragment.ADDMSG);
                broad.putExtra("bean", cb);
                sendBroadcast(broad);

                listview.setSelection(adapter.getCount() - 1);
                listview.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                adapter.addNewsItem(msg);
                adapter.notifyDataSetChanged();
                text.setText("");

                chat_radio_btn.setEnabled(true);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void onSuccess(JSONObject jo) {
            try {
                int status = jo.getInt(URL.STATUS);
                switch (status) {
                    case 0:
                        msg.setDownload(SocketManage.D_downloaded);
                        messageDao.update(msg);
                        adapter.notifyDataSetChanged();
                        break;
                    case 3:
                        fail();
                        showMoreMsg();
                        break;
                    default:
                        fail();
                        showFailInfo(jo);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ;

        @Override
        public void onFailure() {
            fail();
        }

        public void fail() {
            try {
                msg.setDownload(SocketManage.D_destroy);
                messageDao.update(msg);
                adapter.notifyDataSetChanged();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    ;

    public void rightClick(View v) {
        // if (menupop == null) {
        // menupop = new PopFactory(this, new String[] { "加入黑名单", "举报并拉黑" },
        // menuClick, parent);
        // }
        // menupop.toogle();
    }

    ;

    OnClickListener menuClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case 0:
                    new AlertDialog.Builder(ChatActivity.this)
                            .setMessage("您确定要拉黑该用户吗？")
                            .setTitle("提示")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            RequestParams ap = getAjaxParams();
                                            ap.put("otherid", friend.getId());
                                            ac.finalHttp.post(URL.ADDTOBLACK, ap,
                                                    addBlackCallBack);
                                        }
                                    })
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                        }
                                    }).create().show();
                    break;
                case 1:
                    new AlertDialog.Builder(ChatActivity.this)
                            .setMessage("您确定要举报并拉黑该用户吗？")
                            .setTitle("提示")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            // if (menupop1 == null) {
                                            // menupop1 = new PopFactory(
                                            // ChatActivity.this,
                                            // new String[] { "骚扰信息",
                                            // "个人资料不当", "盗用他人资料",
                                            // "垃圾广告", "色情相关" },
                                            // menuClick1, parent);
                                            // }
                                            // menupop1.toogle();
                                        }
                                    })
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                        }
                                    }).create().show();
                    break;
            }
            // menupop.closeMenu();
        }
    };

    JsonHttpResponseHandler addBlackCallBack = new JsonHttpResponseHandler() {
        public void onStart() {
            customShowDialog("正在发送请求");
        }

        ;

        public void onSuccess(JSONObject jo) {
            try {
                int status = jo.getInt(URL.STATUS);
                switch (status) {
                    case 0:
                        showCustomToast("操作成功");
                        break;
                    default:
                        showFailInfo(jo);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                customDismissDialog();
            }
        }

        ;

        public void onFailure() {
            customDismissDialog();
            showIntentErrorToast();
        }

        ;
    };


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        edit_ll.setSelected(hasFocus);
        if (hasFocus) {
            chat_layout_emote.setVisibility(View.GONE);
            text_ll.setBackgroundResource(R.drawable.abc_textfield_search_selected_holo_light);
        } else {
            text_ll.setBackgroundResource(R.drawable.abc_textfield_searchview_holo_light);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            send_btn.performClick();
            return true;
        }
        return false;
    }

    class sendTextThread implements Runnable {
        String t;

        public sendTextThread(String t) {
            this.t = t;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                JSONObject msg = DfMessage.getMessage(user, t, friend, 0,
                        (int) recodeTime);
                final JSONObject jo = new JSONObject();
                jo.put(SocketManage.ORDER, SocketManage.ORDER_SENDMESSAGE);
                jo.put(SocketManage.SEND_USER_ID, user.getId());
                jo.put(SocketManage.RECEIVER_USER_ID, friend.getId());
                jo.put(SocketManage.MESSAGE, msg);
                jo.put(SocketManage.MESSAGE_ID,
                        msg.getString(SocketManage.MESSAGE_ID));

                recodeTime = 0.0f;
                final DfMessage msgs = new Gson().fromJson(msg.toString(),
                        new TypeToken<DfMessage>() {
                        }.getType());
                msgs.setDownload(SocketManage.D_downloading);
                messageDao.create(msgs);

                Util.setAlarmTime(ChatActivity.this, System.currentTimeMillis()
                                + BroadcastUtil.CHECKMESSAGETIME,
                        BroadcastUtil.ACTION_CHECKMESSAGE,
                        BroadcastUtil.CHECKMESSAGETIME);

                chatListBeanDao.delete(chatListBeanDao.deleteBuilder().where()
                        .eq("userid", user.getId()).and()
                        .eq("friendid", friend.getId()).prepareDelete());
                ChatListBean cb = new ChatListBean(user, msgs, friend);
                chatListBeanDao.create(cb);

                Intent broad = new Intent(MyLeaveMessageFragment.ADDMSG);
                broad.putExtra("bean", cb);
                sendBroadcast(broad);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        listview.setSelection(adapter.getCount() - 1);
                        listview.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                        adapter.addNewsItem(msgs);
                        adapter.notifyDataSetChanged();
                        text.setText("");

                        sendMessage(jo.toString());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Handler imgHandle = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (AmrEngine.getSingleEngine().isRecordRunning()) {
                        AmrEngine.getSingleEngine().stopRecording();
                        hideall();
                        voiceValue = 0.0;
                        chat_radio_btn.setText(R.string.normaltalk);
                        chat_radio_btn.setEnabled(false);
                        if (recodeTime < MIX_TIME) {
                            showCustomToast("太短了");
                            File o = new File(filename);
                            if (o.exists()) {
                                o.delete();
                            }
                            new Handler().postDelayed(new Runnable() {

                                public void run() {
                                    hideall();
                                    chat_radio_btn.setEnabled(true);
                                }
                            }, 1000);
                        } else {
                            new sendFile(new File(filename), 2).run();
                        }
                    }
                    break;
                case 1:
                    setDialogImage();
                    break;
                default:
                    break;
            }

        }
    };

    @Click
    public void chat_borad_btn(View v) {
        chat_voice_btn.setVisibility(View.VISIBLE);
        edit_ll.setVisibility(View.VISIBLE);
        text.requestFocus();
        showKeyBoard();
        chat_radio_btn.setVisibility(View.GONE);
        chat_borad_btn.setVisibility(View.GONE);
    }

    @Click
    public void chat_voice_btn(View v) {
        chat_voice_btn.setVisibility(View.GONE);
        edit_ll.setVisibility(View.GONE);
        closeInput();
        text.clearFocus();
        chat_layout_emote.setVisibility(View.GONE);
        chat_radio_btn.setVisibility(View.VISIBLE);
        chat_borad_btn.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!AmrEngine.getSingleEngine().isRecordRunning()) {

                    showVoiceLoading();

                    chat_radio_panel.setVisibility(View.VISIBLE);
                    chat_radio_btn.setText(R.string.pressedtalk);

                    File file = new File(StaticFactory.APKCardPathChat);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    AmrEncodSender sender = new AmrEncodSender(
                            filename = (StaticFactory.APKCardPathChat + String.valueOf((String
                                    .valueOf(new Date().getTime()) + ".amr")
                                    .hashCode())), new MicRealTimeListener() {

                        @Override
                        public void getMicRealTimeSize(double size,
                                                       long time) {
                            voiceValue = size;
                        }
                    });

                    AmrEngine.getSingleEngine().startRecording();
                    new Thread(sender).start();
                    showVoiceStart();
                    mythread();
                }

                break;
            case MotionEvent.ACTION_UP:
                if (AmrEngine.getSingleEngine().isRecordRunning()) {
                    AmrEngine.getSingleEngine().stopRecording();
                    chat_radio_panel.setVisibility(View.GONE);
                    chat_radio_btn.setText(R.string.normaltalk);

                    voiceValue = 0.0;
                    chat_radio_btn.setEnabled(false);
                    if (recodeTime < MIX_TIME) {
                        showCustomToast("太短了");

                        new Handler().postDelayed(new Runnable() {

                            public void run() {
                                hideall();
                                chat_radio_btn.setEnabled(true);
                            }
                        }, 1000);
                    } else {
                        showVoiceLoading();
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                hideall();
                                if (isCanle) {
                                    File o = new File(filename);
                                    if (o.exists()) {
                                        o.delete();
                                    }
                                    chat_radio_btn.setEnabled(true);
                                } else {
                                    new sendFile(new File(filename), 2).run();
                                }
                            }
                        }, 500);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                chat_radio_btn.getLocationOnScreen(location);
                chat_radio_panel.getLocationOnScreen(location1);
                if (event.getRawY() < location[1]) {
                    showVoiceCancle();
                    isCanle = true;
                    if (event.getRawY() <= location1[1]
                            + ImageUtil.dip2px(this, 150)
                            && event.getRawY() >= location1[1]
                            && event.getRawX() <= location1[0]
                            + ImageUtil.dip2px(this, 150)
                            && event.getRawX() >= location1[0]) {
                        delete.setSelected(true);
                    } else {
                        delete.setSelected(false);
                    }
                } else {
                    showVoiceStart();
                    isCanle = false;
                }
                break;
        }
        return false;
    }

    int[] location = new int[2];
    int[] location1 = new int[2];
    boolean isCanle = false;
    private MediaPlayer mediaPlayer;
    // Button player;
    private Thread recordThread;

    private static int MAX_TIME = 60; // 最长录制时间，单位秒，0为无时间限制
    private static int MIX_TIME = 1; // 最短录制时间，单位秒，0为无时间限制，建议设为1

    private static float recodeTime = 0.0f; // 录音的时间
    private static double voiceValue = 0.0; // 麦克风获取的音量值

    private static boolean playState = false; // 播放状态

    // 录音计时线程
    public void mythread() {
        recordThread = new Thread(ImgThread);
        recordThread.start();
    }

    private Runnable ImgThread = new Runnable() {

        public void run() {
            recodeTime = 0.0f;
            while (AmrEngine.getSingleEngine().isRecordRunning()) {
                if (recodeTime >= MAX_TIME && MAX_TIME != 0) {
                    imgHandle.sendEmptyMessage(0);
                } else {
                    try {
                        Thread.sleep(200);
                        recodeTime += 0.2;
                        if (AmrEngine.getSingleEngine().isRecordRunning()) {
                            // voiceValue = mr.getAmplitude();
                            imgHandle.sendEmptyMessage(1);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    // 录音Dialog图片随声音大小切换
    void setDialogImage() {
        /*
		 * if (voiceValue <= 11.0) {
		 * voicesize.setImageResource(R.drawable.hua1); } else if (voiceValue >
		 * 15.0 && voiceValue <= 18.0) {
		 * voicesize.setImageResource(R.drawable.hua2); } else if (voiceValue >
		 * 18.0 && voiceValue <= 21.0) {
		 * voicesize.setImageResource(R.drawable.hua3); } else if (voiceValue >
		 * 21.0 && voiceValue <= 25.0) {
		 * voicesize.setImageResource(R.drawable.hua4); } else if (voiceValue >
		 * 25.0 && voiceValue <= 28.0) {
		 * voicesize.setImageResource(R.drawable.hua5); } else if (voiceValue >
		 * 28.0 && voiceValue <= 31.0) {
		 * voicesize.setImageResource(R.drawable.hua6); } else if (voiceValue >
		 * 31.0 && voiceValue <= 36.0) {
		 * voicesize.setImageResource(R.drawable.hua7); } else if (voiceValue >
		 * 36.0) { voicesize.setImageResource(R.drawable.hua7); }
		 */
        if (voiceValue > 160) {
            voicesize.setImageResource(R.drawable.hua7);
        } else if (voiceValue > 150) {
            voicesize.setImageResource(R.drawable.hua6);
        } else if (voiceValue > 140) {
            voicesize.setImageResource(R.drawable.hua5);
        } else if (voiceValue > 130) {
            voicesize.setImageResource(R.drawable.hua4);
        } else if (voiceValue > 120) {
            voicesize.setImageResource(R.drawable.hua3);
        } else if (voiceValue > 110) {
            voicesize.setImageResource(R.drawable.hua2);
        } else {
            voicesize.setImageResource(R.drawable.hua1);
        }
    }

    public void hideall() {
        chat_radio_panel.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        voicesize.setVisibility(View.VISIBLE);
    }

    public void showVoiceLoading() {
        voicesize.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    public void showVoiceCancle() {
        loading.setVisibility(View.GONE);
        voicesize.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
    }

    public void showVoiceStart() {
        loading.setVisibility(View.GONE);
        voicesize.setVisibility(View.VISIBLE);
        delete.setVisibility(View.GONE);
    }

    @Click
    public void chat_add_btn(View view) {
        closeInput();
        AlertDialog dialog = new AlertDialog.Builder(this).setItems(
                new String[]{"相机", "从相册中选择"},
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        addClick(which);
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void addClick(int which) {
        switch (which) {
            case 0:
                if (Util.existSDcard()) {
                    Intent intent = new Intent(); // 调用照相机
                    String messagepath = StaticFactory.APKCardPathChat;
                    File fa = new File(messagepath);
                    if (!fa.exists()) {
                        fa.mkdirs();
                    }
                    filename = messagepath + new Date().getTime();// 图片路径
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(new File(filename)));
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, AddPicFragment.Camera);
                } else {
                    Toast.makeText(getApplicationContext(), "亲，请检查是否安装存储卡!",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                if (Util.existSDcard()) {
                    Intent intent = new Intent();
                    String messagepath = StaticFactory.APKCardPathChat;
                    File fa = new File(messagepath);
                    if (!fa.exists()) {
                        fa.mkdirs();
                    }
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, AddPicFragment.Album);
                } else {
                    Toast.makeText(getApplicationContext(), "亲，请检查是否安装存储卡!",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void onBackPressed() {
        if (chat_layout_emote.getVisibility() == View.VISIBLE) {
            chat_layout_emote.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Click
    public void chat_face_btn(View v) {
        if (chat_layout_emote.getVisibility() == View.VISIBLE) {
            chat_layout_emote.setVisibility(View.GONE);
            text.requestFocus();
            showKeyBoard();
        } else {
            rlayout.setHideView(chat_layout_emote);
            closeInput();
        }
    }

    Handler handler = new Handler();
    Handler itemHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            AlertDialog dialog;
            switch (msg.what) {
                case 0:
                    startDetail((DfMessage) msg.obj);
                    break;
                case 1:
                    startImage((DfMessage) msg.obj);
                    break;
                case 2:
                    listview.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                    playVoice((DfMessage) msg.obj);
                    break;
                case 3:
                    dialog = new AlertDialog.Builder(ChatActivity.this).setItems(
                            new String[]{"复制文本", "删除消息"},
                            new content_click((DfMessage) msg.obj)).create();
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                    break;
                case 4:
                case 7:
                case 8:
                case 5:
                    dialog = new AlertDialog.Builder(ChatActivity.this).setItems(
                            new String[]{"删除消息"},
                            new img_click((DfMessage) msg.obj)).create();
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                    break;
                case 6:// 消息重发
                    DfMessage dm = (DfMessage) msg.obj;
                    if (dm.getReceiverUid().equals(user.getId())) {
                        reReceiver(dm);
                    } else {
                        reSend(dm);
                    }
                    break;
            }
            super.dispatchMessage(msg);
        }

        ;
    };

    void startDetail(DfMessage bean) {
        OtherMessage msg = bean.getOtherContent();
        DateBean db = new DateBean();
        switch (msg.getInfoType()) {
            case 0:
                DongTaiBean dbean = new DongTaiBean();
                dbean.setDyid(msg.getDyid());
                DongTaiReplyBean rb = new DongTaiReplyBean();
                rb.setId(msg.getDcid());
                rb.setUserid(friend.getId());
                rb.setNickname(friend.getNickname());
                DongTaiDetailActivity_.intent(this).bean(dbean).selectBean(rb)
                        .init(false).start();
                break;
            case 1:
                db.setDtid(Integer.valueOf(msg.getDtid()));
                db.setUserid(user.getId());
                DateDetailActivity_.intent(this).bean(db).start();
                break;
            case 2:
                db.setDtid(Integer.valueOf(msg.getDtid()));
                db.setUserid(friend.getId());
                DateDetailActivity_.intent(this).bean(db).start();
                break;
            case 3:
                DateBean db1 = new DateBean();
                db1.setDtid(Integer.valueOf(msg.getDtid()));
                db1.setUserid(user.getId());
                DongTaiReplyBean rb1 = new DongTaiReplyBean();
                rb1.setId(msg.getDcid());
                rb1.setUserid(friend.getId());
                rb1.setNickname(friend.getNickname());
                DateDetailActivity_.intent(this).bean(db1).selectBean(rb1).start();
                break;
        }
    }

    public void reReceiver(final DfMessage msg) {
        if (msg.getMsgtype() == 2) {
            ac.finalHttp.get(msg.getContent(),
                    new fileDownload(msg, StaticFactory.APKCardPathChat
                            + msg.getContent().hashCode()));
        }
    }

    public void reSend(final DfMessage msg) {
        new AlertDialog.Builder(this).setMessage("您确定要重发这条信息吗？").setTitle("提示")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try {
                            messageDao.delete(msg);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        adapter.removeObj(msg);
                        adapter.notifyDataSetChanged();
                        switch (msg.getMsgtype()) {
                            case 0:
                                new Thread(new sendTextThread(msg.getContent()))
                                        .start();
                                break;
                            case 1:
                                new sendFile(new File(msg.getContent()), 1).run();
                                break;
                            case 2:
                                new sendFile(new File(msg.getContent()), 2).run();
                                break;
                            case 5:
                                new Thread(new sendGifThread(msg.getGifContent()))
                                        .start();
                                break;
                        }
                    }
                }).create().show();
    }

    class content_click implements DialogInterface.OnClickListener {
        DfMessage msg;

        public content_click(DfMessage msg) {
            this.msg = msg;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    copy(msg.getContent());
                    break;
                case 1:
                    deleteMsg(msg);
                    break;
            }
        }
    }

    ;

    public void deleteMsg(DfMessage msg) {
        try {
            messageDao.delete(msg);
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            if (msg.getMsgtype() > 0) {
                File file = new File(msg.getContent());
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter.removeObj(msg);
        adapter.notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void copy(String content) {
        if(SdkVersionHelper.getSdkInt()>=11){
            ClipboardManager c = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            c.setPrimaryClip(ClipData.newPlainText(null, content));
        }else{
            android.text.ClipboardManager c = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            c.setText(content);
        }

        Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show();
    }

    class img_click implements DialogInterface.OnClickListener {
        DfMessage msg;

        public img_click(DfMessage msg) {
            this.msg = msg;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            deleteMsg(msg);
        }
    }

    ;

    DfMessage playingMsg;

    public void playVoice(DfMessage msg) {
        if (playState) {
            stopArm(playingMsg);
            if (playingMsg.getId() != msg.getId()) {
                playArm(msg);
            }
        } else {
            playArm(msg);
        }
    }

    public void playArm(final DfMessage dm) {
        File file = new File(dm.getContent());
        if (!file.exists()) {
            return;
        }

        mediaPlayer = new MediaPlayer();
        try {
            playingMsg = dm;
            mediaPlayer.setDataSource(dm.getContent());
            mediaPlayer.prepare();
            mediaPlayer.start();
            dm.setPlaying(true);
            adapter.notifyDataSetChanged();
            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {
                    playState = false;
                    dm.setPlaying(false);
                    adapter.notifyDataSetChanged();
                    mediaPlayer = null;
                }
            });
            playState = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopArm(DfMessage msg) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (msg != null) {
            msg.setPlaying(false);
            adapter.notifyDataSetChanged();
        }
        playState = false;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        stopArm(null);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        stopArm(null);
        mSensorManager.unregisterListener(this);
    }

    public void startImage(DfMessage msg) {
        List<DfMessage> imgDf = new ArrayList<DfMessage>();
        List<DfMessage> list = adapter.getList();
        for (DfMessage dfMessage : list) {
            if (dfMessage.getMsgtype() == 1) {
                imgDf.add(dfMessage);
            }
        }
        int position = imgDf.indexOf(msg);
        List<ImageBean> beans = new ArrayList<ImageBean>();
        for (DfMessage dfMessage : imgDf) {
            ImageBean ib = new ImageBean();
            ib.imgpath = dfMessage.getContent();
            beans.add(ib);
        }

        ImageBrowserActivity_.intent(this).mPosition(position).mProfile(beans)
                .start();
        ;

    }

    class fileDownload extends FileAsyncHttpResponseHandler {
        DfMessage msg;

        public fileDownload(DfMessage msg, String filePath) {
            super(new File(filePath));
            this.msg = msg;
        }

        public void onStart() {
            File file = new File(StaticFactory.APKCardPathChat);
            if (!file.exists()) {
                file.mkdirs();
            }
            msg.setDownload(SocketManage.D_downloading);
            try {
                messageDao.update(msg);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }

        ;

        @Override
        public void onFailure(int statusCode, Header[] headers,
                              Throwable throwable, File file) {
            // TODO Auto-generated method stub
            msg.setDownload(SocketManage.D_destroy);
            try {
                messageDao.update(msg);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, File file) {
            msg.setDownload(SocketManage.D_downloaded);
            msg.setContent(file.getPath());
            try {
                messageDao.update(msg);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }

        ;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float range = event.values[0];
        if (range >= mSensor.getMaximumRange()) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }

    AtomicBoolean open = new AtomicBoolean(false);

    @Override
    public void open(int height) {
    }

    @Override
    public void close() {

    }

    @Override
    public void onEmoticonClick(EmoticonImageBean bean) {
        if (isFastDoubleClick()) {
            showCustomToast("太快了，休息一下~");
            return;
        }
        new Thread(new sendGifThread(new EmoticonJsonBean(bean.getGifUrl(),
                bean.getFlagName(), bean.getGiffile()))).start();
    }

    @Override
    public void onEmoticonLongPress(EmoticonImageBean bean, int[] xy, int[] wh) {
    }

    @Override
    public void onEmoticonLongPressCancle() {
    }

    ;

    class sendGifThread implements Runnable {
        EmoticonJsonBean bean;

        public sendGifThread(EmoticonJsonBean bean) {
            this.bean = bean;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                Gson gson = new Gson();
                JSONObject msg = DfMessage.getMessage(user,
                        gson.toJson(bean), friend, 5, (int) recodeTime);
                final JSONObject jo = new JSONObject();
                jo.put(SocketManage.ORDER, SocketManage.ORDER_SENDMESSAGE);
                jo.put(SocketManage.SEND_USER_ID, user.getId());
                jo.put(SocketManage.RECEIVER_USER_ID, friend.getId());
                jo.put(SocketManage.MESSAGE, msg);
                jo.put(SocketManage.MESSAGE_ID,
                        msg.getString(SocketManage.MESSAGE_ID));

                recodeTime = 0.0f;
                final DfMessage msgs = new Gson().fromJson(msg.toString(),
                        new TypeToken<DfMessage>() {
                        }.getType());
                msgs.setDownload(SocketManage.D_downloading);
                messageDao.create(msgs);

                Util.setAlarmTime(ChatActivity.this, System.currentTimeMillis()
                                + BroadcastUtil.CHECKMESSAGETIME,
                        BroadcastUtil.ACTION_CHECKMESSAGE,
                        BroadcastUtil.CHECKMESSAGETIME);

                chatListBeanDao.delete(chatListBeanDao.deleteBuilder().where()
                        .eq("userid", user.getId()).and()
                        .eq("friendid", friend.getId()).prepareDelete());
                ChatListBean cb = new ChatListBean(user, msgs, friend);
                chatListBeanDao.create(cb);

                Intent broad = new Intent(MyLeaveMessageFragment.ADDMSG);
                broad.putExtra("bean", cb);
                sendBroadcast(broad);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        listview.setSelection(adapter.getCount() - 1);
                        listview.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                        adapter.addNewsItem(msgs);
                        adapter.notifyDataSetChanged();

                        sendMessage(jo.toString());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Background
    void sendMessage(String str) {
        ac.sendMessage(str);
    }

    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 1500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
