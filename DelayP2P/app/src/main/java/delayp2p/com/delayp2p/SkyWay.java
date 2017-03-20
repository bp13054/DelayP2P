package delayp2p.com.delayp2p;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import io.skyway.Peer.ConnectOption;
import io.skyway.Peer.DataConnection;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerError;
import io.skyway.Peer.PeerOption;

/**
 * Created by yusuke on 2016/11/03.
 */
public class SkyWay{
    private Context context;
    private Nifty nifty;
    private Peer _peer; //自身のピアを格納
    private DataConnection _data;//相手と通信を行うデータコネクトを格納
    private String _id;//自身のpeerIDを格納
    private Boolean _bConnecting;
    private String recieveRoomID="";
    private MyLocation myl;
    private Activity activity;

    String connectingChecker = "CANCEL";
    final static private String CONNECT_CANCEL = "CANCEL";
    final static private String CONNECT_OK = "OK";

    private Boolean connectionSend = false;



    SkyWay(Context context){
        this.context = context;

        //////////////////////////////////////////////////////////////////////
        //////////////////  START: Initialize Peer ///////////////////////////
        //////////////////////////////////////////////////////////////////////


        // connect option
        PeerOption options = new PeerOption();

        // Please check this page. >> https://skyway.io/ds/
        //Enter your API Key and registered Domain.
        options.key = "e10651a5-82a3-4621-8e13-d75af5d78c22";
        options.domain = "bp13054.com";
        options.turn = true;

        // PeerOption has many options. Please check the document. >> http://nttcom.github.io/skyway/docs/

        _peer = new Peer(context, options);
        setPeerCallback(_peer);

        //////////////////////////////////////////////////////////////////////
        ////////////////// END: Initialize Peer //////////////////////////////
        //////////////////////////////////////////////////////////////////////
    }

    public void setActivity(Activity activity){
        this.activity = activity;
    }

    public void setNifty(Nifty nifty){
        this.nifty = nifty;
    }

    public void setMyLocation(MyLocation myl){
        this.myl = myl;
    }

    public String getSkyWayId(){
        return _id;
    }

    public DataConnection getData(){
        return _data;
    }


    void connecting(final String strPeerId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("aite:skywayId", _id);
                if (null == _peer) {
                    return;
                }

                if (null != _data) {
                    _data.close();
                    _data = null;
                }

                //////////////////////////////////////////////////////////////////////
                ///////////////  START: Connecting Peer   ////////////////////////////
                //////////////////////////////////////////////////////////////////////
                connectionSend = true; // 自分がconnectingしたことを記録

                // connect option
                ConnectOption option = new ConnectOption();
                option.metadata = "data connection";
                option.label = "chat";
                option.serialization = DataConnection.SerializationEnum.BINARY;


                // connect
                _data = _peer.connect(strPeerId, option);
                if (null != _data) {
                    setDataCallback(_data);
                }
                //////////////////////////////////////////////////////////////////////
                ////////////////  END: Connecting Peer   /////////////////////////////
                //////////////////////////////////////////////////////////////////////
            }
        }).start();
    }


    private void setPeerCallback(Peer peer) {
        //////////////////////////////////////////////////////////////////////////////////
        ////////////////////  START: Set SkyWay peer callback   //////////////////////////
        //////////////////////////////////////////////////////////////////////////////////

        // !!!: Event/Open
        peer.on(Peer.PeerEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                // TODO: PeerEvent/OPEN

                if (object instanceof String) {
                    _id = (String) object; //自身のskyway id
                    Log.d("skywayId",_id);
                    myl.setPeerId(_id);
                }
            }
        });

        // !!!: Event/Connection
        peer.on(Peer.PeerEventEnum.CONNECTION, new OnCallback() {
            @Override
            public void onCallback(final Object object) {
                // TODO: PeerEvent/CONNECTION

                if (!(object instanceof DataConnection)) {
                    return;
                }

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // TODO: DataEvent/ERROR
//                        _data = (DataConnection) object;
//
//                    }
//                }).start();
               _data = (DataConnection) object;
                setDataCallback(_data);

//                String title = "アクセス要求";
//                String strMessage = "接続を許可しますか？";
//                String strLabelOk = context.getString(android.R.string.ok);
//                String strLabelNo = context.getString(android.R.string.cancel);
//                MessageDialogFragment dialog = new MessageDialogFragment();
//                dialog.setPositiveLabel(strLabelOk);
//                dialog.setNegativeLabel(strLabelNo);
//                dialog.setTitle(title);
//                dialog.setMessage(strMessage);
//                dialog.show(activity.getFragmentManager(), "test");
//                Log.d("onItemClick", "よべる？");

            }

        });


        // !!!: Event/Close
        peer.on(Peer.PeerEventEnum.CLOSE, new OnCallback() {
            @Override
            public void onCallback(Object object) {
            }
        });

        // !!!: Event/Disconnected
        peer.on(Peer.PeerEventEnum.DISCONNECTED, new OnCallback() {
            @Override
            public void onCallback(Object object) {
            }
        });

        // !!!: Event/Error
        peer.on(Peer.PeerEventEnum.ERROR, new OnCallback() {
            @Override
            public void onCallback(Object object) {
            }
        });
    }
    //////////////////////////////////////////////////////////////////////////////////
    /////////////////////  END: Set SkyWay peer callback   ///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////


    //Unset peer callback
    void unsetPeerCallback(Peer peer) {
        peer.on(Peer.PeerEventEnum.OPEN, null);
        peer.on(Peer.PeerEventEnum.CONNECTION, null);
        peer.on(Peer.PeerEventEnum.CALL, null);
        peer.on(Peer.PeerEventEnum.CLOSE, null);
        peer.on(Peer.PeerEventEnum.DISCONNECTED, null);
        peer.on(Peer.PeerEventEnum.ERROR, null);
    }

    //////////////////////////////////////////////////////////////////////////////////
    ///////////////  START: Set SkyWay peer Data connection callback   ///////////////
    //////////////////////////////////////////////////////////////////////////////////
    void setDataCallback(DataConnection data) {
        // !!!: DataEvent/Open
        data.on(DataConnection.DataEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                if(!connectionSend) {
                    connected();
                }
            }
        });

        // !!!: DataEvent/Data
        data.on(DataConnection.DataEventEnum.DATA, new OnCallback() {

            @Override
            public void onCallback(final Object object) {
                String strValue = null;


                if (object instanceof String) {

                } else if (object instanceof Double) {
                    Double doubleValue = (Double) object;

                    strValue = doubleValue.toString();
                } else if (object instanceof ArrayList) {
                    // TODO: receive Array list object
                    ArrayList arrayValue = (ArrayList) object;

                    StringBuilder sbResult = new StringBuilder();

                    for (Object item : arrayValue) {
                        sbResult.append(item.toString());
                        sbResult.append("\n");
                    }

                    strValue = sbResult.toString();
                } else if (object instanceof Map) {
                    // TODO: receive Map object
                    Map mapValue = (Map) object;

                    StringBuilder sbResult = new StringBuilder();

                    Object[] objKeys = mapValue.keySet().toArray();
                    for (Object objKey : objKeys) {
                        Object objValue = mapValue.get(objKey);

                        sbResult.append(objKey.toString());
                        sbResult.append(" = ");
                        sbResult.append(objValue.toString());
                        sbResult.append("\n");
                    }

                    strValue = sbResult.toString();
                } else if (object instanceof byte[]) {
                    // TODO: receive byte[] object
                    Bitmap bmp = null;
                    byte[] byteArray = (byte[]) object;
                    if (byteArray != null) {
                        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    }
                    strValue = "Received Image.(Type:byte[])";
                }



            }
        });

        // !!!: DataEvent/Close
        data.on(DataConnection.DataEventEnum.CLOSE, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                // TODO: DataEvent/CLOSE
                _data = null;
                disconnected();
            }
        });

        // !!!: DataEvent/Error
        data.on(DataConnection.DataEventEnum.ERROR, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                // TODO: DataEvent/ERROR
                PeerError error = (PeerError) object;



                String strMessage = error.message;
                String strLabel = context.getString(android.R.string.ok);


            }
        });
    }
    //////////////////////////////////////////////////////////////////////////////////
    /////////////////  END: Set SkyWay peer Data connection callback   ///////////////
    //////////////////////////////////////////////////////////////////////////////////

    void unsetDataCallback(DataConnection data) {
        data.on(DataConnection.DataEventEnum.OPEN, null);
        data.on(DataConnection.DataEventEnum.DATA, null);
        data.on(DataConnection.DataEventEnum.CLOSE, null);
        data.on(DataConnection.DataEventEnum.ERROR, null);
    }


    /**
     * Destroy Peer.
     */
    public void destroyPeer() {
        if (null != _data) {
            unsetDataCallback(_data);

            _data = null;
        }

        if (null != _peer) {
            unsetPeerCallback(_peer);

            if (false == _peer.isDisconnected) {
                _peer.disconnect();
            }

            if (false == _peer.isDestroyed) {
                _peer.destroy();
            }

            _peer = null;
        }
    }


    /**
     * Closing connection.
     */
    void closing() {
        if (false == _bConnecting) {
            return;
        }

        _bConnecting = false;

        if (null != _data) {
            _data.close();
        }
        connectingChecker = CONNECT_CANCEL; //複数人通信だと怪しい
    }

    void connected() {
        _bConnecting = true;
    }

    void disconnected() {
        _bConnecting = false;

    }

    public void onRestart(){
        this._id = null;
    }
}
