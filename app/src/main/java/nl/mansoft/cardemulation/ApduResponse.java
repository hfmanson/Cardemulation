package nl.mansoft.cardemulation;

import java.util.Arrays;

public class ApduResponse {
    private byte[] mResponse;
    private int offset;
    private int mMaxResponseLength;

    ApduResponse(byte[] response, int maxResponseLength) {
        mResponse = response;
        mMaxResponseLength = maxResponseLength;
        offset = 0;
    }

    public byte[] getResponse() {
        byte[] result = null;
        if (mResponse.length - offset > mMaxResponseLength) {
            result = Arrays.copyOfRange(mResponse, offset, offset + mMaxResponseLength + 2);
            result[result.length - 2] = (byte) 0x61;
            result[result.length - 1] = (byte) 0x00;
            offset += mMaxResponseLength;
        } else {
            result = Arrays.copyOfRange(mResponse, offset, mResponse.length + 2);
            result[result.length - 2] = (byte) 0x90;
            result[result.length - 1] = (byte) 0x00;
        }
        return result;
    }
}
