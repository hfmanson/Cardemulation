package nl.mansoft.cardemulation;

import android.content.Intent;
import android.nfc.cardemulation.OffHostApduService;
import android.os.IBinder;
import android.util.Log;

public class MyOffHostApduService extends OffHostApduService {
    public static final String TAG = MyOffHostApduService.class.getSimpleName();
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "OnBind");
        return null;
    }
}
