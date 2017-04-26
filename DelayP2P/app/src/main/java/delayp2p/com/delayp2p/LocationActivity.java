package delayp2p.com.delayp2p;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.nifty.cloud.mb.core.NCMBObject;

import java.util.List;


public class LocationActivity extends AppCompatActivity implements LocationListener {
    final private static int LOCATION_UPDATE_COUNT = 5; //位置情報を5回取得したらNiftyに現在位置を登録．また，周辺ユーザ検索も実施
    final private static int MAX_UPDATE = 60;//位置情報を60回取得したら位置情報を取得することを終了する
    final private static int REQUEST_PERMISSION = 1000;
    private Nifty nifty; //Nifty Cloudを使うクラス
    private P2PVer2 p2p; //SkyWayを使うクラス
    private int delayTime = 0;//初期値
    private String peerId = "unSetPeerId";//初期値
    private int getLocationUpdateCount = 4;//位置情報のカウント回数を記録 5回で初期化
    private int LocationCounter = 0;
    private static List<NCMBObject> peerList;
    private Location location = new Location(""); //自身の位置情報を保存するのに用いる
    private TextView latLongTime;
    private TextView gosa;
    private MyLocation myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        nifty = new Nifty(this.getApplicationContext());
        myLocation = new MyLocation(getApplicationContext(), this);


        Intent intent = getIntent();
        delayTime = intent.getIntExtra("Delay", 0);
        peerId = intent.getStringExtra("peerId");
        //Log.d("端末の時間誤差とID", "時間誤差:" + delayTime + "ID" + peerId);
        p2p = new P2PVer2(this.getApplicationContext(), delayTime, peerId);


        //Peerクラスに自身のidを登録し、Peerクラスを生成
        p2p.createPeer();


        //実験を開始する
        latLongTime = (TextView) findViewById(R.id.latlongtime);
        gosa = (TextView) findViewById(R.id.textView2);
        gosa.setText("端末の誤差は" + delayTime);
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
    public void LocationPermissionCheck() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, REQUEST_PERMISSION);
            } else {
                myLocation.createGoogleApiClient();//android6未満，または，すでにロケーションの許可がある
            }
        } else {
            myLocation.createGoogleApiClient();//android6未満，または，すでにロケーションの許可がある
        }
    }


    // パーミッション(LocationPermissionCheck()のコールバック)の結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                myLocation.createGoogleApiClient();
                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "許可がないためデータの取得ができません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


    /********************************************LocationListenerのoverride************************************************************/
    @Override
    public void onLocationChanged(Location geo) {
        this.location = geo;
        if (geo == null) {
            this.location.setLatitude(0);
            this.location.setLongitude(0);
        }
        LocationCounter++;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                latLongTime.setText("位置情報の取得回数:" + LocationCounter);
            }
        });
        getLocationUpdateCount++;//位置情報更新回数を+1する.....................................................................................................................................................................................................................................
        if (getLocationUpdateCount == LOCATION_UPDATE_COUNT) {//位置情報が5回更新されたタイミングでNiftyから周辺端末のリストを取得し，自身の現在位置情報も送信，その後，ほかのpeerに自身の位置情報を送信(最初は強制的に呼ぶ)
            getLocationUpdateCount = 0;//カウント回数を初期化
            System.gc();//メモリの初期化


            //////////////////////////////////////PeerListの更新//////////////////////////////
            peerList = nifty.getDataNifty(location, 50);//500---検索範囲   1000---kmをmに変換するために割っている
            while (true) {//peerListを取得するまで繰り返す
                if (peerList != null)
                    break;
            }
            int num = peerList.size(); //取得したデータ数
            //自身の情報を削除する
            for (int i = 0; i < num; i++) {
                if (peerId.equals(peerList.get(i).getString("peerId"))) {//SkywayIdとはNiftyサーバのフィールドの名前
                    peerList.remove(i);
                    break;
                }
            }
            //////////////////////////////////////PeerListの更新//////////////////////////////


            nifty.setDataNifty(peerId, location);//ここでNiftyサーバに位置情報を送信
            p2p.connectPeer(peerList);


//            SetDate d = new SetDate();
//            String time = d.convertLong(location.getTime());
            p2p.send(location,"yes");
        } else {
//            SetDate d = new SetDate();
//            String time = d.convertLong(location.getTime());
            p2p.send(location,"");
        }
    }

    /********************************************LocationListenerのoverride************************************************************/

    @Override
    protected void onDestroy() {
        Log.d("確認", "onDestroy");
        LocationServices.FusedLocationApi.removeLocationUpdates(myLocation.getGoogleApiClient(), this);
        nifty.deleteDataNifty();
        p2p.closeData();
        p2p.close();
        super.onDestroy();
        p2p.getFileInput().fileClose();
        p2p.getRecieveFileInput().fileClose();
    }

    protected void onStart() {
        super.onStart();
    }
}
