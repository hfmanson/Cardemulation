package nl.mansoft.cardemulation;

import android.os.Bundle;
import android.util.Log;

import nl.mansoft.smartcardio.CommandAPDU;
import nl.mansoft.smartcardio.ResponseAPDU;

public abstract class HostApduService extends android.nfc.cardemulation.HostApduService  {
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

    abstract ResponseAPDU processCommandApdu(CommandAPDU commandAPDU, Bundle bundle);
}
