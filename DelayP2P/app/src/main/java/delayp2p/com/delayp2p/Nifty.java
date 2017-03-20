package delayp2p.com.delayp2p;

/**
 * Created by yusuke on 2016/10/28.
 */

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.nifty.cloud.mb.core.DoneCallback;
import com.nifty.cloud.mb.core.FindCallback;
import com.nifty.cloud.mb.core.NCMB;
import com.nifty.cloud.mb.core.NCMBException;
import com.nifty.cloud.mb.core.NCMBObject;
import com.nifty.cloud.mb.core.NCMBQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yusuke on 2016/09/20.
 */
public class Nifty{

    private NCMBObject obj = new NCMBObject("GPSNifty");
    private NCMBQuery<NCMBObject> query = new NCMBQuery<>("GPSNifty");
    private ArrayList<NCMBObject> dataList = new ArrayList<NCMBObject>(); //自身の周辺にいるユーザ検索結果

    //Niftyのインスタンスを生成
    Nifty(Context context) {
        //ニフティを使うための初期設定
        NCMB.initialize(context, "e027728d4daaafe3cf9605c0f316d43062951e9b2605466983915239006b1ea5", "67686993a6344499972ca0beef25a1dc0bbb45568265824caa92f021f6082dec");
    }





//    //Niftyの初期化
//    public void niftyInitialize() {
//
//    }

    //Niftyから検索範囲のデータを取得(データを一部加工)
    public List<NCMBObject> getDataNifty(Location geo, final double distance) {
        Log.d("位置情報", "検索開始");
        final Location location = geo;
        dataList.clear();
        query.whereWithinKilometers("GeoPoint", location, distance);
        List<NCMBObject> results = null;
        try {
            results = query.find();
        } catch (Exception e) {

        }

        return results;
    }

    public void setDataNifty(String skyWayId, Location location) {

        obj.put("SkyWayID", skyWayId);
        obj.put("GeoPoint", location);

        obj.saveInBackground(new DoneCallback() {
            @Override
            public void done(NCMBException e) {
                if (e != null) {
                    Log.d("位置情報を格納", "失敗");
                } else {
                    Log.d("位置情報を格納", "成功");
                }
            }
        });
    }

    public void setDataNifty(String skyWayId, String roomID) {

        obj.put("SkyWayID", skyWayId);
        obj.put("roomID", roomID);

        obj.saveInBackground(new DoneCallback() {
            @Override
            public void done(NCMBException e) {
                if (e != null) {
                    //保存失敗
                    /*new android.app.AlertDialog.Builder(context)
                            .setTitle("Notification from Nifty")
                            .setMessage("Error:" + e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();*/
                    Log.d("roomID格納", "失敗");

                } else {
                    //保存成功
                    /*new android.app.AlertDialog.Builder(context)
                            .setTitle("Notification from Nifty")
                            .setMessage("Save successfull! with ID:" + obj.getObjectId())
                            .setPositiveButton("OK", null)
                            .show();*/
                    Log.d("roomID格納", "成功");
                }
            }
        });
    }

    public void deleteDataNifty() {
        obj.deleteObjectInBackground(new DoneCallback() {
            @Override
            public void done(NCMBException e) {
                if (e != null) {
                   /* new android.app.AlertDialog.Builder(context)
                            .setTitle("Notification from Nifty")
                            .setMessage("あなたの現在地は格納されていません")
                            .setPositiveButton("OK", null)
                            .show();*/
                    Log.d("位置情報の削除", "失敗");
                } else {
                   /* new android.app.AlertDialog.Builder(context)
                            .setTitle("Notification from Nifty")
                            .setMessage("位置情報削除に成功:")
                            .setPositiveButton("OK", null)
                            .show();*/
                    Log.d("位置情報の削除", "成功");

                }
            }
        });

    }
}

