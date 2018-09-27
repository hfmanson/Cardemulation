package nl.mansoft.cardemulation;

import android.os.Bundle;
import android.util.Log;

import nl.mansoft.isoappletprovider.TransmitCallback;
import nl.mansoft.smartcardio.CommandAPDU;
import nl.mansoft.smartcardio.ResponseAPDU;

import static nl.mansoft.isoappletprovider.Util.ByteArrayToHexString;

public abstract class HostApduService extends android.nfc.cardemulation.HostApduService implements TransmitCallback {
    private static final String TAG = HostApduService.class.getSimpleName();
    private ApduResponse mApduResponse;
    public final int MAX_RESPONSE_SIZE = 0xF0;

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
                mApduResponse = new ApduResponse(responseAPDU.getData(), MAX_RESPONSE_SIZE);
                result = mApduResponse.getResponse();
            }
        }
        return result;
    }
    @Override
    public void callBack(ResponseAPDU responseAPDU) {
        mApduResponse = new ApduResponse(responseAPDU.getData(), MAX_RESPONSE_SIZE);
        byte[] bytes = mApduResponse.getResponse();
        Log.i(TAG, "Response APDU (async): " + (bytes == null ? "(null)" : ByteArrayToHexString(bytes)));
        sendResponseApdu(bytes);
    }

    abstract ResponseAPDU processCommandApdu(CommandAPDU commandAPDU, Bundle bundle);
}
