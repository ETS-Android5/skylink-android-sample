package sg.com.temasys.skylink.sdk.sampleapp.data.service;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import org.webrtc.SurfaceViewRenderer;

import java.util.Date;

import sg.com.temasys.skylink.sdk.listener.LifeCycleListener;
import sg.com.temasys.skylink.sdk.listener.MediaListener;
import sg.com.temasys.skylink.sdk.listener.OsListener;
import sg.com.temasys.skylink.sdk.listener.RemotePeerListener;
import sg.com.temasys.skylink.sdk.rtc.Errors;
import sg.com.temasys.skylink.sdk.rtc.SkylinkCaptureFormat;
import sg.com.temasys.skylink.sdk.rtc.SkylinkConnection;
import sg.com.temasys.skylink.sdk.rtc.UserInfo;
import sg.com.temasys.skylink.sdk.sampleapp.data.model.AudioRemotePeer;
import sg.com.temasys.skylink.sdk.sampleapp.data.model.PermRequesterInfor;
import sg.com.temasys.skylink.sdk.sampleapp.utils.AudioRouter;
import sg.com.temasys.skylink.sdk.sampleapp.ConfigFragment.Config;
import sg.com.temasys.skylink.sdk.sampleapp.utils.Utils;
import sg.com.temasys.skylink.sdk.sampleapp.audio.AudioCallContract;
import sg.com.temasys.skylink.sdk.sampleapp.utils.PermissionUtils;

import static sg.com.temasys.skylink.sdk.sampleapp.utils.Utils.toastLog;
import static sg.com.temasys.skylink.sdk.sampleapp.utils.Utils.toastLogLong;

/**
 * Created by muoi.pham on 20/07/18.
 */

public class AudioCallService extends SDKService implements AudioCallContract.Service, LifeCycleListener, MediaListener, OsListener, RemotePeerListener {

    private final String TAG = AudioCallService.class.getName();

    private Context mContext;

    private SdkConnectionManager sdkConnectionManager;

    //these variables need to be static for configuration change
    private static AudioCallContract.Presenter mPresenter;

    private static SkylinkConnection skylinkConnection;

    private static AudioRemotePeer audioRemotePeer;

    public AudioCallService(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void setPresenter(AudioCallContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void connectToRoomServiceHandler() {
        sdkConnectionManager = new SdkConnectionManager(mContext);

        // Initialize the skylink connection using SdkConnectionManager
        skylinkConnection = sdkConnectionManager.initializeSkylinkConnectionForAudioCall();


        setListeners();

        // Create the Skylink connection string.
        // In production, the connection string should be generated by an external entity
        // (such as a secure App server that has the Skylink App Key secret), and sent to the App.
        // This is to avoid keeping the App Key secret within the application, for better security.
        String skylinkConnectionString = Utils.getSkylinkConnectionString(
                Config.ROOM_NAME_AUDIO, new Date(), SkylinkConnection.DEFAULT_DURATION);

        // The skylinkConnectionString should not be logged in production,
        // as it contains potentially sensitive information like the Skylink App Key ID.
        boolean connectFailed = !connectToRoomBaseServiceHandler(skylinkConnection, skylinkConnectionString, Config.USER_NAME_AUDIO);

        if (connectFailed) {
            String log = "Unable to connect to room!";
            toastLog(TAG, mContext, log);
            return;
        } else {
            String log = "Connecting...";
            toastLog(TAG, mContext, log);
        }

        // Initialize and use the Audio router to switch between headphone and headset
        AudioRouter.startAudioRouting(mContext);
    }

    @Override
    public void disconnectFromRoomServiceHandler() {
        if (skylinkConnection != null) {
            disconnectFromRoomBaseServiceHandler(skylinkConnection);
            AudioRouter.stopAudioRouting(mContext);
        }
    }

    @Override
    public boolean isConnectingOrConnectedServiceHandler() {
        if (skylinkConnection != null) {
            return isConnectingOrConnectedBaseServiceHandler(skylinkConnection);
        }
        return false;
    }

    public String getRoomDetailsServiceHandler() {
        boolean isConnected = isConnectingOrConnectedServiceHandler();
        String roomName = getRoomNameBaseServiceHandler(skylinkConnection, Config.ROOM_NAME_AUDIO);
        String userName = getUserNameBaseServiceHandler(skylinkConnection, null, Config.USER_NAME_AUDIO);

        boolean isPeerJoined = audioRemotePeer == null ? false : audioRemotePeer.isPeerJoined();

        String roomDetails = "You are not connected to any room";

        if (isConnected) {
            roomDetails = "Now connected to Room named : " + roomName
                    + "\n\nYou are signed in as : " + userName + "\n";
            if (isPeerJoined) {
                roomDetails += "\nPeer(s) are in the room";
                roomDetails += "\n" + audioRemotePeer.getRemotePeerName();
            } else {
                roomDetails += "\nYou are alone in this room";
            }
        }

        return roomDetails;
    }

    public int getNumRemotePeersServiceHandler() {
        int totalInRoom = getTotalInRoomServiceHandler();
        if (totalInRoom == 0) {
            return 0;
        }
        // The first Peer is the local Peer.
        return totalInRoom - 1;
    }

    public int getTotalInRoomServiceHandler() {
        String[] peerIdList = getPeerIdListBaseServiceHandler(skylinkConnection);
        if (peerIdList == null) {
            return 0;
        }
        // Size of array is number of Peers in room.
        return peerIdList.length;
    }


    //----------------------------------------------------------------------------------------------
    // Skylink Listeners
    //----------------------------------------------------------------------------------------------

    /**
     * Set listeners to receive callbacks when events are triggered.
     * SkylinkConnection instance must not be null or listeners cannot be set.
     * Do not set before {@link SkylinkConnection#init} as that will remove all existing Listeners.
     *
     * @return false if listeners could not be set.
     */
    private boolean setListeners() {
        if (skylinkConnection != null) {
            skylinkConnection.setLifeCycleListener(this);
            skylinkConnection.setMediaListener(this);
            skylinkConnection.setOsListener(this);
            skylinkConnection.setRemotePeerListener(this);
            return true;
        } else {
            return false;
        }
    }

    /***
     * Lifecycle Listener Callbacks -- triggered during events that happen during the SDK's
     * lifecycle
     */

    /**
     * Triggered when connection is successful
     *
     * @param isSuccessful
     * @param message
     */

    @Override
    public void onConnect(boolean isSuccessful, String message) {
        if (isSuccessful) {
            String log = "Connected to room " + Config.ROOM_NAME_AUDIO + " (" + getRoomIdBaseServiceHandler(skylinkConnection) +
                    ") as " + getPeerIdBaseServiceHandler(skylinkConnection) + " (" + Config.USER_NAME_AUDIO + ").";
            toastLogLong(TAG, mContext, log);

        } else {
            String log = "Skylink failed to connect!\nReason : " + message;
            toastLogLong(TAG, mContext, log);
        }

        mPresenter.setRoomDetailsPresenterHandler(getRoomDetailsServiceHandler());
    }

    @Override
    public void onDisconnect(int errorCode, String message) {

        String log = "[onDisconnect] ";
        if (errorCode == Errors.DISCONNECT_FROM_ROOM) {
            log += "We have successfully disconnected from the room.";
        } else if (errorCode == Errors.DISCONNECT_UNEXPECTED_ERROR) {
            log += "WARNING! We have been unexpectedly disconnected from the room!";
        }
        log += " Server message: " + message;
        toastLogLong(TAG, mContext, log);

        mPresenter.setRoomDetailsPresenterHandler(getRoomDetailsServiceHandler());

    }

    @Override
    public void onLockRoomStatusChange(String remotePeerId, boolean lockStatus) {
        String log = "[SA] Peer " + remotePeerId + " changed Room locked status to "
                + lockStatus + ".";
        toastLog(TAG, mContext, log);
    }

    @Override
    public void onReceiveLog(int infoCode, String message) {
        Utils.handleSkylinkReceiveLog(infoCode, message, mContext, TAG);
    }

    @Override
    public void onWarning(int errorCode, String message) {
        Utils.handleSkylinkWarning(errorCode, message, mContext, TAG);
    }

    /**
     * Media Listeners Callbacks - triggered when receiving changes to Media Stream from the
     * remote peer
     */

    @Override
    public void onLocalMediaCapture(SurfaceViewRenderer surfaceView) {
        Log.d(TAG, "onLocalMediaCapture");
    }

    @Override
    public void onInputVideoResolutionObtained(int width, int height, int fps, SkylinkCaptureFormat captureFormat) {
        // Will not be called in Audio only client.
    }

    @Override
    public void onReceivedVideoResolutionObtained(String peerId, int width, int height, int fps) {
        // Will not be called in Audio only client.
    }

    @Override
    public void onSentVideoResolutionObtained(String peerId, int width, int height, int fps) {
        // Will not be called in Audio only client.
    }

    @Override
    public void onVideoSizeChange(String peerId, Point size) {
        // Will not be called in Audio only client.
    }

    @Override
    public void onRemotePeerMediaReceive(String remotePeerId, SurfaceViewRenderer videoView) {
        String log = "Received new ";
        if (videoView != null) {
            log += "Video ";
        } else {
            log += "Audio ";
        }
        log += "from Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId) + ".\r\n";

        UserInfo remotePeerUserInfo = getUserInfoBaseServiceHandler(skylinkConnection, remotePeerId);
        log += "isAudioStereo:" + remotePeerUserInfo.isAudioStereo() + ".";
        toastLog(TAG, mContext, log);
    }

    @Override
    public void onRemotePeerAudioToggle(String remotePeerId, boolean isMuted) {
        String log = "Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId) +
                " Audio mute status via:\r\nCallback: " + isMuted + ".";

        // It is also possible to get the mute status via the UserInfo.
        UserInfo userInfo = getUserInfoBaseServiceHandler(skylinkConnection, remotePeerId);
        if (userInfo != null) {
            log += "\r\nUserInfo: " + userInfo.isAudioMuted() + ".";
        }
        toastLog(TAG, mContext, log);
    }

    @Override
    public void onRemotePeerVideoToggle(String remotePeerId, boolean isMuted) {
        String log = "Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId) +
                " Video mute status via:\r\nCallback: " + isMuted + ".";

        // It is also possible to get the mute status via the UserInfo.
        UserInfo userInfo = getUserInfoBaseServiceHandler(skylinkConnection, remotePeerId);
        if (userInfo != null) {
            log += "\r\nUserInfo: " + userInfo.isVideoMuted() + ".";
        }
        toastLog(TAG, mContext, log);
    }

    /**
     * OsListener Callbacks - triggered by Android OS related events.
     */
    @Override
    public void onPermissionRequired(
            final String[] permissions, final int requestCode, final int infoCode) {
        PermRequesterInfor infor = new PermRequesterInfor(permissions, requestCode, infoCode);
        PermissionUtils.onPermissionRequiredHandler(infor, TAG, mContext, mPresenter.getFragmentPresenterHandler());
    }

    @Override
    public void onPermissionGranted(String[] permissions, int requestCode, int infoCode) {
        Utils.onPermissionGrantedHandler(permissions, infoCode, TAG);
    }

    @Override
    public void onPermissionDenied(String[] permissions, int requestCode, int infoCode) {
        Utils.onPermissionDeniedHandler(infoCode, mContext, TAG);
    }

    /**
     * Remote Peer Listener Callbacks - triggered during events that happen when data or connection
     * with remote peer changes
     */

    @Override
    public void onRemotePeerJoin(String remotePeerId, Object userData, boolean hasDataChannel) {
        // When remote peer joins room, keep track of user and update text-view to display details
        String remotePeerName = null;
        if (userData instanceof String) {
            remotePeerName = (String) userData;
        }

        audioRemotePeer = new AudioRemotePeer(true, remotePeerId, remotePeerName);

        mPresenter.setRoomDetailsPresenterHandler(getRoomDetailsServiceHandler());

        String log = "Your Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId) + " connected.";
        toastLog(TAG, mContext, log);
    }

    @Override
    public void onRemotePeerLeave(String remotePeerId, String message, UserInfo userInfo) {
        //reset audioRemotePeer
        audioRemotePeer = null;

        mPresenter.setRoomDetailsPresenterHandler(getRoomDetailsServiceHandler());

        int numRemotePeers = getNumRemotePeersServiceHandler();

        String log = "Your Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId, userInfo) + " left: " +
                message + ". " + numRemotePeers + " remote Peer(s) left in the room.";
        toastLog(TAG, mContext, log);
    }

    @Override
    public void onRemotePeerConnectionRefreshed(String remotePeerId, Object userData,
                                                boolean hasDataChannel, boolean wasIceRestarted) {
        String peer = "Skylink Media Relay server";
        if (remotePeerId != null) {
            peer = "Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId);
        }
        String log = "Your connection with " + peer + " has just been refreshed";
        if (wasIceRestarted) {
            log += ", with ICE restarted.";
        } else {
            log += ".\r\n";
        }

        UserInfo remotePeerUserInfo = getUserInfoBaseServiceHandler(skylinkConnection, remotePeerId);
        log += "isAudioStereo:" + remotePeerUserInfo.isAudioStereo() + ".";
        toastLog(TAG, mContext, log);
    }

    @Override
    public void onRemotePeerUserDataReceive(String s, Object userData) {
        // If Peer has no userData, use an empty string for nick.
        String nick = "";
        if (userData != null) {
            nick = userData.toString();
        }
        String log = "[SA][onRemotePeerUserDataReceive] Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, audioRemotePeer.getRemotePeerId()) +
                ":\n" + nick;
        toastLog(TAG, mContext, log);
    }

    @Override
    public void onOpenDataConnection(String s) {
        Log.d(TAG, "onOpenDataConnection");
    }


}
