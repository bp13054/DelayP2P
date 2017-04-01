package delayp2p.com.delayp2p;

/**
 * Created by minet-hp on 2017/03/24.
 */

//AsncTaskでは取得した情報をgetできないのでインターフェースを作成
    public interface AsyncTaskCallbacks{
        public void onTaskFinished(final int delayTime);
    }

