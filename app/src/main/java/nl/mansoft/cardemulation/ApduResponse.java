package nl.mansoft.cardemulation;

import java.util.Arrays;

public class ApduResponse {
    private byte[] mResponse;
    private int mOffset;
    private int mMaxResponseLength;

    ApduResponse(byte[] response, int maxResponseLength) {
        mResponse = response;
        mMaxResponseLength = maxResponseLength;
        mOffset = 0;
    }

    public byte[] getResponse() {
        byte[] result = null;
        if (mResponse.length - mOffset > mMaxResponseLength + 2) {
            result = Arrays.copyOfRange(mResponse, mOffset, mOffset + mMaxResponseLength + 2);
            result[result.length - 2] = (byte) 0x61;
            result[result.length - 1] = (byte) 0x00;
            mOffset += mMaxResponseLength;
        } else {
            result = Arrays.copyOfRange(mResponse, mOffset, mResponse.length);
        }
        return result;
    }
}
