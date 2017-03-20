package delayp2p.com.delayp2p;

/**
 * Created by yusuke on 2017/03/19.
 */

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;

/**
 * NTP時刻非同期取得タスク
 */
public class SntpTimeUpdateAsyncTask extends AsyncTask<String, String, Integer> {
    private static final int RET_ERROR_SNTP = 0xFFFFFFFF;
    long ntpNow = 0;
    /**
     * 更新前処理
     */
    @Override
    protected void onPreExecute() {

        //m_ntpSabun = 0;
    }

    /**
     * 処理開始
     * 空文字を指定するとすべてのデータを取得する
     * @param text NTPサーバーURL
     * @return NTPサーバーとの差分時間（ミリ秒）
     */
    @Override
    protected Integer doInBackground(String... text) {
        String url = "clock.nc.fukuoka-u.ac.jp";

        SntpClient sntp = new SntpClient();
        int result = RET_ERROR_SNTP;
        if (sntp.requestTime(url, 100))
        {
            //getNtpTime:NTPサーバから得られた時間

             ntpNow = sntp.getNtpTime() + SystemClock.elapsedRealtime() - sntp.getNtpTimeReference();
            //ntpNow = sntp.getNtpTime();

            long localNow = Calendar.getInstance().getTime().getTime();
            result = (int)ntpNow;
            //result = (int)(localNow - ntpNow);
            //PSDebug.d("NT時刻取得成功 差分時刻=" + result);
        }
        else
        {
           Log.d("ntpTime","NTP時刻取得失敗");
            result = RET_ERROR_SNTP;
        }
        return result;
    }

    /**
     * 完了処理
     * @param result 現在時刻とNTP時刻との差分（ミリ秒）＋の場合は端末側が進んでいる、-の場合は端末側が遅れている
     */
    @Override
    protected void onPostExecute(Integer result) {
        SetDate date = new SetDate();
        String tmp = date.convertLong(ntpNow);
        Log.d("ntpTime",tmp);

//        if (result != RET_ERROR_SNTP)
//        {
//            m_ntpSabun = result;
//            updateSabunTextView(0 - m_ntpSabun, m_ntpTextView, "NTP補正時間：");
//        }
//        else
//        {
//            m_ntpTextView.setText("NTPサーバー時刻取得失敗");
//        }
    }
}

