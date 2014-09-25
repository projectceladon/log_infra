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

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utility class for checking whether the prerequisite "Intel Commons" application is
 * installed and compatible.
 */
public final class IntelCommonsApp
{
    /**
     * Do not allow instantiation of this static utility class.
     */
    private IntelCommonsApp() {}

    /**
     * Checks whether the prerequisite "Intel Commons" application is installed on the device.
     */
    public static boolean isInstalled(Context context)
    {
        return IntelCommonsApp.isMinimumVersion(context, 0, 0);
    }

    /**
     * Checks whether a specific minimum version of the prerequisite "Intel Commons" application
     * is installed on the device.
     */
    public static boolean isMinimumVersion(Context context, int major, int minor)
    {
        try
        {
            final Pattern pattern = Pattern.compile("(\\d+)\\D(\\d+)\\D(\\d+)");
            Matcher matcher = pattern.matcher(IntelCommonsApp.getVersion(context));
            matcher.find();
            return (major <= Integer.parseInt(matcher.group(1)) &&
                    minor <= Integer.parseInt(matcher.group(2)));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Returns the reported version of the prerequisite "Intel Commons" application, or
     * {@code null} if not installed.
     */
    public static String getVersion(Context context)
    {
        try
        {
            return context.getPackageManager().getPackageInfo("com.intel.commons.app", 0)
                    .versionName;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
