package delayp2p.com.delayp2p;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;



public class MyLocation implements LocationListener {

    private Location location = new Location("");
    private String peerId = "aa";
    private Nifty nifty;
    private LocationManager mLocationManager;
    long lastUpdate=0;
    String lastProvider="";

    MyLocation(Context context, Nifty nifty) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.nifty = nifty;

    }

    public void requestLocationUpdates() {


        if (mLocationManager!=null) {
            try {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, this);
            }catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }
        }

    }

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

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public void onStart() {

    }


    public void onResume() {
        requestLocationUpdates();
    }


    public void onPause() {

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }


    public void onStop() {

    }


    @Override
    public void onLocationChanged(Location geo) {
        if(geo.getProvider().equals(LocationManager.GPS_PROVIDER)||
                geo.getTime()-lastUpdate>30 ||
               lastProvider.equals(LocationManager.NETWORK_PROVIDER)) {
            this.location = geo;
            if (geo == null) {
                this.location.setLatitude(0);
                this.location.setLongitude(0);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // マルチスレッドにしたい処理 ここから
                    if (!peerId.equals("aa"))
                        nifty.setDataNifty(peerId, location);
                    // マルチスレッドにしたい処理 ここまで
                }
            }).start();


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


}