package im.zego.video.talk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import im.zego.common.util.SettingDataUtil;
import im.zego.video.talk.R;
import im.zego.video.talk.databinding.CoachTalkBinding;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.zego.common.util.AppLogger;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.ZegoMediaPlayer;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoIMSendCustomCommandCallback;
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

public class CoachVideoTalkUI extends Activity {
    public static final String showModel = "showModel";
    public static final String notShowModel = "notShowModel";

    private static final String TAG = "CoachVideoTalkUI";
    private static final String userID = "userIdCoach";
    private static final String userName = "userNameCoach";
    private static final String mainStreamId = "mainStreamIdCoach";

    private CoachTalkBinding binding;
    public static final String mRoomID="VideoTalkRoom-1";
    private ZegoExpressEngine mSDKEngine;

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, CoachVideoTalkUI.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.coach_talk);
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
        ZegoRoomConfig config = new ZegoRoomConfig();
        /* 使能用户登录/登出房间通知 */
        /* Enable notification when user login or logout */
        config.isUserStatusNotify = true;
        mSDKEngine.loginRoom(mRoomID, new ZegoUser(userID, userName), config);
        AppLogger.getInstance().i("startPublishStream streamId:"+mainStreamId);
//        ZegoCanvas zegoCanvas = new ZegoCanvas(binding.localView);
//        zegoCanvas.viewMode = ZegoViewMode.ASPECT_FIT;
        ZegoVideoConfig videoConfig = new ZegoVideoConfig();
        videoConfig.setEncodeResolution(640, 360);
        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
        ZegoExpressEngine.getEngine().setAppOrientation(ZegoOrientation.ORIENTATION_90);
        // 设置预览视图及视图展示模式
//        mSDKEngine.startPreview(zegoCanvas);
//        mSDKEngine.startPublishingStream(mainStreamId);

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
                Toast.makeText(CoachVideoTalkUI.this, String.format("login room fail, errorCode: %d", errorCode), Toast.LENGTH_LONG).show();
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
        public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {
            super.onRoomStreamUpdate(roomID, updateType, streamList);
            AppLogger.getInstance().i("onRoomStreamUpdate: roomID" + roomID + ", updateType:" + updateType.value() + ", streamList: " + streamList);
            // 这里拉流之后动态添加渲染的View
            if(updateType == ZegoUpdateType.ADD){
                for(ZegoStream zegoStream: streamList){
                    AppLogger.getInstance().i("onRoomStreamUpdate: ZegoUpdateType.ADD streamId:"+zegoStream.streamID);
                    if(zegoStream.streamID.equals(CoachShootUI.mainStreamId)){
                        mSDKEngine.startPlayingStream(zegoStream.streamID, new ZegoCanvas(binding.remoteView));
                    }else if(zegoStream.streamID.equals(SportShootUI.mainStreamId)){
                        mSDKEngine.startPlayingStream(zegoStream.streamID, new ZegoCanvas(binding.localView));
                    }
                }
            }else if(updateType == ZegoUpdateType.DELETE){// callback in UIThread
                for(ZegoStream zegoStream: streamList){
                    AppLogger.getInstance().i("onRoomStreamUpdate:  ZegoUpdateType.DELETE streamId:"+zegoStream.streamID);
                    mSDKEngine.stopPlayingStream(zegoStream.streamID);
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
    }
    // 登出房间，去除推拉流回调监听并释放ZEGO SDK
    //Log out of the room, remove the push-pull stream callback listener and release the ZEGO SDK
    public void logoutLiveRoom() {
        mSDKEngine.logoutRoom(mRoomID);
        ZegoExpressEngine.destroyEngine(null);
    }
    private void initView() {
        binding.roomId.setText("教练教学端");
        binding.roomConnectState.setText("");
    }

    public void showModelState(Boolean isChecked){
        if(mSDKEngine==null){
            return;
        }
        final String msg = isChecked ? showModel : notShowModel;
        ArrayList<ZegoUser> userList = new ArrayList<>();
        //指定要接收的对象
        ZegoUser user = new ZegoUser(SportVideoTalkUI.userID, SportVideoTalkUI.userName);
        userList.add(user);
        if (!msg.equals("")) {
            mSDKEngine.sendCustomCommand(mRoomID, msg  ,userList, new IZegoIMSendCustomCommandCallback() {
                /** 发送用户自定义消息结果回调处理 */
                /** Send custom command result callback processing */
                @Override
                public void onIMSendCustomCommandResult(int errorCode) {
                    if (errorCode == 0) {
                        AppLogger.getInstance().i("send custom message success");
                        Toast.makeText(CoachVideoTalkUI.this, getString(R.string.tx_im_send_cc_ok), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        AppLogger.getInstance().i("send custom message fail");
                        Toast.makeText(CoachVideoTalkUI.this, getString(R.string.tx_im_send_cc_fail) + errorCode, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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
//        FloatingView.get().attach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        FloatingView.get().detach(this);
    }

}

