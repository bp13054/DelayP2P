package delayp2p.com.delayp2p;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * P2P通信で位置情報を送受信する際の通信遅延を測定するアプリ
 * @author Shimomura Yusuke
 */

public class MainActivity extends AppCompatActivity implements AsyncTaskCallbacks {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SntpTimeUpdateAsyncTask task = new SntpTimeUpdateAsyncTask(this); //***AsyncTskは一度使うたびにnewする必要がある

        //実験を開始する
        Button measurment = (Button) findViewById(R.id.measurment);
        measurment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.execute();
            }
        });
    }

        @Override
        public void onTaskFinished ( int delayTime){
            Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
            intent.putExtra("Delay", delayTime);//端末の時間誤差を送信
            EditText editText = (EditText) findViewById(R.id.peerIdName);
            String tp = editText.getText().toString();
            intent.putExtra("peerId",tp);//PeerIdを送信
            startActivity(intent);
        }
}
