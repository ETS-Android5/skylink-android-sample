package sg.com.temasys.skylink.sdk.sampleapp.multipartyvideo;

import android.content.Intent;
import android.support.v4.app.Fragment;

import org.webrtc.SurfaceViewRenderer;

import java.util.List;

import sg.com.temasys.skylink.sdk.rtc.SkylinkConfig;
import sg.com.temasys.skylink.sdk.rtc.SkylinkMedia;
import sg.com.temasys.skylink.sdk.sampleapp.BaseService;
import sg.com.temasys.skylink.sdk.sampleapp.BaseView;
import sg.com.temasys.skylink.sdk.sampleapp.service.model.SkylinkPeer;

/**
 * Created by muoi.pham on 20/07/18.
 * This interface is responsible for specify behaviors of View, Presenter, Service
 */

public interface MultiPartyVideoCallContract {

    interface View extends BaseView<Presenter> {

        /**
         * Update UI into connected state
         */
        void onPresenterRequestUpdateUIConnected(String roomId);

        /**
         * Update UI into disconnected state
         */
        void onPresenterRequestUpdateUIDisconnected();

        /**
         * Update UI when remote peer join the room
         */
        void onPresenterRequestChangeUiRemotePeerJoin(SkylinkPeer newPeer, int index);

        /**
         * Update UI details when peers are in room
         */
        void onPresenterRequestChangeUIRemotePeerLeft(int peerIndex, List<SkylinkPeer> peersList);

        /**
         * Update UI details when need to add local video view
         */
        void onPresenterRequestAddSelfView(SurfaceViewRenderer videoView, SkylinkMedia.MediaType mediaType);

        /**
         * Update UI details when need to add remote video view
         */
        void onPresenterRequestAddRemoteView(int peerIndex, SkylinkMedia.MediaType mediaType, SurfaceViewRenderer remoteView);

        /**
         * Update UI details when need to remove remote video view
         */
        void onPresenterRequestRemoveRemotePeer(int viewIndex);

        /**
         * Get instance of the fragment for processing runtime permission
         */
        Fragment onPresenterRequestGetFragmentInstance();

        /**
         * Show or hide button stop screen sharing on UI
         */
        void onPresenterRequestShowButtonStopScreenSharing();

        void onPresenterRequestChangeDefaultVideoDevice(SkylinkConfig.VideoDevice videoDevice);
    }

    interface Presenter {

        /**
         * process runtime audio/camera permission results
         */
        void onViewRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

        /**
         * process resuming the app state when view resumed
         */
        void onViewRequestResume();

        /**
         * process pausing the app state when view paused
         */
        void onViewRequestPause();

        /**
         * process disconnecting/closing the app when view exited
         */
        void onViewRequestExit();

        /**
         * process switching camera to front/back camera
         */
        void onViewRequestSwitchCamera();

        /**
         * process starting recording function
         */
        void onViewRequestStartRecording();

        /**
         * process stopping recording function
         */
        void onViewRequestStopRecording();

        /**
         * process get info about roomId and nickName of peer from SDK
         */
        String onViewRequestGetRoomIdAndNickname();

        /**
         * process get info about local input video resolution from SDK
         */
        void onViewRequestGetInputVideoResolution();

        /**
         * process get info about local sent video resolution from SDK
         */
        void onViewRequestGetSentVideoResolution(int peerIndex);

        /**
         * process get info about remote received video resolution from SDK
         */
        void onViewRequestGetReceivedVideoResolution(int peerIndex);

        /**
         * process toggling stats from WebRTC lib
         */
        void onViewRequestWebrtcStatsToggle(int peerIndex);

        /**
         * process getting info about transfer speed through network
         */
        void onViewRequestGetTransferSpeeds(int peerIndex, SkylinkMedia.MediaType mediaType, boolean forSending);

        /**
         * process refresh the connection to remote peer
         */
        void onViewRequestRefreshConnection(int peerIndex, boolean iceRestart);

        /**
         * process getting total number of peers in the current room
         */
        int onViewRequestGetTotalInRoom();

        /**
         * process getting remote video view from specific peer
         */
        List<SurfaceViewRenderer> onViewRequestGetVideoViewByIndex(int index);

        /**
         * process data to display on view at initiative connection
         */
        void onViewRequestConnectedLayout();

        /**
         * Get specific peer at index
         */
        SkylinkPeer onViewRequestGetPeerByIndex(int index);

        /**
         * Get the stats state of specific remote video view
         */
        Boolean onViewRequestGetWebRtcStatsState(int peerIndex);

        /**
         * Start local audio
         */
        void onViewRequestStartAudio();

        /**
         * Start local video base on the default video device setting
         */
        void onViewRequestStartVideo();

        void onViewRequestStartVideoCustom();

        /**
         * Start local video from the camera base on the default video device setting for camera (front/back)
         */
        void onViewRequestStartVideoCamera();

        /**
         * Start local screen video view
         */
        void onViewRequestStartVideoScreen();

        /**
         * Start second video view
         */
        void onViewRequestStartSecondVideoView();

        /**
         * process the permission for screen sharing
         */
        void onViewRequestActivityResult(int requestCode, int resultCode, Intent data);

        void onViewRequestStartLocalMediaIfConfigAllow();

        void onViewRequestStopScreenSharing();
    }

    interface Service extends BaseService<Presenter> {


    }
}

