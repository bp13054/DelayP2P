package delayp2p.com.delayp2p;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import io.skyway.Peer.ConnectOption;
import io.skyway.Peer.DataConnection;
import io.skyway.Peer.IceConfig;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerError;
import io.skyway.Peer.PeerOption;

public class P2P{
    final private static Boolean DATACONNECTION_OPEN_CHECK = true;//dataConnectionがopenしている場合がtrue
    final  private static Boolean GET_PARTNER_LOCATION = true; // 相手から位置情報を取得した場合はtrue
    private Context context;
    private Peer peer;
    private Location myLocation;
    private String myId;
    private ArrayList<DataConnection> dataArray;
    private int delayTime = 0;
    private Boolean dataConnectionOpenCheck = false;
    private Boolean getPartnerLocation = false;
    /* debug timer */ /*
    private long startTime;
    private long stopTime;
    /* debug timer */

    public P2P(Context context,int delayTime) {
        this.context = context;
        this.delayTime = delayTime;
        dataArray = new ArrayList<>();
    }

    // 自分のPeerを作成
    public void createPeer(String myId) {
        this.myId = myId;
        PeerOption options = new PeerOption();
        options.key = "e10651a5-82a3-4621-8e13-d75af5d78c22";
        options.domain = "bp13054.com";
        options.turn = true;
        peer = new Peer(context, myId, options);
        setPeerCallback(peer);
    }

    // 自分のPeerを作成 (MyServer)
    public void createPeerMyServer() {
        PeerOption options = new PeerOption();
        options.key = "peerjs";
        options.host = "<シグナリングサーバのURLを入力>";
        options.port = 443;
        options.secure = true;

        ArrayList<IceConfig> configArray = new ArrayList<>();
        IceConfig iceConfig = new IceConfig();
        iceConfig.url = "stun:stun.l.google.com:19302";
        configArray.add(iceConfig);
        options.config = configArray;

        peer = new Peer(context, options);
        setPeerCallback(peer);
    }

    // 相手のPeerに接続
    public void connectPeer(String ptrId) {
        ConnectOption option = new ConnectOption();
        option.metadata = "data connection";
        option.serialization = DataConnection.SerializationEnum.BINARY;
        DataConnection dataConnection = peer.connect(ptrId, option);
        setDataCallbackConnect(dataConnection);
        dataArray.add(dataConnection);
        Log.d("debug", "--------------------------------------- [System]Connecting... --------------------------------------------");
    }

    // 相手に文字列を送信
    public void sendMsg(DataConnection dataConnection, String msg) {
        /* debug timer */ /*
        startTime = System.currentTimeMillis();
        /* debug timer */
        boolean result = dataConnection.send(msg);
        if (result) {   // 送信に成功した場合
            Log.d("debug", "--------------------------------------- [You]" + msg + " --------------------------------------------");
        } else {    // 送信に失敗した場合
            Log.d("debug", "--------------------------------------- [System]Error. --------------------------------------------");
        }
    }

    // 相手にLocation型を送信
    public void sendLocation(DataConnection dataConnection, Location location) {
        boolean result = dataConnection.send(location);
        if (result) {   // 送信に成功した場合
            Log.d("debug", "--------------------------------------- [You:location送信] --------------------------------------------");
        } else {    // 送信に失敗した場合
            Log.d("debug", "--------------------------------------- [System]Error. --------------------------------------------");
        }
    }


    // PeerCallback集
    private void setPeerCallback(Peer peer) {
        // Peerを作成できた場合に呼び出される
        peer.on(Peer.PeerEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d("debug", "--------------------------------------- [System]Peer created. --------------------------------------------");
                Log.d("debug", "--------------------------------------- registered SkyWayID: " + (String)o + "------------------------------------------------");
            }
        });

        // 相手から接続された場合に呼び出される
        peer.on(Peer.PeerEventEnum.CONNECTION, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                if (o instanceof DataConnection) {
                    DataConnection dataConnection = (DataConnection) o;
                    setDataCallbackConnected(dataConnection);
                    dataArray.add(dataConnection);
                }
            }
        });

        // 何かしらのエラーが発生した場合に呼び出される
        peer.on(Peer.PeerEventEnum.ERROR, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                PeerError peerError = (PeerError) o;
                Log.d("debug", "--------------------------------------- Peer:" + peerError.type + "--------------------------------------------");
                switch (peerError.type) {
                    // IDが正しくなかった場合
                    case INVALID_ID:
                        break;
                }
            }
        });
    }

    // PeerCallbackのCallbackメソッドを設定しないバージョン
    private void unsetPeerCallback() {
        peer.on(Peer.PeerEventEnum.OPEN, null);
        peer.on(Peer.PeerEventEnum.CONNECTION, null);
        peer.on(Peer.PeerEventEnum.ERROR, null);
    }

    // DataCallback集(接続する側)
    private void setDataCallbackConnect(final DataConnection dataConnection) {
        // 相手と接続できた場合に呼び出される
        dataConnection.on(DataConnection.DataEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d("debug", "--------------------------------------- [System]送信する側Connected. --------------------------------------------");
                // 相手に自分の位置情報を送信
                SetDate date = new SetDate();
                String t = date.convertLong(myLocation.getTime());
                sendMsg(dataConnection, t);
                // 接続を切断
//                dataConnection.close();
//                unsetDataCallback(dataConnection);
//                dataArray.remove(dataConnection);
            }
        });

        // 相手からデータが送られてきた場合に呼び出される
        dataConnection.on(DataConnection.DataEventEnum.DATA, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                /* debug timer */ /*
                stopTime = System.currentTimeMillis();
                long time = stopTime - startTime;
                int second = (int)(time / 1000);
                int comma = (int)(time % 1000);
                Log.d("DebugTime", "," + dataConnection.peer + "," + second + "." + comma);
                /* debug timer */
                if (o instanceof String) {
                    Log.d("debug", "--------------------------------------- [Partner]" + o + " --------------------------------------------");

                }
            }
        });

        // 相手との接続が切れた場合に呼び出される
        dataConnection.on(DataConnection.DataEventEnum.CLOSE, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d("debug", "--------------------------------------- [System]送信する側Disconnected. --------------------------------------------");
                unsetDataCallback(dataConnection);
            }
        });

        // 何かしらのエラーが発生した場合に呼び出される
        dataConnection.on(DataConnection.DataEventEnum.ERROR, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                PeerError peerError = (PeerError) o;
                Log.d("debug", "--------------------------------------- Data:" + peerError.type + "--------------------------------------------");
            }
        });
    }

    // DataCallback集(接続される側)
    private void setDataCallbackConnected(final DataConnection dataConnection) {
        // 相手と接続できた場合に呼び出される
        dataConnection.on(DataConnection.DataEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d("debug", "--------------------------------------- [System]送信される側Connected. --------------------------------------------");
                if(getPartnerLocation != GET_PARTNER_LOCATION){
                    dataConnectionOpenCheck = DATACONNECTION_OPEN_CHECK;
                }else{
                    dataConnection.close();
                    unsetDataCallback(dataConnection);
                    dataArray.remove(dataConnection);

                    getPartnerLocation = false;
                }

            }
        });

        // 相手からデータが送られてきた場合に呼び出される
        dataConnection.on(DataConnection.DataEventEnum.DATA, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                //if (o instanceof Location) {
                    Log.d("debug", "------------------------------------------------ 相手位置情報をget -------------------------------------------------");
                    long tmp = System.currentTimeMillis()-(long)delayTime;
                   // Location sendedLocation = (Location)o;
String locationTime = (String)o;
//                    DisplayToast toast = new DisplayToast(context);
                    SetDate d = new SetDate();
                    String t= d.convertLong(tmp);
//                    toast.toast(dataConnection.peer,locationTime,t);
                Log.d("debug","送信元peerId:"+dataConnection);
                Log.d("debug","相手位置情報取得時間:"+locationTime);
                Log.d("debug","MYPCに送信された時間:"+t);

                if(dataConnectionOpenCheck == DATACONNECTION_OPEN_CHECK) {
                    dataConnection.close();
                    unsetDataCallback(dataConnection);
                    dataArray.remove(dataConnection);

                    dataConnectionOpenCheck = false;
                }else{
                    getPartnerLocation = GET_PARTNER_LOCATION;
                }

//                    // 相手に自分の位置情報を返信
//                    if (myLocation == null) {
//                        Log.d("debug", "------------------------------------------------ myLocation : NULL in P2PSpecial -------------------------------------------------");
//                        String tmp = "REP,";
//                        tmp += myId + "," ;
//                        tmp += 35.8641 + "," + 139.4176;
//                        sendMsg(dataConnection, tmp);
//                    } else {
//                        Log.d("debug", "------------------------------------------------ myLocation : notNULL in P2PSpecial -------------------------------------------------");
//                        sendMsg(dataConnection, "REP," + myId + "," + myLocation.getLatitude() + "," + myLocation.getLongitude());
//                    }
                //}
            }
        });

        // 相手との接続が切れた場合に呼び出される
        dataConnection.on(DataConnection.DataEventEnum.CLOSE, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d("debug", "--------------------------------------- [System]送信される側Disconnected. --------------------------------------------");
                unsetDataCallback(dataConnection);
                dataArray.remove(dataConnection);
            }
        });

        // 何かしらのエラーが発生した場合に呼び出される
        dataConnection.on(DataConnection.DataEventEnum.ERROR, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                PeerError peerError = (PeerError) o;
                Log.d("debug", "--------------------------------------- Data:" + peerError.type + "--------------------------------------------");
            }
        });
    }

    // DataCallbackのCallbackメソッドを設定しないバージョン
    private void unsetDataCallback(final DataConnection dataConnection) {
        dataConnection.on(DataConnection.DataEventEnum.OPEN, null);
        dataConnection.on(DataConnection.DataEventEnum.DATA, null);
        dataConnection.on(DataConnection.DataEventEnum.CLOSE, null);
        dataConnection.on(DataConnection.DataEventEnum.ERROR, null);
    }

    /**
     * Destroy Peer.
     */
    public void destroyPeer() {
        int size = dataArray.size();
        if (size > 0) {
            for (int i = 0; i<=size; i++){
             unsetDataCallback(dataArray.get(i));
            }
        }
    }

    // SkyWayIDの作成
    public String generateID() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmssSSS", Locale.JAPAN);
        Date date = new Date(System.currentTimeMillis());
        UUID uuid = UUID.randomUUID();
        Log.d("debug", "---------------------------------------- SkyWayID : " + df.format(date) + "-" + uuid +"-------------------------------------------------");
        return df.format(date) + "-" + uuid;
    }

    public void setMyLocation(Location myLocation) {
        this.myLocation = myLocation;
    }

    public String getPeerId(){
        return myId;
    }

    //onDestroy時にサーバとのコネクションを切断および破棄
    public void onDestroy() {
        for (int i = 0; i < dataArray.size(); i++) {
            DataConnection dataConnection = dataArray.get(i);
            if (dataConnection != null && dataConnection.isOpen) {
                dataConnection.close();
                unsetDataCallback(dataConnection);
            }
        }
        dataArray.clear();

        if (peer != null) {
            unsetPeerCallback();

            if (!peer.isDisconnected) {
                peer.disconnect();
            }
            if (!peer.isDestroyed) {
                peer.destroy();
            }

            peer = null;
        }
    }
}
