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
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.text.format.DateUtils;

/**
 * Static utility class for displaying a standard Intel-internal splash screen.
 */
public final class SplashScreen
{
    public static final String ACTION = "com.intel.commons.action.SPLASH";

    public static final String EXTRA_TITLE        = "com.intel.commons.extra.TITLE";
    public static final String EXTRA_TIME_MS      = "com.intel.commons.extra.TIME_MS";
    public static final String EXTRA_ALLOW_CANCEL = "com.intel.commons.extra.ALLOW_CANCEL";

    public static final long DEFAULT_TIMEOUT = 5 * DateUtils.SECOND_IN_MILLIS;

    /**
     * Do not allow instantiation of this static utility class.
     */
    private SplashScreen() {}

    /**
     * Displays a standard Intel-internal splash screen.
     * The result is returned to {@link Activity#onActivityResult(int, int, Intent)}:
     * {@code resultCode} = {@link Activity#RESULT_OK} (timer expired), or
     * {@link Activity#RESULT_CANCELED} (dismissed by the user).
     *
     * For safety, always call this method within a {@code try} block, in case the "Intel Commons"
     * app is not installed or not compatible with your app.
     *
     * @param context      the caller's context
     * @param requestCode  if >= 0, this code will be returned in onActivityResult()
     * @param title        your app's name or title
     * @param allowCancel  allow the user to dismiss the screen?
     *
     * @throws ActivityNotFoundException if the "Intel Commons" app is not installed, or the
     *                                   calling app is signed with an incompatible certificate
     */
    public static void startActivity(Activity context, int requestCode, String title,
            boolean allowCancel) throws ActivityNotFoundException
    {
        SplashScreen.startActivity(context, requestCode, title, SplashScreen.DEFAULT_TIMEOUT,
                allowCancel);
    }

    /**
     * Displays a standard Intel-internal splash screen.
     * The result is returned to {@link Activity#onActivityResult(int, int, Intent)}:
     * {@code resultCode} = {@link Activity#RESULT_OK} (timer expired), or
     * {@link Activity#RESULT_CANCELED} (dismissed by the user).
     *
     * For safety, always call this method within a {@code try} block, in case the "Intel Commons"
     * app is not installed or not compatible with your app.
     *
     * @param context      the caller's context
     * @param requestCode  if >= 0, this code will be returned in onActivityResult()
     * @param title        your app's name or title
     * @param milliseconds maximum time for the splash screen to be displayed
     * @param allowCancel  allow the user to dismiss the screen?
     *
     * @throws ActivityNotFoundException if the "Intel Commons" app is not installed, or the
     *                                   calling app is signed with an incompatible certificate
     */
    public static void startActivity(Activity context, int requestCode, String title,
            long milliseconds, boolean allowCancel) throws ActivityNotFoundException
    {
        final Intent intent = new Intent(SplashScreen.ACTION)
                .putExtra(SplashScreen.EXTRA_TITLE, title)
                .putExtra(SplashScreen.EXTRA_TIME_MS, milliseconds)
                .putExtra(SplashScreen.EXTRA_ALLOW_CANCEL, allowCancel);

        context.startActivityForResult(intent, requestCode);
    }
}
