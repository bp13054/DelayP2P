package delayp2p.com.delayp2p;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;



public class SntpTimeUpdateAsyncTask extends AsyncTask<String, String, Integer> {

    private static final int RET_ERROR_SNTP = 0xFFFFFFFF;
    private AsyncTaskCallbacks callback = null;


    // コンストラクタ
    public SntpTimeUpdateAsyncTask(AsyncTaskCallbacks callback) {
        this.callback = callback;//MainActivity側のAsnycTaskCallback情報を取得
    }


    /**
     * 更新前処理
     */
    @Override
    protected void onPreExecute() {
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
        for(int count = 1;count<=10;count++) {
            if (sntp.requestTime(url, 1000)) {
                long ntpNow = sntp.getNtpTime() + SystemClock.elapsedRealtime() - sntp.getNtpTimeReference();
                long localNow = Calendar.getInstance().getTime().getTime();
                result = result + (int) (localNow - ntpNow);

            } else {
                result = RET_ERROR_SNTP;
                break;
            }
        }
        return result/10;//十回計測した平均
    }

    /**
     * 完了処理
     * @param result 現在時刻とNTP時刻との差分（ミリ秒）＋の場合は端末側が進んでいる、-の場合は端末側が遅れている
     */
    @Override
    protected void onPostExecute(Integer result) {
        if (result != RET_ERROR_SNTP)
        {
            callback.onTaskFinished(result);//MainActivityに値を返却

        }
        else
        {

        }
    }
}