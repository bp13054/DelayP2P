package delayp2p.com.delayp2p;


import java.util.Date;
import java.util.Calendar;

import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * Created by minet-hp on 2017/03/20.
 */


public class SetDate {

    public String convertLong(long ntpTime){
        Date date = new Date(ntpTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
        String tmp = sdf.format(date);
        return tmp;
    }
}
