package nl.mansoft.cardemulation;

import android.content.ComponentName;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class MainActivity extends ActionBarActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private CardEmulation mCardEmulation;
    private ComponentName mOffHostService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOffHostService = new ComponentName("nl.mansoft.cardemulation","nl.mansoft.cardemulation.MyOffHostApduService");
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mCardEmulation = CardEmulation.getInstance(nfcAdapter);
//        boolean b = mCardEmulation.categoryAllowsForegroundPreference(CardEmulation.CATEGORY_OTHER);
//        Log.d(TAG, "categoryAllowsForegroundPreference: " + b);
//        b = mCardEmulation.isDefaultServiceForAid(mOffHostService, "01020304050601");
//        Log.d(TAG, "isDefaultServiceForAid: " + b);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mCardEmulation.setPreferredService(this, mOffHostService);
    }
}
