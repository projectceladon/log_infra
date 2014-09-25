/*
 *  INTEL CONFIDENTIAL
 *
 *  Copyright (C) 2012-2013, Intel Corporation. All Rights Reserved.
 *
 *  The source code contained or described herein and all documents related to the source code
 *  ("Material") are owned by Intel Corporation or its suppliers or licensors. Title to the Material
 *  remains with Intel Corporation or its suppliers and licensors. The Material contains trade
 *  secrets and proprietary and confidential information of Intel or its suppliers and licensors.
 *  The Material is protected by worldwide copyright and trade secret laws and treaty provisions.
 *  No part of the Material may be used, copied, reproduced, modified, published, uploaded, posted,
 *  transmitted, distributed, or disclosed in any way without Intel`s prior express written
 *  permission.
 *
 *  No license under any patent, copyright, trade secret or other intellectual property right is
 *  granted to or conferred upon you by disclosure or delivery of the Materials, either expressly,
 *  by implication, inducement, estoppel or otherwise. Any license under such intellectual property
 *  rights must be express and approved by Intel in writing.
 */

package com.intel.commons;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Static utility class for helper methods concerning the "Cisco AnyConnect"
 * Virtual Private Network (VPN) client.
 * <p>
 * @see http://www.cisco.com/en/US/docs/security/vpn_client/anyconnect/anyconnect30/administration/guide/acmobiledevices.html
 */
public final class Vpn
{
    /**
     * Do not allow instantiation of this static utility class.
     */
    private Vpn() {}

    /**
     * Checks whether the "Cisco AnyConnect" VPN client is installed on the device.
     */
    public static boolean isInstalled(Context context)
    {
        try
        {
            context.getPackageManager().getPackageInfo("com.cisco.anyconnect.vpn.android.avf", 0);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Checks whether the "Cisco AnyConnect" VPN client is currently connected to a gateway server.
     * <p>
     * TODO: implementation + documentation, or remove this method
     */
    public static boolean isConnected()
    {
        return false;
    }

    /**
     * Safely triggers the "Cisco AnyConnect" VPN client to connect automatically to its default
     * gateway server.
     * <p>
     * Note that this method's usability depends on the "External Control" setting in the
     * "Cisco AnyConnect" VPN client.
     * <p>
     * Either way, this method does not return any result!
     *
     * @param context the caller's context
     *
     * @return {@code true} if an only if the "Cisco AnyConnect" VPN client is installed
     */
    public static boolean connect(Activity context)
    {
        try
        {
            final Uri uri = Uri.parse("anyconnect:connect");
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Safely triggers the "Cisco AnyConnect" VPN client to disconnect automatically from its
     * default gateway server.
     * <p>
     * Note that this method's usability depends on the "External Control" setting in the
     * "Cisco AnyConnect" VPN client.
     * <p>
     * Either way, this method does not return any result!
     *
     * @param context the caller's context
     *
     * @return {@code true} if an only if the "Cisco AnyConnect" VPN client is installed
     */
    public static boolean disconnect(Activity context)
    {
        try
        {
            final Uri uri = Uri.parse("anyconnect:disconnect");
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Safely displays the connection screen of the "Cisco AnyConnect" VPN client.
     *
     * @param context the caller's context
     *
     * @return {@code true} if an only if the "Cisco AnyConnect" VPN client is installed
     */
    public static boolean startActivity(Activity context)
    {
        try
        {
            context.startActivity(new Intent(
                    "com.cisco.anyconnect.vpn.android.CONNECTION_ACTIVITY_ACTION_CONNECT_INTENT"));
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
