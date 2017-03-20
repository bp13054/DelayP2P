package delayp2p.com.delayp2p;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;

/**
 * P2P通信で位置情報を送受信する際の通信遅延を測定するアプリ
 * @author Shimomura Yusuke
 */

public class MainActivity extends AppCompatActivity implements LocationListener {
    final private static int LOCATION_UPDATE_COUNT = 5; //位置情報を5回取得したらNiftyに現在位置を登録．また，周辺ユーザ検索も実施
    final private static int REQUEST_PERMISSION = 1000;

    //private Nifty nifty; //Nifty Cloudを使うクラス
    //private SkyWay skyway; //SkyWayを使うクラス

    private Location location = new Location(""); //自身の位置情報を保存するのに用いる
    private LocationManager mLocationManager;//位置情報を取得するのに用いる
    private long lastUpdate=0; //GPSとNETから重複して位置情報を取得してないか確認するのに用いる
    private String lastProvider="";//同様


    private long localTime;
    private long serverTime;


    private TextView latLongTime;

    private int getLocationUpdateCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        nifty = new Nifty(this.getApplicationContext());
//        skyway = new SkyWay(getApplicationContext());
//        skyway.setNifty(nifty);
//        skyway.setActivity(this);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);




        //実験を開始する
        latLongTime = (TextView)findViewById(R.id.latlongtime) ;
        Button measurment = (Button) findViewById(R.id.measurment);
        measurment.setOnClickListener(new View.OnClickListener() {
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
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, this);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, this);
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
        //Log.d("端末Time",t);
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
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                }
//            }).start();

            SntpTimeUpdateAsyncTask task = new SntpTimeUpdateAsyncTask();
            task.execute();

            SetDate date = new SetDate();
            String tmp = date.convertLong(getLocation().getTime());
            Log.d("GPSTime",tmp);
Log.d("MYTTime",t);


            latLongTime.setText("緯度:"+getLocation().getLatitude()+", 経度:"+getLocation().getLongitude()+", GPS時間:"+tmp+",端末時間"+t);
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






    public void sendLocationWithP2P(){

    }

}
