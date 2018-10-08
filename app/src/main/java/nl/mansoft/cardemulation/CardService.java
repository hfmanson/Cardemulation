/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.mansoft.cardemulation;

import android.os.Bundle;
import android.util.Log;

import org.simalliance.openmobileapi.SEService;

import java.io.IOException;

import nl.mansoft.isoappletprovider.SmartcardIO;
import nl.mansoft.smartcardio.CommandAPDU;
import nl.mansoft.smartcardio.ResponseAPDU;

import static nl.mansoft.isoappletprovider.Util.ByteArrayToHexString;

/**
 * This is a sample APDU Service which demonstrates how to interface with the card emulation support
 * added in Android 4.4, KitKat.
 *
 */

public class CardService extends HostApduService {
    private static final String TAG = CardService.class.getSimpleName();
    public static final int INS_SELECT = 0xA4;
    private SmartcardIO mSmartcardIO;

    @Override
    public void onCreate() {
        super.onCreate();
        mSmartcardIO = new SmartcardIO(this, null, null);
        mSmartcardIO.mDebug = true;
    }

    /**
     * Called if the connection to the NFC card is lost, in order to let the application know the
     * cause for the disconnection (either a lost link, or another AID being selected by the
     * reader).
     *
     * @param reason Either DEACTIVATION_LINK_LOSS or DEACTIVATION_DESELECTED
     */
    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "onDeactivated: reason = " + reason);
        //mSmartcardIO.closeChannel();
        mSmartcardIO.teardown();
    }

    /**
     * Determine if commandAPDU is a SELECT AID command
     * @param commandAPDU
     * @return aid if command is SELECT AID, null otherwise
     */
    private byte[] testSelectApdu(CommandAPDU commandAPDU) {
        byte[] aid = null;
        if (commandAPDU.getCLA() == 0x00 && commandAPDU.getINS() == INS_SELECT && commandAPDU.getP1() == 0x04) {
            int aidLength = commandAPDU.getNc();
            aid = new byte[aidLength];
            System.arraycopy(commandAPDU.getData(), 0, aid, 0, aidLength);
        }
        return aid;
    }

    /**
     *  Select aid on the SIM card
     * @param aid
     * @return result of SIM SELECT command
     */
    private ResponseAPDU selectAid(byte[] aid) {
        byte[] result = null;
        Log.d(TAG, "Selecting AID: " + ByteArrayToHexString(aid));
        try {
            mSmartcardIO.closeChannel();
            result = mSmartcardIO.openChannel(aid);
        } catch (Exception e) {
            Log.e(TAG, "Error opening channel: " + e.getMessage());
        }
        return result == null ? null : new ResponseAPDU(result);
    }

    /**
     * This method will be called when a command APDU has been received from a remote device. A
     * response APDU can be provided directly by returning a byte-array in this method. In general
     * response APDUs must be sent as quickly as possible, given the fact that the user is likely
     * holding his device over an NFC reader when this method is called.
     *
     * <p class="note">If there are multiple services that have registered for the same AIDs in
     * their meta-data entry, you will only get called if the user has explicitly selected your
     * service, either as a default or just for the next tap.
     *
     * <p class="note">This method is running on the main thread of your application. If you
     * cannot return a response APDU immediately, return null and use the {@link
     * #sendResponseApdu(byte[])} method later.
     *
     * @param commandAPDU The APDU that received from the remote device
     * @param bundle A bundle containing extra data. May be null.
     * @return The response APDU, or null if no response APDU can be sent
     * at this point.
     */
    // BEGIN_INCLUDE(processCommandApdu)

    @Override
    ResponseAPDU processCommandApdu(CommandAPDU commandAPDU, Bundle bundle) {
        ResponseAPDU responseAPDU = null;
        Log.i(TAG, "Received APDU: " + ByteArrayToHexString(commandAPDU.getBytes()));
        byte[] aid = testSelectApdu(commandAPDU);
        if (aid != null) {
            responseAPDU = selectAid(aid);
        } else {
            try {
                mSmartcardIO.runAPDU(commandAPDU, this);
                Log.i(TAG, "Response from SIM");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (responseAPDU == null) {
            Log.i(TAG, "Response APDU is null ");
        } else {
            byte[] bytes = responseAPDU.getBytes();
            Log.i(TAG, "Response APDU: " + (bytes == null ? "(null)" : ByteArrayToHexString(bytes)));
        }
        return responseAPDU;
    }
    // END_INCLUDE(processCommandApdu)
}

