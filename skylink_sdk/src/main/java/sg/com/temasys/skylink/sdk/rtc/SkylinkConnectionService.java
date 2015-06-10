package sg.com.temasys.skylink.sdk.rtc;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.PeerConnection;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by xiangrong on 20/5/15.
 */
class SkylinkConnectionService implements AppServerClientListener, SignalingMessageListener {
    private static final String TAG = SkylinkConnectionService.class.getName();
    private ConnectionState connectionState;
    private final SkylinkConnection skylinkConnection;
    private final SignalingMessageProcessingService signalingMessageProcessingService;
    private final AppServerClient appServerClient;

    private AppRTCSignalingParameters appRTCSignalingParameters;

    public SkylinkConnectionService(SkylinkConnection skylinkConnection) {
        this.skylinkConnection = skylinkConnection;
        this.appServerClient = new AppServerClient(this);
        connectionState = ConnectionState.DISCONNECTED;
        this.signalingMessageProcessingService = new SignalingMessageProcessingService(
                skylinkConnection, this, new MessageProcessorFactory(), this);
    }

    @Override
    public void onErrorAppServer(final int message) {
        skylinkConnection.runOnUiThread(new Runnable() {
            public void run() {
                // Prevent thread from executing with disconnect concurrently.
                synchronized (skylinkConnection.getLockDisconnect()) {
                    // If user has indicated intention to disconnect,
                    // We should no longer process messages from signalling server.
                    if (isDisconnected()) {
                        return;
                    }
                    skylinkConnection.getLifeCycleListener()
                            .onConnect(false, "Obtained ErrorCode: " + message + ".");
                }
            }
        });
    }

    @Override
    public void onErrorAppServer(final String message) {
        skylinkConnection.runOnUiThread(new Runnable() {
            public void run() {
                // Prevent thread from executing with disconnect concurrently.
                synchronized (skylinkConnection.getLockDisconnect()) {
                    // If user has indicated intention to disconnect,
                    // We should no longer process messages from signalling server.
                    if (isDisconnected()) {
                        return;
                    }
                    skylinkConnection.getLifeCycleListener().onConnect(false, message);
                }
            }
        });
    }

    /**
     * Connect to Signaling Server and start signaling process with room.
     *
     * @param params Parameters obtained from App server.
     */
    @Override
    public void onObtainedRoomParameters(AppRTCSignalingParameters params) {
        setAppRTCSignalingParameters(params);
        // Connect to Signaling Server and start signaling process with room.
        signalingMessageProcessingService.connect(getIpSigServer(),
                getPortSigServer(), getSid(), getRoomId());
    }

    /**
     * SignalingMessageListener implementation
     */
    @Override
    public void onConnectedToRoom() {
        // Send joinRoom.
        ProtocolHelper.sendJoinRoom(this);

        skylinkConnection.runOnUiThread(new Runnable() {
            public void run() {
                // Prevent thread from executing with disconnect concurrently.
                synchronized (skylinkConnection.getLockDisconnect()) {
                    // If user has indicated intention to disconnect,
                    // We should no longer process messages from signalling server.
                    if (isDisconnected()) {
                        return;
                    }
                    skylinkConnection.getLifeCycleListener().onConnect(true, null);
                }
            }
        });
    }

    /**
     * Check if already connected to Room, i.e., to Signaling server.
     *
     * @return
     */
    boolean isAlreadyConnected() {
        boolean connected = (connectionState == ConnectionState.CONNECTED);
        return connected;
    }

    /**
     * Check if disconnected or disconnecting from Room, i.e., to Signaling server.
     *
     * @return
     */
    boolean isDisconnected() {
        boolean disconnected = (connectionState == ConnectionState.DISCONNECTED ||
                connectionState == ConnectionState.DISCONNECTING);
        return disconnected;
    }

    /**
     * List of Connection state types
     */
    public enum ConnectionState {
        CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED
    }

    /**
     * Asynchronously connect to an AppRTC room URL, e.g. https://apprtc.appspot.com/?r=NNN and
     * register message-handling callbacks on its GAE Channel.
     *
     * @throws IOException
     * @throws JSONException
     * @throws Exception
     */
    public void connectToRoom(String url) throws IOException, JSONException {
        // Record user intention for connection to room state
        connectionState = ConnectionState.CONNECTING;
        this.appServerClient.connectToRoom(url);
    }

    /**
     * Sends a user defined message to a specific remote peer or to all remote peers via a server.
     *
     * @param remotePeerId Id of the remote peer to whom we will send a message. Use 'null' if the
     *                     message is to be broadcast to all remote peers in the room.
     * @param message      User defined data. May be a 'java.lang.String', 'org.json.JSONObject' or
     *                     'org.json.JSONArray'.
     */
    void sendServerMessage(String remotePeerId, Object message) {
        if (this.appServerClient == null)
            return;

        JSONObject dict = new JSONObject();
        try {
            dict.put("cid", getCid());
            dict.put("data", message);
            dict.put("mid", getSid());
            dict.put("rid", getRoomId());
            if (remotePeerId != null) {
                dict.put("type", "private");
                dict.put("target", remotePeerId);
            } else {
                dict.put("type", "public");
            }
            sendMessage(dict);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Sends local user data related to oneself, to all remote peers in our room.
     *
     * @param userData User defined data relating to the peer. May be a 'java.lang.String',
     *                 'org.json.JSONObject' or 'org.json.JSONArray'.
     */
    void sendLocalUserData(Object userData) {
        if (this.appServerClient == null) {
            return;
        }

        skylinkConnection.setUserData(userData);
        JSONObject dict = new JSONObject();
        try {
            dict.put("type", "updateUserEvent");
            dict.put("mid", getSid());
            dict.put("rid", getRoomId());
            dict.put("userData", userData);
            sendMessage(dict);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Disconnect from the Signaling Channel.
     *
     * @return False if unable to disconnect.
     */
    public boolean disconnect() {
        // Record user intention for disconnecting to room
        connectionState = ConnectionState.DISCONNECTING;

        if (this.signalingMessageProcessingService != null) {
            signalingMessageProcessingService.disconnect();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Restart all connections when rejoining room.
     *
     * @param skylinkConnection SkylinkConnection instance.
     */
    void rejoinRestart(SkylinkConnection skylinkConnection) {
        if (skylinkConnection.getPcObserverPool() != null) {
            // Create a new peerId set to prevent concurrent modification of the set
            Set<String> peerIdSet = new HashSet<String>(skylinkConnection.getPcObserverPool().keySet());
            for (String peerId : peerIdSet) {
                rejoinRestart(peerId, skylinkConnection);
            }
        }
    }

    /**
     * Restart specific connection when rejoining room. Sends targeted "enter" for non-Android
     * peers. This is a hack to accomodate the non-Android clients until the update to SM 0.1.1 This
     * is esp. so for the JS clients which do not allow restarts for PeerIds without
     * PeerConnection.
     *
     * @param remotePeerId      PeerId of the remote Peer with whom we should restart with.
     * @param skylinkConnection SkylinkConnection instance.
     */
    void rejoinRestart(String remotePeerId, SkylinkConnection skylinkConnection) {
        if (skylinkConnection.getSkylinkConnectionService().getConnectionState() == ConnectionState.DISCONNECTING) {
            return;
        }
        synchronized (skylinkConnection.getLockDisconnect()) {
            try {
                Log.d(TAG, "[rejoinRestart] Peer " + remotePeerId + ".");
                PeerInfo peerInfo = skylinkConnection.getPeerInfoMap().get(remotePeerId);
                if (peerInfo != null && peerInfo.getAgent().equals("Android")) {
                    // If it is Android, send restart.
                    Log.d(TAG, "[rejoinRestart] Peer " + remotePeerId + " is Android.");
                    ProtocolHelper.sendRestart(remotePeerId, skylinkConnection, this,
                            skylinkConnection.getLocalMediaStream(), skylinkConnection.getMyConfig());
                } else {
                    // If web or others, send directed enter
                    // TODO XR: Remove after JS client update to compatible restart protocol.
                    Log.d(TAG, "[rejoinRestart] Peer " + remotePeerId + " is non-Android or has no PeerInfo.");
                    ProtocolHelper.sendEnter(remotePeerId, skylinkConnection, this);
                }
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage(), e);
            }
        }
    }

    void restartConnectionInternal(String remotePeerId, SkylinkConnection skylinkConnection) {
        if (skylinkConnection.getSkylinkConnectionService().getConnectionState() == ConnectionState.DISCONNECTING) {
            return;
        }
        synchronized (skylinkConnection.getLockDisconnect()) {
            try {
                ProtocolHelper.sendRestart(remotePeerId, skylinkConnection, this, skylinkConnection.getLocalMediaStream(),
                        skylinkConnection.getMyConfig());
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public void sendMessage(JSONObject dictMessage) {
        if (this.signalingMessageProcessingService == null) {
            return;
        }
        signalingMessageProcessingService.sendMessage(dictMessage);
    }

    /**
     * Notify all the peers in the room on our changed audio status.
     *
     * @param isMuted Flag that specifies whether audio is now mute
     */
    void sendMuteAudio(boolean isMuted) {
        ProtocolHelper.sendMuteAudio(isMuted, this);
    }

    /**
     * Notify all the peers in the room on our changed video status.
     *
     * @param isMuted Flag that specifies whether video is now mute
     */
    void sendMuteVideo(boolean isMuted) {
        ProtocolHelper.sendMuteVideo(isMuted, this);
    }

    // Getters and Setters
    public SignalingMessageProcessingService getSignalingMessageProcessingService() {
        return signalingMessageProcessingService;
    }

    public AppRTCSignalingParameters getAppRTCSignalingParameters() {
        return appRTCSignalingParameters;
    }

    public void setAppRTCSignalingParameters(AppRTCSignalingParameters appRTCSignalingParameters) {
        this.appRTCSignalingParameters = appRTCSignalingParameters;
    }

    public String getAppOwner() {
        return appRTCSignalingParameters.getAppOwner();
    }

    public String getIpSigServer() {
        return this.appRTCSignalingParameters.getIpSigserver();
    }

    public int getPortSigServer() {
        return this.appRTCSignalingParameters.getPortSigserver();
    }

    public String getCid() {
        return appRTCSignalingParameters.getCid();
    }

    public List<PeerConnection.IceServer> getIceServers() {
        return this.appRTCSignalingParameters.getIceServers();
    }

    public void setIceServers(List<PeerConnection.IceServer> iceServers) {
        this.appRTCSignalingParameters.setIceServers(iceServers);
    }


    public String getLen() {
        return appRTCSignalingParameters.getLen();
    }

    public String getRoomCred() {
        return appRTCSignalingParameters.getRoomCred();
    }

    public String getRoomId() {
        return appRTCSignalingParameters.getRoomId();
    }

    public String getSid() {
        return appRTCSignalingParameters.getSid();
    }

    public void setSid(String sid) {
        this.appRTCSignalingParameters.setSid(sid);
    }

    public String getStart() {
        return appRTCSignalingParameters.getStart();
    }

    public void setStart(String start) {
        this.appRTCSignalingParameters.setStart(start);
    }

    public String getTimeStamp() {
        return appRTCSignalingParameters.getTimeStamp();
    }

    public String getUserCred() {
        return appRTCSignalingParameters.getUserCred();
    }

    public String getUserId() {
        return appRTCSignalingParameters.getUserId();
    }

    void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    ConnectionState getConnectionState() {
        return connectionState;
    }

}
