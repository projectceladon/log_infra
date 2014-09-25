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
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Static utility class for device and user identity helper methods.
 */
public final class Identity
{
    /**
     * Do not allow instantiation of this static utility class.
     */
    private Identity() {}

    public static final String LOGIN_ACTION = "com.intel.commons.action.LOGIN";

    public static final String LOGIN_EXTRA_IN_TITLE       = "com.intel.commons.extra.TITLE";
    public static final String LOGIN_EXTRA_IN_HIDE_DOMAIN = "com.intel.commons.extra.HIDE_DOMAIN";
    public static final String LOGIN_EXTRA_IN_FORCE_GUI   = "com.intel.commons.extra.FORCE_GUI";

    public static final String LOGIN_EXTRA_OUT_USERNAME = "com.intel.commons.extra.USERNAME";
    public static final String LOGIN_EXTRA_OUT_PASSWORD = "com.intel.commons.extra.PASSWORD";
    public static final String LOGIN_EXTRA_OUT_DOMAIN   = "com.intel.commons.extra.DOMAIN";

    /**
     * Used in {@link #isValidIntelEmailAddress(CharSequence)}.
     * 
     * @see android.util.Patterns#EMAIL_ADDRESS
     * @see http://www.regular-expressions.info/email.html
     * @see http://www.rfc-editor.org/errata_search.php?rfc=3696&eid=1690
     */
    private static final Pattern EMAIL_ADDRESS = Pattern.compile(
            "^[a-z0-9\\+\\.\\_\\%\\-\\+]{1,64}\\@intel.com$", Pattern.CASE_INSENSITIVE);

    /**
     * Returns a 128-bit <a href="http://en.wikipedia.org/wiki/Universally_unique_identifier">
     * Universally Unique Identifier</a> (UUID), which remains constant for the lifetime of the
     * device, but changes if a factory reset is performed on the device.
     * <p>
     * The implementation is based on {@link android.provider.Settings.Secure.ANDROID_ID}, but
     * hashes the value instead of exposing it in clear-text, for privacy reasons. Also note that
     * all Android emulators have the same {@link android.provider.Settings.Secure.ANDROID_ID}.
     *
     * @param context the caller's context
     *
     * @return valid {@link UUID} object
     *
     * @throws NullPointerException if the input or the device's ANDROID_ID are invalid
     */
    public static UUID getDeviceIdentity(Context context)
    {
        // http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
        // http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
        return UUID.nameUUIDFromBytes(Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID).getBytes());
    }

    /**
     * Returns all the Intel email addresses provisioned on the device (0 or more).
     *
     * @param context the caller's context
     *
     * @return 0 or more {@link String}s, but never {@code null}
     */
    public static String[] getIntelEmailAddresses(Context context)
    {
        final ArrayList<String> results = new ArrayList<String>();
        Cursor cursor = null;
        try
        {
            final Uri uri = Uri.parse("content://com.intel.commons.provider.IntelEmailAddresses/");
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            while (cursor.moveToNext())
            {
                results.add(cursor.getString(0));
            }
        }
        catch (Exception e)
        {
            // Silent failover, do nothing
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close(); // Either way, cleanup
            }
        }
        return results.toArray(new String[results.size()]);
    }

    /**
     * Checks whether the given input is a valid Intel email address.
     *
     * @param emailAddress {@link String} to check ({@code null} is also handled gracefully)
     *
     * @return {@code true} if and only if the given input is a valid Intel email address
     */
    public static boolean isValidIntelEmailAddress(CharSequence emailAddress)
    {
        if (emailAddress == null)
        {
            return false;
        }
        return Identity.EMAIL_ADDRESS.matcher(emailAddress).matches();
    }

    /**
     * Safely displays a standard Intel-internal login screen (unless the user has already entered
     * them recently, in which case the activity may finish before it starts). Either way, the
     * result is returned to {@link Activity#onActivityResult(int, int, Intent)}:
     *
     * <ul>
     *   <li>{@code resultCode} = {@link Activity#RESULT_OK} or {@link Activity#RESULT_CANCELED}</li>
     *   <li>Extra String: {@link #LOGIN_EXTRA_OUT_USERNAME} (may be empty)</li>
     *   <li>Extra String: {@link #LOGIN_EXTRA_OUT_PASSWORD} (may be empty)</li>
     *   <li>Extra String: {@link #LOGIN_EXTRA_OUT_DOMAIN} ({@code null} if {@code hideDomain} is {@code true})</li>
     * </ul>
     *
     * @param context     the caller's context
     * @param requestCode if >= 0, this code will be returned in onActivityResult()
     * @param title       login screen's title (such as your app's title)
     * @param hideDomain  hide domain selection?
     * @param forceGui    force GUI display, even if details already saved by user? (For example,
     *                    this is necessary in case of an authentication failure, to let the user
     *                    re-enter the details)
     *
     * @return {@code true} if an only if:
     *         <ul>
     *           <li>The "Intel Commons" app is installed</li>
     *           <li>Your app is signed with the same certificate as the "Intel Commons App"</li>
     *           <li>Your app's manifest uses the permission "com.intel.commons.permission.LOGIN"</li>
     *         </ul>
     */
    public static boolean startLoginActivity(Activity context, int requestCode, String title,
            boolean hideDomain, boolean forceGui)
    {
        try
        {
            final Intent intent = new Intent(Identity.LOGIN_ACTION)
                    .putExtra(Identity.LOGIN_EXTRA_IN_TITLE, title)
                    .putExtra(Identity.LOGIN_EXTRA_IN_HIDE_DOMAIN, hideDomain)
                    .putExtra(Identity.LOGIN_EXTRA_IN_FORCE_GUI, forceGui);

            context.startActivityForResult(intent, requestCode);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    /**
     * The recommended practice for retrieving the Intel user certificate and its private key,
     * assuming that they have already been pushed to the device by the "MobileIron" app.
     * <p>
     * Launches an {@link Activity} for the user to select the alias for a private key and
     * certificate pair for authentication. The selected alias or {@code null} will be returned
     * via the callback.
     * 
     * @param activity the context to use for launching a sub-{@link Activity} to
     *        prompt the user to select a private key, must not be {@code null}
     *
     * @param response callback to invoke when the request completes, must not be {@code null}
     *
     * @see KeyChain#choosePrivateKeyAlias(Activity, KeyChainAliasCallback, String[],
     *      java.security.Principal[], String, int, String)
     */
    public static void getIntelCertificate(final Activity activity,
            final IntelUserCertificateCallback response)
    {
        KeyChain.choosePrivateKeyAlias(activity, new KeyChainAliasCallback()
        {
            @Override
            public void alias(String alias)
            {
                // Sanity check: did the user dismiss the selection?
                if (alias == null)
                {
                    response.onUserDenied();
                    return;
                }
                // Attempt to retrieve this certificate
                try
                {
                    final X509Certificate certificate =
                            KeyChain.getCertificateChain(activity, alias)[0];

                    certificate.checkValidity();

                    // Another validity check: certificate contains Intel email address
                    for (List<?> list : certificate.getSubjectAlternativeNames())
                    {
                        final String rfc822Name = (String) list.get(1);
                        if (rfc822Name.endsWith("@intel.com"))
                        {
                            response.onUserSelection(alias, certificate); // Success
                            return;
                        }
                        else
                        {
                            throw new IllegalStateException("No Intel email address"); // Error
                        }
                    }
                    throw new IllegalStateException("No subject alternative names"); // Error
                }
                catch (Exception e)
                {
                    response.onCertificateError(alias, e);
                }
            }
        },
        null, null, null, -1, null);
    }
}
