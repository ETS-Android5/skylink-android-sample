package sg.com.temasys.skylink.sdk.sampleapp.data.service;

import android.content.Context;

import sg.com.temasys.skylink.sdk.rtc.SkylinkConnection;
import sg.com.temasys.skylink.sdk.sampleapp.ConfigFragment.Config;
import sg.com.temasys.skylink.sdk.sampleapp.multipartyvideo.MultiPartyVideoCallContract;

import static sg.com.temasys.skylink.sdk.sampleapp.utils.Utils.toastLog;

/**
 * Created by muoi.pham on 20/07/18.
 */

//public class MultiPartyVideoService extends SDKService implements MultiPartyVideoCallContract.Service, LifeCycleListener, OsListener, MediaListener, RemotePeerListener, RecordingListener,
//        StatsListener {
public class MultiPartyVideoService {

    private final String TAG = MultiPartyVideoService.class.getName();

    private Context mContext;

    //this variable need to be static for configuration change
    private static MultiPartyVideoCallContract.Presenter mPresenter;

    private SdkConnectionManager sdkConnectionManager;

    //this variable need to be static for configuration change
    private static SkylinkConnection skylinkConnection;

    private String ROOM_NAME;
    private String MY_USER_NAME;

    public MultiPartyVideoService(Context mContext) {
        this.mContext = mContext;
        ROOM_NAME = Config.ROOM_NAME_PARTY;
        MY_USER_NAME = Config.USER_NAME_PARTY;
    }

//    @Override
//    public void setPresenter(MultiPartyVideoCallContract.Presenter presenter) {
//        this.mPresenter = presenter;
//    }
//
//    @Override
//    public void connectToRoomServiceHandler() {
//
//    }
//
//    //----------------------------------------------------------------------------------------------
//    // MultiPartyVideoService implementation to work with SDK
//    //----------------------------------------------------------------------------------------------
//
//    public void connectToRoomServiceHandler(String roomName) {
//        sdkConnectionManager = new SdkConnectionManager(mContext);
//        skylinkConnection = sdkConnectionManager.initializeSkylinkConnection(Constants.CONFIG_TYPE.MULTI_PARTY_VIDEO);
//
//        setListeners();
//
//        // Create the Skylink connection string.
//        // In production, the connection string should be generated by an external entity
//        // (such as a secure App server that has the Skylink App Key secret), and sent to the App.
//        // This is to avoid keeping the App Key secret within the application, for better security.
//        String skylinkConnectionString = Utils.getSkylinkConnectionString(
//                roomName, new Date(), SkylinkConnection.DEFAULT_DURATION);
//
//        // The skylinkConnectionString should not be logged in production,
//        // as it contains potentially sensitive information like the Skylink App Key ID.
//
//        boolean connectFailed = !connectToRoomBaseServiceHandler(skylinkConnection, skylinkConnectionString, Config.USER_NAME_PARTY);
//
//        if (connectFailed) {
//            String log = "[SA][Video][connectToRoom] Unable to connect to room!";
//            toastLog(TAG, mContext, log);
//            return;
//        }
//
//        // Initialize and use the Audio router to switch between headphone and headset
//        AudioRouter.startAudioRouting(mContext);
//    }
//
//    public void disconnectFromRoomServiceHandler() {
//        if (skylinkConnection != null && isConnectingOrConnectedBaseServiceHandler(skylinkConnection)) {
//            disconnectFromRoomBaseServiceHandler(skylinkConnection);
//            AudioRouter.stopAudioRouting(mContext);
//        }
//    }
//
//    public String getRoomPeerIdNickServiceHandler() {
//
//        String roomName = Config.ROOM_NAME_PARTY;
//
//        String title = "Room: " + getRoomRoomIdServiceHandler(roomName);
//        // Add PeerId to title if a Peer occupies clicked location.
//        title += "\r\n" + getPeerIdNickBaseServiceHandler(skylinkConnection, getPeerIdBaseServiceHandler(skylinkConnection));
//        return title;
//    }
//
//    @NonNull
//    public String getRoomRoomIdServiceHandler(String roomName) {
//        String roomId = "";
//        if (skylinkConnection != null) {
//            roomId = getRoomIdBaseServiceHandler(skylinkConnection);
//        }
//        return roomName + " (" + roomId + ")";
//    }
//
//    public void getInputVideoResolutionServiceHandler() {
//        getInputVideoResolutionBaseServiceHandler(skylinkConnection);
//    }
//
//    public void switchCameraServiceHandler() {
//        switchCameraBaseServiceHandler(skylinkConnection);
//    }
//
//    public boolean toggleCameraServiceHandler() {
//        return toggleCameraBaseServiceHandler(skylinkConnection);
//    }
//
//    public boolean toggleCameraServiceHandler(boolean isToggle) {
//        return toggleCameraBaseServiceHandler(skylinkConnection, isToggle);
//    }
//
//    public SurfaceViewRenderer getVideoViewServiceHandler(String remotePeerId) {
//        return getVideoViewBaseServiceHandler(skylinkConnection, remotePeerId);
//    }
//
//    public void refreshConnectionServiceHandler(String peerId, boolean iceRestart) {
//        String peer = "all Peers";
//        if (peerId != null) {
//            peer = "Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, peerId);
//        }
//        String log = "Refreshing connection for " + peer;
//        if (iceRestart) {
//            log += " with ICE restart.";
//        } else {
//            log += ".";
//        }
//        toastLog(TAG, mContext, log);
//
//        // Refresh connections and log errors if any.
//        String[] failedPeers = refreshConnectionBaseServiceHandler(skylinkConnection, peerId, iceRestart);
//        if (failedPeers != null) {
//            log = "Unable to refresh ";
//            if ("".equals(failedPeers[0])) {
//                log += "as there is no Peer in the room!";
//            } else {
//                log += "for Peer(s): " + Arrays.toString(failedPeers) + "!";
//            }
//            toastLog(TAG, mContext, log);
//        }
//    }
//
//    public boolean startRecordingServiceHandler() {
//
//        boolean success = startRecordingBaseServiceHandler(skylinkConnection);
//
//        String log = "[SRS][SA] startRecording=" + success +
//                ", isRecording=" + isRecordingBaseServiceHandler(skylinkConnection) + ".";
//        toastLog(TAG, mContext, log);
//
//        return success;
//    }
//
//    public boolean stopRecordingServiceHandler() {
//
//        boolean success = stopRecordingBaseServiceHandler(skylinkConnection);
//
//        String log = "[SRS][SA] stopRecording=" + success +
//                ", isRecording=" + isRecordingBaseServiceHandler(skylinkConnection) + ".";
//        toastLog(TAG, mContext, log);
//        return success;
//    }
//
//    public boolean getTransferSpeedsServiceHandler(String peerId, int mediaDirectionBoth, int mediaAll) {
//        return getTransferSpeedsBaseServiceHandler(skylinkConnection, peerId, mediaDirectionBoth, mediaAll);
//    }
//
//    public boolean getWebrtcStatsServiceHandler(String peerId, int mediaDirectionBoth, int mediaAll) {
//        return getWebrtcStatsBaseServiceHandler(skylinkConnection, peerId, mediaDirectionBoth, mediaAll);
//    }
//
//    public String[] getPeerIdListServiceHandler() {
//        return getPeerIdListBaseServiceHandler(skylinkConnection);
//    }
//
//    public void getSentVideoResolutionServiceHandler(String peerId) {
//        getSentVideoResolutionBaseServiceHandler(skylinkConnection, peerId);
//    }
//
//    public void getReceivedVideoResolutionServiceHandler(String peerId) {
//        getReceivedVideoResolutionBaseServiceHandler(skylinkConnection, peerId);
//    }
//
//    public String getRoomPeerIdNickServiceHandler(String roomName, String peerId) {
//
//        String title = "Room: " + getRoomRoomIdServiceHandler(roomName);
//        // Add PeerId to title if a Peer occupies clicked location.
//        title += "\r\n" + getPeerIdNickBaseServiceHandler(skylinkConnection, peerId);
//        return title;
//    }
//
//    public int getTotalInRoomServiceHandler() {
//        String[] peerIdList = getPeerIdListBaseServiceHandler(skylinkConnection);
//        if (peerIdList == null) {
//            return 0;
//        }
//        // Size of array is number of Peers in room.
//        return peerIdList.length;
//    }
//
//    public int getNumRemotePeersServiceHandler() {
//        int totalInRoom = getTotalInRoomServiceHandler();
//        if (totalInRoom == 0) {
//            return 0;
//        }
//        // The first Peer is the local Peer.
//        return totalInRoom - 1;
//    }
//
//
//    //----------------------------------------------------------------------------------------------
//    // Skylink Listeners
//    //----------------------------------------------------------------------------------------------
//
//    /**
//     * Set listeners to receive callbacks when events are triggered.
//     * SkylinkConnection instance must not be null or listeners cannot be set.
//     * Do not set before {@link SkylinkConnection#init} as that will remove all existing Listeners.
//     *
//     * @return false if listeners could not be set.
//     */
//    private boolean setListeners() {
//        if (skylinkConnection != null) {
//            skylinkConnection.setLifeCycleListener(this);
//            skylinkConnection.setMediaListener(this);
//            skylinkConnection.setOsListener(this);
//            skylinkConnection.setRecordingListener(this);
//            skylinkConnection.setRemotePeerListener(this);
//            skylinkConnection.setStatsListener(this);
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    /***
//     * Lifecycle Listener Callbacks -- triggered during events that happen during the SDK's
//     * lifecycle
//     */
//
//    @Override
//    public void onConnect(boolean isSuccessful, String message) {
//        if (isSuccessful) {
//            String log = "Connected to room " + ROOM_NAME + " (" + getRoomIdBaseServiceHandler(skylinkConnection) +
//                    ") as " + getPeerIdBaseServiceHandler(skylinkConnection) + " (" + MY_USER_NAME + ").";
//            toastLogLong(TAG, mContext, log);
//
//            mPresenter.setPeerListPresenterHandler(getPeerIdListBaseServiceHandler(skylinkConnection)[0]);
//
//        } else {
//            String log = "Skylink failed to connect!\nReason : " + message;
//            toastLogLong(TAG, mContext, log);
//        }
//    }
//
//    @Override
//    public void onDisconnect(int errorCode, String message) {
//        String log = "[onDisconnect] ";
//        if (errorCode == Errors.DISCONNECT_FROM_ROOM) {
//            log += "We have successfully disconnected from the room.";
//        } else if (errorCode == Errors.DISCONNECT_UNEXPECTED_ERROR) {
//            log += "WARNING! We have been unexpectedly disconnected from the room!";
//        }
//        log += " Server message: " + message;
//        toastLogLong(TAG, mContext, log);
//    }
//
//    @Override
//    public void onLockRoomStatusChange(String remotePeerId, boolean lockStatus) {
//        String log = "[SA] Peer " + remotePeerId + " changed Room locked status to "
//                + lockStatus + ".";
//        toastLog(TAG, mContext, log);
//    }
//
//    @Override
//    public void onReceiveLog(int infoCode, String message) {
//        Utils.handleSkylinkReceiveLog(infoCode, message, mContext, TAG);
//    }
//
//    @Override
//    public void onWarning(int errorCode, String message) {
//        Utils.handleSkylinkWarning(errorCode, message, mContext, TAG);
//    }
//
//    /**
//     * Media Listeners Callbacks - triggered when receiving changes to Media Stream from the
//     * remote peer
//     */
//
//    @Override
//    public void onLocalMediaCapture(SurfaceViewRenderer videoView) {
//        if (videoView == null) {
//            return;
//        }
//
//        mPresenter.addSelfViewPresenterHandler(videoView);
//    }
//
//    @Override
//    public void onInputVideoResolutionObtained(int width, int height, int fps, SkylinkCaptureFormat captureFormat) {
//        String log = "[SA][VideoResInput] The current video input has width x height, fps: " +
//                width + " x " + height + ", " + fps + " fps.\r\n";
//        toastLogLong(TAG, mContext, log);
//    }
//
//    @Override
//    public void onReceivedVideoResolutionObtained(String peerId, int width, int height, int fps) {
//        String log = "[SA][VideoResRecv] The current video received from Peer " + peerId +
//                " has width x height, fps: " + width + " x " + height + ", " + fps + " fps.\r\n";
//        toastLogLong(TAG, mContext, log);
//    }
//
//    @Override
//    public void onSentVideoResolutionObtained(String peerId, int width, int height, int fps) {
//        String log = "[SA][VideoResSent] The current video sent to Peer " + peerId +
//                " has width x height, fps: " + width + " x " + height + ", " + fps + " fps.\r\n";
//        toastLogLong(TAG, mContext, log);
//    }
//
//    @Override
//    public void onVideoSizeChange(String peerId, Point size) {
//        String peer = "Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, peerId);
//        // If peerId is null, this call is for our local video.
//        if (peerId == null) {
//            peer = "We've";
//        }
//        Log.d(TAG, peer + " got video size changed to: " + size.toString() + ".");
//    }
//
//    @Override
//    public void onRemotePeerMediaReceive(String remotePeerId, SurfaceViewRenderer videoView) {
//        mPresenter.addRemoteViewPresenterHandler(remotePeerId);
//
//        String log = "Received new ";
//        if (videoView != null) {
//            log += "Video ";
//        } else {
//            log += "Audio ";
//        }
//        log += "from Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId) + ".\r\n";
//
//        UserInfo remotePeerUserInfo = getUserInfoBaseServiceHandler(skylinkConnection, remotePeerId);
//
//        log += "isAudioStereo:" + remotePeerUserInfo.isAudioStereo() + ".\r\n" +
//                "video height:" + remotePeerUserInfo.getVideoHeight() + ".\r\n" +
//                "video width:" + remotePeerUserInfo.getVideoHeight() + ".\r\n" +
//                "video frameRate:" + remotePeerUserInfo.getVideoFps() + ".";
//        toastLog(TAG, mContext, log);
//    }
//
//    @Override
//    public void onRemotePeerAudioToggle(String remotePeerId, boolean isMuted) {
//        String log = "Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId) +
//                " Audio mute status via:\r\nCallback: " + isMuted + ".";
//
//        // It is also possible to get the mute status via the UserInfo.
//        UserInfo userInfo = getUserInfoBaseServiceHandler(skylinkConnection, remotePeerId);
//        if (userInfo != null) {
//            log += "\r\nUserInfo: " + userInfo.isAudioMuted() + ".";
//        }
//        toastLog(TAG, mContext, log);
//    }
//
//    @Override
//    public void onRemotePeerVideoToggle(String remotePeerId, boolean isMuted) {
//        String log = "Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId) +
//                " Video mute status via:\r\nCallback: " + isMuted + ".";
//
//        // It is also possible to get the mute status via the UserInfo.
//        UserInfo userInfo = getUserInfoBaseServiceHandler(skylinkConnection, remotePeerId);
//        if (userInfo != null) {
//            log += "\r\nUserInfo: " + userInfo.isVideoMuted() + ".";
//        }
//        toastLog(TAG, mContext, log);
//    }
//
//    /**
//     * OsListener Callbacks - triggered by Android OS related events.
//     */
//    @Override
//    public void onPermissionRequired(
//            final String[] permissions, final int requestCode, final int infoCode) {
//        // Create a new PermRequesterInfo to represent this request.
//        PermRequesterInfo permRequesterInfo = new PermRequesterInfo(permissions, requestCode, infoCode);
//
//        PermissionUtils.onPermissionRequiredHandler(
//                permRequesterInfo, TAG, mContext, mPresenter.getFragmentPresenterHandler());
//    }
//
//    @Override
//    public void onPermissionGranted(String[] permissions, int requestCode, int infoCode) {
//        PermissionUtils.onPermissionGrantedHandler(permissions, infoCode, TAG);
//    }
//
//    @Override
//    public void onPermissionDenied(String[] permissions, int requestCode, int infoCode) {
//        PermissionUtils.onPermissionDeniedHandler(infoCode, mContext, TAG);
//    }
//
//    /**
//     * Remote Peer Listener Callbacks - triggered during events that happen when data or connection
//     * with remote peer changes
//     */
//
//    @Override
//    public void onRemotePeerJoin(String remotePeerId, Object userData, boolean hasDataChannel) {
//        mPresenter.addRemotePeerPresenterHandler(remotePeerId);
//
//        String log = "Your Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId) + " connected.";
//        toastLog(TAG, mContext, log);
//    }
//
//    @Override
//    public void onRemotePeerLeave(String remotePeerId, String message, UserInfo userInfo) {
//        int numRemotePeers = mPresenter.getNumRemotePeersPresenterHandler();
//        mPresenter.removeRemotePeerPresenterHandler(remotePeerId);
//
//        String log = "Your Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId, userInfo) + " left: " +
//                message + ". " + numRemotePeers + " remote Peer(s) left in the room.";
//        toastLog(TAG, mContext, log);
//    }
//
//    @Override
//    public void onRemotePeerConnectionRefreshed(
//            String remotePeerId, Object userData, boolean hasDataChannel, boolean wasIceRestarted) {
//        String peer = "Skylink Media Relay server";
//        if (remotePeerId != null) {
//            peer = "Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId);
//        }
//        String log = "Your connection with " + peer + " has just been refreshed";
//        if (wasIceRestarted) {
//            log += ", with ICE restarted.";
//        } else {
//            log += ".\r\n";
//        }
//
//        UserInfo remotePeerUserInfo = getUserInfoBaseServiceHandler(skylinkConnection, remotePeerId);
//        log += "isAudioStereo:" + remotePeerUserInfo.isAudioStereo() + ".\r\n" +
//                "video height:" + remotePeerUserInfo.getVideoHeight() + ".\r\n" +
//                "video width:" + remotePeerUserInfo.getVideoHeight() + ".\r\n" +
//                "video frameRate:" + remotePeerUserInfo.getVideoFps() + ".";
//        toastLog(TAG, mContext, log);
//    }
//
//    @Override
//    public void onRemotePeerUserDataReceive(String remotePeerId, Object userData) {
//        // If Peer has no userData, use an empty string for nick.
//        String nick = "";
//        if (userData != null) {
//            nick = userData.toString();
//        }
//        String log = "[SA][onRemotePeerUserDataReceive] Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId) +
//                ":\n" + nick;
//        toastLog(TAG, mContext, log);
//    }
//
//    @Override
//    public void onOpenDataConnection(String remotePeerId) {
//        Log.d(TAG, "onOpenDataConnection for Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, remotePeerId) + ".");
//    }
//
//    /**
//     * Recording Listener Callbacks - triggered during Recording events.
//     */
//
//    @Override
//    public void onRecordingStart(String recordingId) {
//        String log = "[SRS][SA] Recording Started! isRecording=" +
//                isRecordingBaseServiceHandler(skylinkConnection) + ".";
//        toastLogLong(TAG, mContext, log);
//    }
//
//    @Override
//    public void onRecordingStop(String recordingId) {
//        String log = "[SRS][SA] Recording Stopped! isRecording=" +
//                isRecordingBaseServiceHandler(skylinkConnection) + ".";
//        toastLogLong(TAG, mContext, log);
//    }
//
//    @Override
//    public void onRecordingVideoLink(String recordingId, String peerId, String videoLink) {
//        String peer = " Mixin";
//        if (peerId != null) {
//            peer = " Peer " + getPeerIdNickBaseServiceHandler(skylinkConnection, peerId) + "'s";
//        }
//        String msg = "Recording:" + recordingId + peer + " video link:\n" + videoLink;
//
//        // Create a clickable video link.
//        final SpannableString videoLinkClickable = new SpannableString(msg);
//        Linkify.addLinks(videoLinkClickable, Linkify.WEB_URLS);
//
//        // Create TextView for video link.
//        final TextView msgTxtView = new TextView(mContext);
//        msgTxtView.setText(videoLinkClickable);
//        msgTxtView.setMovementMethod(LinkMovementMethod.getInstance());
//
//        // Create AlertDialog to present video link.
//        AlertDialog.Builder videoLinkDialogBuilder = new AlertDialog.Builder(mContext);
//        videoLinkDialogBuilder.setTitle("Recording: " + recordingId + " Video link");
//        videoLinkDialogBuilder.setView(msgTxtView);
//        videoLinkDialogBuilder.setPositiveButton("OK", null);
//        videoLinkDialogBuilder.show();
//        Log.d(TAG, "[SRS][SA] " + msg);
//    }
//
//    @Override
//    public void onRecordingError(String recordingId, int errorCode, String description) {
//        String log = "[SRS][SA] Received Recording error with errorCode:" + errorCode +
//                "! Error: " + description;
//        toastLogLong(TAG, mContext, log);
//        Log.e(TAG, log);
//    }
//
//    /**
//     * Stats Listener Callbacks - triggered during statistics measuring events.
//     */
//
//    @Override
//    public void onWebrtcStatsReceived(final String peerId, int mediaDirection, int mediaType, HashMap<String, String> stats) {
//        // Log the WebRTC stats.
//        StringBuilder log =
//                new StringBuilder("[SA][WStatsRecv] Received for Peer " + peerId + ":\r\n");
//        for (Map.Entry<String, String> entry : stats.entrySet()) {
//            log.append(entry.getKey()).append(": ").append(entry.getValue()).append(".\r\n");
//        }
//        Log.d(TAG, log.toString());
//    }
//
//    @Override
//    public void onTransferSpeedReceived(String peerId, int mediaDirection, int mediaType, double transferSpeed) {
//        String direction = "Send";
//        if (Info.MEDIA_DIRECTION_RECV == mediaDirection) {
//            direction = "Recv";
//        }
//        // Log the transfer speeds.
//        String log = "[SA][TransSpeed] Transfer speed for Peer " + peerId + ": " +
//                Info.getInfoString(mediaType) + " " + direction + " = " + transferSpeed + " kbps";
//        Log.d(TAG, log);
//    }
//
//    public boolean isConnectingOrConnectedServiceHandler() {
//        if (skylinkConnection != null) {
//            return isConnectingOrConnectedBaseServiceHandler(skylinkConnection);
//        }
//        return false;
//    }
}
