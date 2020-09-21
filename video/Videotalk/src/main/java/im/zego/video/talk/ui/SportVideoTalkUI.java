package im.zego.video.talk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.gridlayout.widget.GridLayout;

import im.zego.common.util.SettingDataUtil;
import im.zego.video.talk.R;
import im.zego.video.talk.adapter.MyExpandableListView;
import im.zego.video.talk.databinding.SportTalkBinding;
import im.zego.video.talk.utils.CommonTools;
import im.zego.video.talk.utils.ScreenHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.zego.common.util.AppLogger;
import im.zego.common.widgets.log.FloatingView;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.ZegoMediaPlayer;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoMediaPlayerEventHandler;
import im.zego.zegoexpress.callback.IZegoMediaPlayerLoadResourceCallback;
import im.zego.zegoexpress.constants.ZegoMediaPlayerNetworkEvent;
import im.zego.zegoexpress.constants.ZegoMediaPlayerState;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

public class SportVideoTalkUI extends Activity {
    private static final String TAG = "SportVideoTalkUI";
    private static final int REQUEST = 1;

    private SportTalkBinding binding;
    public static final String mRoomID="VideoTalkRoom-1";
    private ZegoExpressEngine mSDKEngine;
    private String userID;
    private String userName;
    private String mainStreamId;
    private ZegoMediaPlayer mMediaplayer;
    private TextureView textureView;
    private Map<String,TextureView> viewMap;
    private List<String> streamIdList;
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, SportVideoTalkUI.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.sport_talk);
        binding.setActivity(this);
        /** 添加悬浮日志视图 */
        /** Add floating log view */
//        FloatingView.get().add();
        /** 记录SDK版本号 */
        /** Record SDK version */
        AppLogger.getInstance().i("SDK version : %s", ZegoExpressEngine.getVersion());
        initView();
        createEngine();
        loginRoomAndPublishStream();
    }

    private void loginRoomAndPublishStream() {
        String randomSuffix = "student";
        userID = "user" + randomSuffix;
        userName = "userName" + randomSuffix;
        mainStreamId="streamId"+randomSuffix;
        streamIdList.add(mainStreamId);
        viewMap.put(mainStreamId,textureView);
        ZegoRoomConfig config = new ZegoRoomConfig();
        /* 使能用户登录/登出房间通知 */
        /* Enable notification when user login or logout */
        config.isUserStatusNotify = true;
        mSDKEngine.loginRoom(mRoomID, new ZegoUser(userID, userName), config);
        AppLogger.getInstance().i("startPublishStream streamId:"+mainStreamId);
        ZegoCanvas zegoCanvas = new ZegoCanvas(binding.localView);
        zegoCanvas.viewMode = ZegoViewMode.ASPECT_FIT;
        ZegoVideoConfig videoConfig = new ZegoVideoConfig();
        videoConfig.setEncodeResolution(640, 360);
        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
        ZegoExpressEngine.getEngine().setAppOrientation(ZegoOrientation.ORIENTATION_90);
        // 设置预览视图及视图展示模式
//        mSDKEngine.startPreview(zegoCanvas);
        mSDKEngine.startPublishingStream(mainStreamId);
        createLocalMedia();
        playLocalMedia();
    }

    private void createEngine() {
        mSDKEngine = ZegoExpressEngine.createEngine(SettingDataUtil.getAppId(), SettingDataUtil.getAppKey()
                , SettingDataUtil.getEnv(), SettingDataUtil.getScenario(), this.getApplication(), null);
        mSDKEngine.setEventHandler(zegoEventHandler);
        mSDKEngine.enableCamera(true);//打开摄像头
        mSDKEngine.muteMicrophone(false);//打开麦克风
        mSDKEngine.muteSpeaker(false);//开启音频输出
        AppLogger.getInstance().i(getString(R.string.create_zego_engine));
    }
    IZegoEventHandler zegoEventHandler = new IZegoEventHandler() {


        @Override
        public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
            /** 房间状态回调，在登录房间后，当房间状态发生变化（例如房间断开，认证失败等），SDK会通过该接口通知 */
            /** Room status update callback: after logging into the room, when the room connection status changes
             * (such as room disconnection, login authentication failure, etc.), the SDK will notify through the callback
             */
            AppLogger.getInstance().i("onRoomStateUpdate: roomID = " + roomID + ", state = " + state + ", errorCode = " + errorCode);
            if (state == ZegoRoomState.CONNECTED) {
//                binding.roomConnectState.setText(getString(R.string.room_connect));

            } else if (state == ZegoRoomState.DISCONNECTED) {
//                binding.roomConnectState.setText(getString(R.string.room_unconnect));
            }
            if (errorCode != 0) {
                Toast.makeText(SportVideoTalkUI.this, String.format("login room fail, errorCode: %d", errorCode), Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
            AppLogger.getInstance().i("onPlayerStateUpdate: streamID = " + streamID + ", state = " + state + ", errCode = " + errorCode);
        }

        @Override
        public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
            AppLogger.getInstance().i("onPublisherStateUpdate: streamID = " + streamID + ", state = " + state + ", errCode = " + errorCode);
        }

        @Override
        public void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command) {
            AppLogger.getInstance().i("onIMRecvCustomCommand: roomID = " + roomID + "fromUser :"+fromUser+", command= " + command);
            Toast.makeText(SportVideoTalkUI.this, "command1:" + command, Toast.LENGTH_SHORT).show();
            String coachStreamId = "streamId"+CoachVideoTalkUI.randomSuffix;
            if(command.equals(CoachVideoTalkUI.showModel)){
                if(streamIdList.contains(coachStreamId)){
                    destroyLocalMedia();
                    mSDKEngine.startPlayingStream(coachStreamId, new ZegoCanvas(binding.localView));
                }

            }
            if(command.equals(CoachVideoTalkUI.notShowModel)){
                mSDKEngine.stopPlayingStream(coachStreamId);
                createLocalMedia();
                playLocalMedia();
            }
        }

        @Override
        public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {
            super.onRoomStreamUpdate(roomID, updateType, streamList);
            AppLogger.getInstance().i("onRoomStreamUpdate: roomID" + roomID + ", updateType:" + updateType.value() + ", streamList: " + streamList);
            // 这里拉流之后动态添加渲染的View
            if(updateType == ZegoUpdateType.ADD){
                for(ZegoStream zegoStream: streamList){
                    AppLogger.getInstance().i("onRoomStreamUpdate: ZegoUpdateType.ADD streamId:"+zegoStream.streamID);
                    streamIdList.add(zegoStream.streamID);
                    if(!zegoStream.streamID.contains(CoachVideoTalkUI.randomSuffix)){
                        mSDKEngine.startPlayingStream(zegoStream.streamID, new ZegoCanvas(binding.remoteView));
                    }
                }
            }else if(updateType == ZegoUpdateType.DELETE){// callback in UIThread
                for(ZegoStream zegoStream: streamList){
                    AppLogger.getInstance().i("onRoomStreamUpdate:  ZegoUpdateType.DELETE streamId:"+zegoStream.streamID);
                    mSDKEngine.stopPlayingStream(zegoStream.streamID);
                    streamIdList.remove(zegoStream.streamID);

                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoExpressEngine.setEngineConfig(null);
        // 登出房间并释放ZEGO SDK
        //Log out of the room and release the ZEGO SDK
        logoutLiveRoom();
        destroyLocalMedia();
    }
    // 登出房间，去除推拉流回调监听并释放ZEGO SDK
    //Log out of the room, remove the push-pull stream callback listener and release the ZEGO SDK
    public void logoutLiveRoom() {
        mSDKEngine.logoutRoom(mRoomID);
        ZegoExpressEngine.destroyEngine(null);
//        binding.gridLayout.removeView(textureView);
        viewMap.remove(mainStreamId);
        streamIdList.clear();
    }
    private void initView() {
        viewMap=new HashMap<>();
        streamIdList=new ArrayList<>();
        binding.roomId.setText("课程选择");
        binding.roomId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCourseUI();
            }
        });
        binding.roomConnectState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCourseUI();
            }
        });
        resetCourseName();
    }

    private void startCourseUI(){
        Intent intent = new Intent(SportVideoTalkUI.this, CourseMenuUI.class);
        startActivityForResult(intent, REQUEST);
    }

    private void resetCourseName(){
        String courseName = MyExpandableListView.groups[CourseMenuUI.groupId]+":"
                + MyExpandableListView.childs[CourseMenuUI.groupId][CourseMenuUI.childId] + "," + CourseMenuUI.groupId + "_" + CourseMenuUI.childId;
        binding.roomConnectState.setText(courseName);
    }

    public void operateCamera(Boolean isChecked){
        if(mSDKEngine!=null){
            mSDKEngine.enableCamera(isChecked);
        }
    }
    public void operateMic(Boolean isChecked){
        if(mSDKEngine!=null){
            mSDKEngine.muteMicrophone(!isChecked);
        }
    }
    public void operateSpeaker(Boolean isChecked){
        if(mSDKEngine!=null){
            mSDKEngine.muteSpeaker(!isChecked);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        //FloatingView.get().attach(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST && resultCode == CourseMenuUI.RESULT){
            resetCourseName();
            if(mMediaplayer != null){
                playLocalMedia();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //FloatingView.get().detach(this);
    }

    private void createLocalMedia(){
        mMediaplayer = ZegoMediaPlayer.createMediaPlayer();
        mMediaplayer.setEventHandler(new IZegoMediaPlayerEventHandler() {

            @Override
            public void onMediaPlayerNetworkEvent(ZegoMediaPlayer mediaPlayer, ZegoMediaPlayerNetworkEvent networkEvent) {
                AppLogger.getInstance().i("onMediaPlayerNetworkEvent: " + networkEvent);
                Log.d(TAG, "onMediaPlayerNetworkEvent: " + networkEvent);
                Toast.makeText(SportVideoTalkUI.this, "onMediaPlayerNetworkEvent: "+networkEvent, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onMediaPlayerPlayingProgress(ZegoMediaPlayer mediaPlayer, long millisecond) {
                Log.d(TAG, "onMediaPlayerPlayingProgress: millisecond = "+millisecond+", currentResourceTotalDuration = ");

            }

            @Override
            public void onMediaPlayerStateUpdate(ZegoMediaPlayer mediaPlayer, ZegoMediaPlayerState state, int errorCode) {
                AppLogger.getInstance().i("onMediaPlayerStateUpdate: state = " + state + ", errorCode = " + errorCode);
                Log.d(TAG, "onMediaPlayerStateUpdate: state = " + state + ", errorCode = " + errorCode);
                if(errorCode != 0){
                    Toast.makeText(SportVideoTalkUI.this,
                            "onMediaPlayerStateUpdate: state = " + state + ", errorCode = " + errorCode,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void destroyLocalMedia(){
        if(mMediaplayer != null){
            mMediaplayer.destroyMediaPlayer();
            mMediaplayer.setEventHandler(null);
            mMediaplayer = null;
        }
    }

    private void playLocalMedia(){
        mMediaplayer.stop();
        String mp4Path = CourseMenuUI.groupId + "_" + CourseMenuUI.childId + ".mp4";
        mMediaplayer.loadResource(CommonTools.getPath(SportVideoTalkUI.this, mp4Path), new IZegoMediaPlayerLoadResourceCallback() {
            @Override
            public void onLoadResourceCallback(int i) {
                if(i != 0){
                    Log.e(TAG, "onLoadResourceCallback:" + i);
                    AppLogger.getInstance().i("onLoadResourceCallback:" + i);
                    Toast.makeText(SportVideoTalkUI.this, getString(R.string.local_res_error)+i, Toast.LENGTH_LONG).show();
                }
                // 只有在加载成功之后 getTotalDuration 才会返回正常的数值
                //Only after the load is successful, getTotalDuration will return to the normal value

                AppLogger.getInstance().i("currentResourceTotalDuration: " );
                Log.d(TAG, "currentResourceTotalDuration: " );
                Toast.makeText(SportVideoTalkUI.this, "currentResourceTotalDuration: ", Toast.LENGTH_LONG).show();
                mMediaplayer.setPlayerCanvas(new ZegoCanvas(binding.localView));
                mMediaplayer.enableRepeat(true);
                mMediaplayer.start();

            }
        });
    }

}
