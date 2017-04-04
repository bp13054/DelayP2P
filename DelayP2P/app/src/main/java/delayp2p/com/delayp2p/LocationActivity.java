package delayp2p.com.delayp2p;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nifty.cloud.mb.core.NCMBObject;

import java.util.List;

public class LocationActivity extends AppCompatActivity implements LocationListener {
    final private static int LOCATION_UPDATE_COUNT = 5; //位置情報を5回取得したらNiftyに現在位置を登録．また，周辺ユーザ検索も実施
    final private static int MAX_UPDATE = 60;//位置情報を60回取得したら位置情報を取得することを終了する
    final private static int REQUEST_PERMISSION = 1000;
    private Nifty nifty; //Nifty Cloudを使うクラス
    private P2P p2p; //SkyWayを使うクラス
    private int delayTime = 0;//初期値
    private String peerId = "unSetPeerId";//初期値
    private int getLocationUpdateCount = 4;//位置情報のカウント回数を記録
    private List<NCMBObject> peerList;
    private Location location = new Location(""); //自身の位置情報を保存するのに用いる
    private LocationManager mLocationManager;//位置情報を取得するのに用いる
    private long lastUpdate=0; //GPSとNETから重複して位置情報を取得してないか確認するのに用いる
    private String lastProvider="";//同様
    private TextView latLongTime;
    private TextView gosa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);



        nifty = new Nifty(this.getApplicationContext());
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        Intent intent = getIntent();
        delayTime = intent.getIntExtra("Delay",0);
        peerId = intent.getStringExtra("peerId");
        Log.d("端末の時間誤差とID","時間誤差:"+delayTime+"ID"+peerId);
        p2p = new P2P(this.getApplicationContext(),delayTime);

////skywayの通信はActivityの動作に影響しないよう非同期処理で行う
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                p2p.createPeer(peerId);
//            }
//        }).start();
        p2p.createPeer(peerId);


        //実験を開始する
        latLongTime = (TextView)findViewById(R.id.latlongtime) ;
        gosa = (TextView)findViewById(R.id.textView2) ;
        gosa.setText("端末の誤差は"+delayTime);
        Button measurement = (Button) findViewById(R.id.button2);
        measurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationPermissionCheck(); //androidAPIが23以上の場合に対応するためパーミッションの許可を取る
            }
        });
    }















    /**
     * androidAPI23以上の場合，位置情報取得の許可を取るメソッド
     */
    public void LocationPermissionCheck(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, REQUEST_PERMISSION);
            } else {
                requestLocationUpdates();//android6未満，または，すでにロケーションの許可がある
            }
        } else {
            requestLocationUpdates();//android6未満，または，すでにロケーションの許可がある
        }
    }





    // パーミッション(LocationPermissionCheck()のコールバック)の結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "許可がないためデータの取得ができません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }





    public void requestLocationUpdates() {
        if (mLocationManager!=null) {
            try {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
            }catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION",""+e);
            }
        }

    }






    /**
     *位置情報を返却するプログラムである．
     * GPS,NETから位置情報を取得できない場合は芝浦工業大学の位置を返却する
     * @return 最新の現在位置情報
     */
    public Location getLocation() {

        if (lastProvider.equals(LocationManager.GPS_PROVIDER)) {
            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }else if (lastProvider.equals(LocationManager.NETWORK_PROVIDER)){
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }else{
            location.setLatitude(35.9517927);
            location.setLongitude(139.6517786);
        }
        if (location == null) {
            location.setLatitude(35.9517927);
            location.setLongitude(139.6517786);
        }
        return location;
    }






    /********************************************LocationListenerのoverride************************************************************/
    @Override
    public void onLocationChanged(Location geo) {
        SetDate d = new SetDate();
        String t = d.convertLong(System.currentTimeMillis());

        Log.d("test","1");
        if(geo.getProvider().equals(LocationManager.GPS_PROVIDER)||
                geo.getTime()-lastUpdate>30 ||
                lastProvider.equals(LocationManager.NETWORK_PROVIDER)) { //GPSとNETから同時に位置情報を取得する誤作動を防ぐ
            Log.d("test","2");
            this.location = geo;
            if (geo == null) {
                this.location.setLatitude(0);
                this.location.setLongitude(0);
            }




            SetDate date = new SetDate();
            String tmp = date.convertLong(getLocation().getTime());
            Log.d("GPSTime",tmp);
            Log.d("MYTTime",t);


            //latLongTime.setText("緯度:"+getLocation().getLatitude()+", 経度:"+getLocation().getLongitude()+", GPS時間:"+tmp+",端末時間"+t);


            getLocationUpdateCount++;//位置情報更新回数を+1する
            if(getLocationUpdateCount >= LOCATION_UPDATE_COUNT){//位置情報が5回更新されたタイミングでNiftyから周辺端末のリストを取得し，自身の現在位置情報も送信，その後，ほかのpeerに自身の位置情報を送信(最初は強制的に呼ぶ)
                Log.d("debug","count5の場合");
                getLocationUpdateCount = 0;//カウント回数を初期化
                        //////////////////////////////////////PeerListの更新//////////////////////////////
                        Log.d("debug","PeerListの取得を開始");
                        peerList = nifty.getDataNifty(location, 50000 / 1000);//500---検索範囲   1000---kmをmに変換するために割っている
                        int num =  peerList.size(); //取得したデータ数
                        //自身の情報を削除する
                        for (int i = 0; i < num; i++) {
                            Log.d("debug","peerId:"+peerList.get(i).getString("peerId"));
                            if (peerId.equals(peerList.get(i).getString("peerId"))) {//SkywayIdとはNiftyサーバのフィールドの名前
                                peerList.remove(i);
                                num = num - 1;
                                break;
                            }
                        }
                        Log.d("debug","PeerListの取得を終了");
                        //////////////////////////////////////PeerListの更新//////////////////////////////



                        nifty.setDataNifty(peerId, location);//ここでNiftyサーバに位置情報を送信


                        /////////////////////////////////////他Peerにデータを送信//////////////////////////////
                        p2p.setMyLocation(location);
                        for (int i = 0; i < peerList.size(); i++) {//peerの数
                            final String ptrId = peerList.get(i).getString("peerId");//"SkyWayID"とは通信先のpeerId
                            Log.d("debug","送信先peerId:"+ptrId);
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    p2p.connectPeer(ptrId);//setDataCallbackConnect(dataConnection)を呼ぶ
//                                }
//                            }).start();
                            Thread thread = new Thread(new MyOperationP2P(ptrId));
                            thread.start();
                        }
                        /////////////////////////////////////他Peerにデータを送信//////////////////////////////
                    }
            }else{
                Log.d("debug","count5出ない場合"+getLocationUpdateCount);
                /////////////////////////////////////他Peerにデータを送信//////////////////////////////
                p2p.setMyLocation(location);
                for (int i = 0; i < peerList.size(); i++) {//peerの数
                    final String ptrId = peerList.get(i).getString("peerId");//"SkyWayID"とは通信先のpeerId
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.d("debug","送信先peerId:"+ptrId);
//                            p2p.connectPeer(ptrId);//setDataCallbackConnect(dataConnection)を呼ぶ
//                        }
//                    }).start();
                    Thread thread = new Thread(new MyOperationP2P(ptrId));
                    thread.start();
                }
                /////////////////////////////////////他Peerにデータを送信//////////////////////////////
            }

        lastUpdate=location.getTime();
        lastProvider=location.getProvider();
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("使えるprovider", provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
/********************************************LocationListenerのoverride************************************************************/




@Override
protected void onDestroy() {
    Log.d("確認","onDestroy");
    if (mLocationManager != null) {
        mLocationManager.removeUpdates(this);
    }
    nifty.deleteDataNifty();
    // skyway = new SkyWay(getApplicationContext());
    p2p.destroyPeer();
    super.onDestroy();
}



    public class MyOperationP2P implements Runnable {
        String peerId;
        MyOperationP2P(String peerId){
            this.peerId = peerId;
        }
        public void run ( ) {
            p2p.connectPeer(peerId);
        }
    }


}
