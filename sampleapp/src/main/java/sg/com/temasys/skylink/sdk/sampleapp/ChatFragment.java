package sg.com.temasys.skylink.sdk.sampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.temasys.skylink.sampleapp.R;

import org.json.JSONException;

import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sg.com.temasys.skylink.sdk.config.SkyLinkConfig;
import sg.com.temasys.skylink.sdk.listener.LifeCycleListener;
import sg.com.temasys.skylink.sdk.listener.MessagesListener;
import sg.com.temasys.skylink.sdk.listener.RemotePeerListener;
import sg.com.temasys.skylink.sdk.rtc.SkyLinkConnection;
import sg.com.temasys.skylink.sdk.rtc.SkyLinkException;

/**
 * This class is used to demonstrate the Chat between two clients in WebRTC
 * Created by lavanyasudharsanam on 20/1/15.
 */
public class ChatFragment extends Fragment implements LifeCycleListener, RemotePeerListener, MessagesListener {

    private static final String TAG = ChatFragment.class.getCanonicalName();
    public static final String ROOM_NAME = "chatRoom";
    public static final String MY_USER_NAME = "chatRoomUser";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private String remotePeerId;
    private Button btnSendPrivateServerMessage;
    private Button btnSendP2PPublicMessage;
    private Button btnSendPublicServerMessage;
    private ListView listViewChats;
    private TextView tvRoomDetails;
    private SkyLinkConnection skyLinkConnection;
    private BaseAdapter adapter;
    private List<String> chatMessageCollection;
    private String peerName;
    private Button btnSendP2PMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //initialize views
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        listViewChats = (ListView) rootView.findViewById(R.id.lv_messages);
        btnSendPrivateServerMessage = (Button) rootView.findViewById(R.id.btn_send_server_message);
        btnSendPublicServerMessage = (Button) rootView.findViewById(R.id.btn_send_public_server_message);
        btnSendP2PMessage = (Button) rootView.findViewById(R.id.btn_send_private_chat);
        btnSendP2PPublicMessage = (Button) rootView.findViewById(R.id.btn_send_p2p_public_message);
        tvRoomDetails = (TextView) rootView.findViewById(R.id.tv_room_details);
        chatMessageCollection = new ArrayList();

        /** Defining the ArrayAdapter to set items to ListView */
        adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, chatMessageCollection);

        /** Defining a click event listener for the button "Send Private Server Message" */
        btnSendPrivateServerMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Add chat message to the listview
                String message = addMessageToListView(true);

                //pass null for remotePeerId to send message to send mesage to all users in the room
                //sends message using the signalling server
                skyLinkConnection.sendServerMessage(remotePeerId, message);

            }
        });

        /** Defining a click event listener for the button "Send Public Server Message" */
        btnSendPublicServerMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Add chat message to the listview
                String message = addMessageToListView(false);

                //pass remotePeerId instead of null to send message to specific peer
                //sends message using the signalling server
                skyLinkConnection.sendServerMessage(null, message);

                adapter.notifyDataSetChanged();
            }
        });

        /** Defining a click event listener for the button "Send Public Server Message" */
        btnSendPublicServerMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Add chat message to the listview
                EditText edit = (EditText) getActivity().findViewById(R.id.chatMessage);
                String message = edit.getText().toString();
                chatMessageCollection.add("You : " + message);
                edit.setText("");

                //pass null for remotePeerId to send message to send mesage to all users in the room
                //sends message using the signalling server
                skyLinkConnection.sendServerMessage(null, message);

                adapter.notifyDataSetChanged();
            }
        });

        /** Defining a click event listener for the button "Send Private Message" */
        btnSendP2PMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remotePeerId == null) {
                    Toast.makeText(getActivity(), "There is no peer in the room to send a private message to", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Add chat message to the listview
                String message = addMessageToListView(true);

                try {
                    //sends p2p message using the datachannel to the specific user
                    skyLinkConnection.sendP2PMessage(remotePeerId, message);
                } catch (SkyLinkException e) {
                    Log.e(TAG, e.getMessage(), e);
                }

                adapter.notifyDataSetChanged();
            }
        });

        /** Defining a click event listener for the button "Send Public P2P Message" */
        btnSendP2PPublicMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remotePeerId == null) {
                    Toast.makeText(getActivity(), "There is no peer in the room to send a private message to", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Add chat message to the listview
                String message = addMessageToListView(false);

                try {
                    //sends p2p message using the datachannel to the all users
                    skyLinkConnection.sendP2PMessage(null, message);
                } catch (SkyLinkException e) {
                    Log.e(TAG, e.getMessage(), e);
                }

                adapter.notifyDataSetChanged();
            }
        });

        /** Setting the adapter to the ListView */
        listViewChats.setAdapter(adapter);

        return rootView;
    }

    /**
     * Retrives message written in edit text and adds it to the chatlistview
     *
     * @param isPrivateMessage
     * @return message that was added to the listview
     */
    private String addMessageToListView(boolean isPrivateMessage) {
        EditText edit = (EditText) getActivity().findViewById(R.id.chatMessage);
        String message = edit.getText().toString();
        chatMessageCollection.add(isPrivateMessage ? "You : <Private>" + message : "You : " + message);
        edit.setText("");
        adapter.notifyDataSetChanged();
        return message;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeSkylinkConnection();

        try {
            skyLinkConnection.connectToRoom(ROOM_NAME,
                    MY_USER_NAME, new Date(), Constants.DURATION);
        } catch (SignatureException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void initializeSkylinkConnection() {
        skyLinkConnection = SkyLinkConnection.getInstance();
        //the app_key and app_secret is obtained from the temasys developer console.
        skyLinkConnection.init(getString(R.string.app_key),
                getString(R.string.app_secret), getSkylinkConfig(), this.getActivity().getApplicationContext());
        //set listeners to receive callbacks when events are triggered
        skyLinkConnection.setLifeCycleListener(this);
        skyLinkConnection.setMessagesListener(this);
        skyLinkConnection.setRemotePeerListener(this);
    }

    private SkyLinkConfig getSkylinkConfig() {
        SkyLinkConfig config = new SkyLinkConfig();
        //AudioVideo config options can be NO_AUDIO_NO_VIDEO, AUDIO_ONLY, VIDEO_ONLY, AUDIO_AND_VIDEO;
        config.setAudioVideoSendConfig(SkyLinkConfig.AudioVideoConfig.NO_AUDIO_NO_VIDEO);
        config.setHasPeerMessaging(true);
        config.setHasFileTransfer(true);
        config.setTimeout(Constants.TIME_OUT);
        return config;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDetach() {
        //close the connection when the fragment is detached, so the streams are not open.
        if (skyLinkConnection != null) {
            skyLinkConnection.disconnectFromRoom();
            skyLinkConnection.setLifeCycleListener(null);
            skyLinkConnection.setRemotePeerListener(null);
            skyLinkConnection.setMessagesListener(null);
        }
        super.onDetach();
    }


    /***
     * Lifecycle Listener Callbacks -- triggered during events that happen during the SDK's lifecycle
     */

    /**
     * Triggered if the connection is successful
     *
     * @param isSuccess
     * @param message
     */

    @Override
    public void onConnect(boolean isSuccess, String message) {
        //update textview if connection is successful
        if (isSuccess) {
            Utils.setRoomDetails(false, tvRoomDetails, this.peerName, ROOM_NAME, MY_USER_NAME);
        } else {
            Toast.makeText(getActivity(), "Skylink Connection Failed\nReason : " + message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWarning(String message) {
        Log.d(TAG, message + "warning");
    }

    @Override
    public void onDisconnect(String message) {
        Log.d(TAG, message + " disconnected");
    }

    @Override
    public void onReceiveLog(String message) {
        Log.d(TAG, message + " on receive log");
    }

    /**
     * Remote Peer Listener Callbacks - triggered during events that happen when data or connection with remote peer changes
     */

    @Override
    public void onRemotePeerJoin(String remotePeerId, Object userData) {
        // If there is an existing peer, prevent new remotePeer from joining call.
        if (this.remotePeerId != null) {
            Toast.makeText(getActivity(), "Rejected third peer from joining conversation",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //if first remote peer to join room, keep track of user and update text-view to display details
        this.remotePeerId = remotePeerId;
        if (userData instanceof String) {
            this.peerName = (String) userData;
            Utils.setRoomDetails(true, tvRoomDetails, this.peerName, ROOM_NAME, MY_USER_NAME);
        }
    }

    @Override
    public void onRemotePeerUserDataReceive(String remotePeerId, Object userData) {
        Toast.makeText(getActivity(), "Getting user data", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemotePeerLeave(String remotePeerId, String message) {
        Toast.makeText(getActivity(), "Your peer has left the room", Toast.LENGTH_SHORT).show();
        //reset peerId
        this.remotePeerId = null;
        this.peerName = null;
        //update textview to show room status
        Utils.setRoomDetails(false, tvRoomDetails, this.peerName, ROOM_NAME, MY_USER_NAME);
    }

    @Override
    public void onOpenDataConnection(String peerId) {
        Log.d(TAG, "onOpenDataConnection");
    }

    /**
     * Message Listener Callbacks - triggered during events that happen when messages are received from remotePeer
     */

    @Override
    public void onServerMessageReceive(String remotePeerId, Object message, boolean isPrivate) {
        String chatPrefix = "";
        //add prefix if the chat is a private chat - not seen by other users.
        if (isPrivate) {
            chatPrefix = "<Private> ";
        }
        //add message to listview and update ui
        if (message instanceof String) {
            chatMessageCollection.add(this.peerName + " : " + chatPrefix + message);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onP2PMessageReceive(String remotePeerId, Object message, boolean isPrivate) {
        //add prefix if the chat is a private chat - not seen by other users.
        String chatPrefix = "";
        if (isPrivate) {
            chatPrefix = "<Private> ";
        }
        //add message to listview and update ui
        if (message instanceof String) {
            chatMessageCollection.add(this.peerName + " : " + chatPrefix + message);
            adapter.notifyDataSetChanged();
        }
    }
}
