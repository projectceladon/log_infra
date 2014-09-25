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

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

/**
 * Wrapper class providing a safe and standard implementation of communication with web servers.
 * Supports both HTTP and HTTPS, optional basic authentication, optional certificate pinning,
 * and configurable timeouts.
 * <p>
 * How to use this class: extend it to implement the callback method
 * {@link LoaderCallbacks#onLoadFinished(Loader, Object)}, and set configuration details during
 * runtime before calling {@link LoaderManager#initLoader(int, Bundle, LoaderCallbacks)}.
 * <p>
 * Applications which use this class must declare in their manifest that they use these permissions:
 * <ul>
 *   <li>{@link android.Manifest.permission.ACCESS_NETWORK_STATE}</li>
 *   <li>{@link android.Manifest.permission.INTERNET}</li>
 * </ul>
 *
 * @see http://developer.android.com/guide/components/loaders.html
 */
public abstract class WebLoader implements LoaderCallbacks<HttpResponse>
{
    public static final int DEFAULT_TIMEOUT = 4500;

    private Context mContext;
    private Bundle mArgs;

    /**
     * Constructor.
     *
     * @param context the caller's context
     *
     * @param uri HTTP or HTTPS Uniform Resource Identifier (URI)
     */
    public WebLoader(Context context, String uri)
    {
        mContext = context;
        mArgs = new Bundle();
        mArgs.putString(InternalWebLoader.KEY_URI, uri);
        setTimeout(WebLoader.DEFAULT_TIMEOUT);
    }

    public String getUri()
    {
        return mArgs.getString(InternalWebLoader.KEY_URI);
    }

    /**
     * Optional configuration.
     * <p>
     * Warning: this is a static setting for all transactions, meaning you can't use different
     * ones concurrently!
     */
    public String getAuthenticationUsername()
    {
        return mArgs.getString(InternalWebLoader.KEY_USERNAME);
    }

    /**
     * Optional configuration.
     * <p>
     * Warning: this is a static setting for all transactions, meaning you can't use different
     * ones concurrently!
     */
    public String getAuthenticationPassword()
    {
        return mArgs.getString(InternalWebLoader.KEY_PASSWORD);
    }

    /**
     * Optional configuration.
     * <p>
     * Warning: this is a static setting for all transactions, meaning you can't use different
     * ones concurrently!
     */
    public void setAuthentication(String username, String password)
    {
        mArgs.putString(InternalWebLoader.KEY_USERNAME, username);
        mArgs.putString(InternalWebLoader.KEY_PASSWORD, password);
    }

    public String getPinnedHttpsCertificateIssuer()
    {
        return mArgs.getString(InternalWebLoader.KEY_ISSUER);
    }

    public String getPinnedHttpsCertificateCommonName()
    {
        return mArgs.getString(InternalWebLoader.KEY_COMMON_NAME);
    }

    /**
     * Optional optimization for HTTPS transactions.
     * <p>
     * Warning: this is a static setting for all transactions, meaning you can't use different
     * ones concurrently!
     */
    public void pinHttpsCertificate(String issuer, String commonName)
    {
        mArgs.putString(InternalWebLoader.KEY_ISSUER, issuer);
        mArgs.putString(InternalWebLoader.KEY_COMMON_NAME, commonName);
    }

    /**
     * Optional parameter, measured in milliseconds, default = 4500.
     */
    public int getTimeout()
    {
        return mArgs.getInt(InternalWebLoader.KEY_TIMEOUT);
    }

    /**
     * Optional parameter, measured in milliseconds, default = 4500.
     */
    public void setTimeout(int timeout)
    {
        mArgs.putInt(InternalWebLoader.KEY_TIMEOUT, timeout);
    }

    @Override
    public Loader<HttpResponse> onCreateLoader(int id, Bundle args)
    {
        return new InternalWebLoader(mContext, mArgs);
    }

    @Override
    public void onLoaderReset(Loader<HttpResponse> loader)
    {
        // No cache to reuse or resources to clear
    }
}
