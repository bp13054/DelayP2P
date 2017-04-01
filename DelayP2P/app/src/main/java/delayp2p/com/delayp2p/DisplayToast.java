package delayp2p.com.delayp2p;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by yusuke on 2016/11/03.
 */

//トーストを表示するクラス
public class DisplayToast {
    private Context context;

    DisplayToast(Context context) {
        this.context = context;
    }

    public void toast(final String peerId, final String locationTime, final String getTime) {


        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,"送信元peerId:"+peerId+"\n"+"送信元位置取得時間:\n"+locationTime+"\n"+"受信元位置取得時間:\n"+getTime, Toast.LENGTH_LONG).show();
            }
        });
    }
}
