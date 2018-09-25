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

import android.nfc.cardemulation.HostApduService;
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
    private static final String TAG = "CardService";
    private SmartcardIO mSmartcardIO;
    private ApduResponse mApduResponse;
    public final int MAX_RESPONSE_SIZE = 0xF0;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mSmartcardIO = new SmartcardIO();
            mSmartcardIO.setup(getBaseContext(), new SEService.CallBack() {
                @Override
                public void serviceConnected(SEService seService) {
                    try {
                        mSmartcardIO.setSession();
                    } catch (Exception e) {
                        Log.e(TAG, "Error: " + e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * Determine if commandApdu is a SELECT AID command
     * @param commandApdu
     * @return aid if command is SELECT AID, null otherwise
     */
    private byte[] testSelectApdu(byte[] commandApdu) {
        byte[] aid = null;
        if (commandApdu[0] == 0x00 && commandApdu[1] == SmartcardIO.INS_SELECT && commandApdu[2] == 0x04) {
            int aidLength = commandApdu[4] & 0xff;
            aid = new byte[aidLength];
            System.arraycopy(commandApdu, 5, aid, 0, aidLength);
        }
        return aid;
    }

    /**
     *  Select aid on the SIM card
     * @param aid
     * @return result of SIM SELECT command
     */
    private byte[] selectAid(byte[] aid) {
        byte[] result = null;
        Log.d(TAG, "Selecting AID: " + ByteArrayToHexString(aid));
        try {
            mSmartcardIO.closeChannel();
            result = mSmartcardIO.openChannel(aid);
        } catch (Exception e) {
            Log.e(TAG, "Error opening channel: " + e.getMessage());
        }
        return result;
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
     * @param commandApdu The APDU that received from the remote device
     * @param extras A bundle containing extra data. May be null.
     * @return a byte-array containing the response APDU, or null if no response APDU can be sent
     * at this point.
     */
    // BEGIN_INCLUDE(processCommandApdu)
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.i(TAG, "Received APDU: " + ByteArrayToHexString(commandApdu));
        // If the APDU matches the SELECT AID command for this service,
        // send the loyalty card account number, followed by a SELECT_OK status trailer (0x9000).
        byte[] result = null;
        byte[] aid = testSelectApdu(commandApdu);
        if (aid != null) {
            result = selectAid(aid);
        } else {
            try {
                if (commandApdu[1] == (byte) 0xc0) {
                    Log.i(TAG, "Got GET RESPONSE");
                    result = mApduResponse.getResponse();
                } else {
                    ResponseAPDU responseAPDU = mSmartcardIO.runAPDU(new CommandAPDU(commandApdu));
                    byte[] data = responseAPDU.getData();
                    Log.i(TAG, "Response from SIM: " + ByteArrayToHexString(data));
                    mApduResponse = new ApduResponse(data, MAX_RESPONSE_SIZE);
                    result = mApduResponse.getResponse();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "Response APDU: " + ByteArrayToHexString(result));

        return result;
    }
    // END_INCLUDE(processCommandApdu)
}
