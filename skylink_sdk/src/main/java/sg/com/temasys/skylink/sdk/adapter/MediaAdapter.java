package sg.com.temasys.skylink.sdk.adapter;

import android.graphics.Point;
import android.opengl.GLSurfaceView;

import sg.com.temasys.skylink.sdk.listener.MediaListener;

/**
 * @author Temasys Communications Pte Ltd
 */
public class MediaAdapter implements MediaListener {

    /**
     *
     */
    public MediaAdapter() {
    }


    @Override
    public void onRemotePeerAudioToggle(String remotePeerId, boolean isMuted) {

    }

    @Override
    public void onRemotePeerVideoToggle(String remotePeerId, boolean isMuted) {

    }

    @Override
    public void onRemotePeerMediaReceive(String remotePeerId, GLSurfaceView videoView) {

    }

    @Override
    public void onLocalMediaCapture(GLSurfaceView videoView) {

    }

    @Override
    public void onVideoSizeChange(String peerId, Point size) {

    }

}