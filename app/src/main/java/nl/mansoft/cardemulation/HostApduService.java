package nl.mansoft.cardemulation;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import nl.mansoft.isoappletprovider.TransmitCallback;
import nl.mansoft.smartcardio.CommandAPDU;
import nl.mansoft.smartcardio.ResponseAPDU;

import static nl.mansoft.isoappletprovider.Util.ByteArrayToHexString;

public abstract class HostApduService extends android.nfc.cardemulation.HostApduService implements TransmitCallback {
    private static final String TAG = HostApduService.class.getSimpleName();
    private static final long FIRST_INSTALL_TIME_S4 = 1388616505000L;
    private ApduResponse mApduResponse;
    private long mFirstInstallTime;
    public final int MAX_RESPONSE_SIZE = 0xF0;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo("org.simalliance.openmobileapi.service", 0);
            mFirstInstallTime = packageInfo.firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {
            mFirstInstallTime = 0L;
        }
    }

    private boolean isSamsungS4() {
        return mFirstInstallTime == FIRST_INSTALL_TIME_S4;
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle bundle) {
        CommandAPDU commandAPDU = new CommandAPDU(commandApdu);

        byte[] result = null;
        if (commandAPDU.getINS() == 0xC0) {
            Log.i(TAG, "Got GET RESPONSE");
            result = mApduResponse.getResponse();
        } else {
            ResponseAPDU responseAPDU = processCommandApdu(commandAPDU, bundle);
            if (responseAPDU != null) {
                if (isSamsungS4()) {
                    result = responseAPDU.getBytes();
                } else {
                    mApduResponse = new ApduResponse(responseAPDU.getBytes(), MAX_RESPONSE_SIZE);
                    result = mApduResponse.getResponse();
                }
            }
        }
        Log.i(TAG, "Response APDU: " + (result == null ? "(null)" : ByteArrayToHexString(result)));
        return result;
    }

    @Override
    public void callBack(ResponseAPDU responseAPDU) {
        byte[] bytes = null;
        if (isSamsungS4()) {
            bytes = responseAPDU.getBytes();
            if (responseAPDU.getSW1() == 0x61) {
                bytes[bytes.length - 2] = (byte) 0x90;
                bytes[bytes.length - 1] = (byte) 0x00;
            }
        } else {
            mApduResponse = new ApduResponse(responseAPDU.getBytes(), MAX_RESPONSE_SIZE);
            bytes = mApduResponse.getResponse();
        }
        Log.i(TAG, "Response APDU (async): " + (bytes == null ? "(null)" : ByteArrayToHexString(bytes)));
        sendResponseApdu(bytes);
    }

    abstract ResponseAPDU processCommandApdu(CommandAPDU commandAPDU, Bundle bundle);
}
